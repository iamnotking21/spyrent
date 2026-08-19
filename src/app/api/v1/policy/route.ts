import { sql } from "drizzle-orm";
import { db } from "@/db";
import { bearerToken, fail, json } from "@/lib/device";

type Row = {
  child_id: number;
  child_name: string;
  kind: "app" | "site" | null;
  target: string | null;
  label: string | null;
  daily_minutes: number | null;
  used_minutes: number | null;
  bonus_minutes: number | null;
  blocked: boolean | null;
};

/**
 * Full rule set the child device must enforce.
 *
 * The device polls this every twenty seconds, so it is one round trip: the
 * token lookup, the day rollover, the last-seen touch and the rule read all
 * happen in a single statement.
 *
 * Two details worth keeping in mind when editing this:
 *
 *  - the CASE expressions exist because a statement's SELECT reads the snapshot
 *    from before its own UPDATE, so without them a device syncing across
 *    midnight would be handed yesterday's spent minutes exactly once;
 *  - the join is a LEFT JOIN so a paired child with no rules yet still returns
 *    a row, which is how the response knows the token was valid.
 */
export async function GET(req: Request) {
  const token = bearerToken(req);
  if (!token) return fail("unauthorized", 401);


  const result = await db.execute(sql`
    with child as (
      select id, name, timezone
      from children
      where device_token = ${token} and active
      limit 1
    ),
    today as (
      select
        c.id,
        c.name,
        to_char(now() at time zone c.timezone, 'YYYY-MM-DD') as day
      from child c
    ),
    rolled as (
      update rules r
      set used_minutes = 0, bonus_minutes = 0, reset_on = t.day
      from today t
      where r.child_id = t.id and (r.reset_on is null or r.reset_on <> t.day)
      returning r.id
    ),
    seen as (
      update children set last_seen_at = now()
      where id = (select id from today)
      returning id
    )
    select
      t.id as child_id,
      t.name as child_name,
      r.kind,
      r.target,
      r.label,
      r.daily_minutes,
      case when r.reset_on is distinct from t.day then 0 else r.used_minutes end as used_minutes,
      case when r.reset_on is distinct from t.day then 0 else r.bonus_minutes end as bonus_minutes,
      r.blocked
    from today t
    left join rules r on r.child_id = t.id
  `);

  const rows = (Array.isArray(result) ? result : (result.rows ?? [])) as Row[];
  if (rows.length === 0) return fail("unauthorized", 401);

  const withRules = rows.filter((r) => r.kind !== null);

  return json({
    ok: true,
    child: { id: rows[0].child_id, name: rows[0].child_name },
    apps: withRules
      .filter((r) => r.kind === "app")
      .map((r) => ({
        packageName: r.target,
        label: r.label ?? r.target,
        dailyMinutes: r.daily_minutes === null ? null : r.daily_minutes + (r.bonus_minutes ?? 0),
        usedMinutes: r.used_minutes ?? 0,
        bonusMinutes: r.bonus_minutes ?? 0,
        blocked: r.blocked ?? false,
      })),
    sites: withRules
      .filter((r) => r.kind === "site")
      .map((r) => ({
        domain: r.target,
        label: r.label ?? r.target,
        dailyMinutes: r.daily_minutes === null ? null : r.daily_minutes + (r.bonus_minutes ?? 0),
        usedMinutes: r.used_minutes ?? 0,
        blocked: r.blocked ?? false,
      })),
  });
}
