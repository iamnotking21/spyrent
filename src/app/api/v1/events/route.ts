import { sql } from "drizzle-orm";
import { db } from "@/db";
import { bearerToken, fail, json } from "@/lib/device";

type Incoming = {
  kind: "app" | "site";
  target: string;
  label?: string;
  minutes?: number;
  blocked?: boolean;
  occurredAt?: string;
};

/**
 * Device reports usage. Minutes also tick the matching rule budget.
 *
 * Everything happens in a single statement — resolving the device from its
 * token, inserting the events, and adding the minutes to the matching rules.
 * It used to be one query to authenticate, one to insert, and then one more per
 * event in a loop: a device reporting ten apps made a dozen round trips, which
 * is what made this endpoint feel slow.
 */
export async function POST(req: Request) {
  const token = bearerToken(req);
  if (!token) return fail("unauthorized", 401);

  const body = await req.json().catch(() => null);
  const list: Incoming[] = Array.isArray(body?.events) ? body.events : body ? [body] : [];

  const clean = list
    .filter((e) => e?.target && (e.kind === "app" || e.kind === "site"))
    .map((e) => ({
      kind: e.kind,
      target: String(e.target).toLowerCase(),
      label: e.label ? String(e.label) : null,
      minutes: Math.max(0, Math.round(Number(e.minutes ?? 0))),
      blocked: Boolean(e.blocked),
      occurredAt: e.occurredAt ? new Date(e.occurredAt).toISOString() : new Date().toISOString(),
    }));

  if (!clean.length) return fail("no valid events");

  const result = await db.execute(sql`
    with child as (
      select id from children where device_token = ${token} and active limit 1
    ),
    incoming as (
      select *
      from json_to_recordset(${JSON.stringify(clean)}::json)
        as t(kind text, target text, label text, minutes int, blocked boolean, "occurredAt" timestamptz)
    ),
    inserted as (
      insert into events (child_id, kind, target, label, minutes, blocked, occurred_at)
      select c.id, i.kind::event_kind, i.target, i.label, i.minutes, i.blocked, i."occurredAt"
      from incoming i cross join child c
      returning 1
    ),
    totals as (
      select kind, target, sum(minutes)::int as total
      from incoming
      group by kind, target
      having sum(minutes) > 0
    ),
    ticked as (
      update rules r
      set used_minutes = r.used_minutes + t.total
      from totals t, child c
      where r.child_id = c.id and r.kind = t.kind::rule_kind and r.target = t.target
      returning 1
    )
    select
      (select count(*) from child)::int as authorised,
      (select count(*) from inserted)::int as stored
  `);

  const rows = (Array.isArray(result) ? result : (result.rows ?? [])) as Array<{
    authorised: number;
    stored: number;
  }>;
  const row = rows[0];

  if (!row || row.authorised === 0) return fail("unauthorized", 401);

  return json({ ok: true, stored: row.stored });
}
