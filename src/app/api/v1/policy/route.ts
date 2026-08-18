import { sql } from "drizzle-orm";
import { db } from "@/db";
import { authDevice, fail, json } from "@/lib/device";
import { localDate } from "@/lib/budget";

type Row = {
  kind: "app" | "site";
  target: string;
  label: string | null;
  daily_minutes: number | null;
  used_minutes: number;
  bonus_minutes: number;
  blocked: boolean;
};

/**
 * Full rule set the child device must enforce.
 *
 * The device polls this every twenty seconds, so it is worth keeping to a
 * single database round trip. Everything happens in one statement:
 *
 *  - rules whose stored day is not today get rolled over (the UPDATE);
 *  - last_seen_at is touched, so a separate heartbeat is not needed for
 *    freshness;
 *  - the SELECT returns each rule with today's effective figures.
 *
 * The CASE expressions matter: a statement's SELECT reads the snapshot from
 * before its own UPDATE, so without them a device syncing across midnight
 * would be handed yesterday's spent minutes exactly once.
 */
export async function GET(req: Request) {
  const c = await authDevice(req);
  if (!c) return fail("unauthorized", 401);

  const today = localDate(c.timezone);

  const result = await db.execute(sql`
    with rolled as (
      update rules
      set used_minutes = 0, bonus_minutes = 0, reset_on = ${today}
      where child_id = ${c.id} and (reset_on is null or reset_on <> ${today})
      returning id
    ), seen as (
      update children set last_seen_at = now() where id = ${c.id} returning id
    )
    select
      kind,
      target,
      label,
      daily_minutes,
      case when reset_on is distinct from ${today} then 0 else used_minutes end as used_minutes,
      case when reset_on is distinct from ${today} then 0 else bonus_minutes end as bonus_minutes,
      blocked
    from rules
    where child_id = ${c.id}
  `);

  const rows = (Array.isArray(result) ? result : (result.rows ?? [])) as Row[];

  return json({
    ok: true,
    child: { id: c.id, name: c.name },
    apps: rows
      .filter((r) => r.kind === "app")
      .map((r) => ({
        packageName: r.target,
        label: r.label ?? r.target,
        dailyMinutes: r.daily_minutes === null ? null : r.daily_minutes + r.bonus_minutes,
        usedMinutes: r.used_minutes,
        bonusMinutes: r.bonus_minutes,
        blocked: r.blocked,
      })),
    sites: rows
      .filter((r) => r.kind === "site")
      .map((r) => ({
        domain: r.target,
        label: r.label ?? r.target,
        dailyMinutes: r.daily_minutes === null ? null : r.daily_minutes + r.bonus_minutes,
        usedMinutes: r.used_minutes,
        blocked: r.blocked,
      })),
  });
}
