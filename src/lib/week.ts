import { and, gte, eq } from "drizzle-orm";
import { db, events } from "@/db";
import { localDate } from "./budget";
import type { DayTotal } from "@/components/week-chart";

/** The last seven local days for one child, oldest first. */
export async function weekFor(childId: number, timezone: string): Promise<DayTotal[]> {
  const since = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);

  const rows = await db
    .select()
    .from(events)
    .where(and(eq(events.childId, childId), gte(events.occurredAt, since)));

  const totals = new Map<string, number>();
  for (const row of rows) {
    const day = localDate(timezone, row.occurredAt);
    totals.set(day, (totals.get(day) ?? 0) + row.minutes);
  }

  const days: DayTotal[] = [];
  for (let back = 6; back >= 0; back--) {
    const at = new Date(Date.now() - back * 24 * 60 * 60 * 1000);
    const date = localDate(timezone, at);
    days.push({
      date,
      label: new Intl.DateTimeFormat("en", { weekday: "short", timeZone: timezone }).format(at),
      minutes: totals.get(date) ?? 0,
    });
  }

  return days;
}
