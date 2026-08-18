import { and, eq, ne, or, isNull, sql } from "drizzle-orm";
import { db, rules, type Child } from "@/db";

/** The calendar date where this child lives, as YYYY-MM-DD. */
export function localDate(timezone: string, at: Date = new Date()): string {
  try {
    return new Intl.DateTimeFormat("en-CA", {
      timeZone: timezone,
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).format(at);
  } catch {
    // an unknown zone should not stop the clock
    return new Intl.DateTimeFormat("en-CA", {
      timeZone: "UTC",
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).format(at);
  }
}

/**
 * Roll the child's budgets over if their local day has moved on.
 *
 * A nightly cron cannot do this alone: Vercel's free plan runs one job a day,
 * at a single moment in UTC, while families live in every zone. So the rollover
 * happens whenever the device asks for its policy, and the cron is a backstop
 * for devices that have been switched off.
 */
export async function rolloverIfNeeded(child: Pick<Child, "id" | "timezone">): Promise<string> {
  const today = localDate(child.timezone);

  await db
    .update(rules)
    .set({ usedMinutes: 0, bonusMinutes: 0, resetOn: today })
    .where(
      and(
        eq(rules.childId, child.id),
        or(isNull(rules.resetOn), ne(rules.resetOn, today)),
      ),
    );

  return today;
}

/** Same rollover for every child, used by the nightly job. */
export async function rolloverAll(): Promise<number> {
  const rows = await db.execute(sql`
    update rules r
    set used_minutes = 0,
        bonus_minutes = 0,
        reset_on = to_char(now() at time zone c.timezone, 'YYYY-MM-DD')
    from children c
    where c.id = r.child_id
      and r.reset_on is distinct from to_char(now() at time zone c.timezone, 'YYYY-MM-DD')
    returning r.id`);

  return Array.isArray(rows) ? rows.length : (rows.rowCount ?? 0);
}
