import { and, desc, eq, gte, inArray, sql } from "drizzle-orm";
import { db, apps, children, events, rules, timeRequests } from "@/db";
import { localDate } from "./budget";
import type { DayTotal } from "@/components/week-chart";

export type TopApp = {
  target: string;
  label: string;
  icon: string | null;
  minutes: number;
};

export type ChildSummary = {
  id: number;
  name: string;
  lastSeenAt: Date | null;
  todayMinutes: number;
  ruleCount: number;
  usedUpCount: number;
  pending: number;
};

export type ParentStats = {
  childCount: number;
  todayMinutes: number;
  yesterdayMinutes: number;
  weekMinutes: number;
  ruleCount: number;
  blockedThisWeek: number;
  pendingRequests: number;
  week: DayTotal[];
  topApps: TopApp[];
  perChild: ChildSummary[];
};

/**
 * Everything the parent dashboard shows, in one pass.
 *
 * Days are bucketed in each child's own timezone: a household spread across
 * zones should still see "today" mean today for each of them.
 */
export async function statsFor(parentId: number): Promise<ParentStats> {
  const kids = await db
    .select()
    .from(children)
    .where(eq(children.parentId, parentId))
    .orderBy(desc(children.createdAt));

  const empty: ParentStats = {
    childCount: 0,
    todayMinutes: 0,
    yesterdayMinutes: 0,
    weekMinutes: 0,
    ruleCount: 0,
    blockedThisWeek: 0,
    pendingRequests: 0,
    week: [],
    topApps: [],
    perChild: [],
  };

  if (kids.length === 0) return empty;

  const ids = kids.map((k) => k.id);
  const weekAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);

  const [eventRows, ruleRows, pendingRows, iconRows] = await Promise.all([
    db
      .select()
      .from(events)
      .where(and(inArray(events.childId, ids), gte(events.occurredAt, weekAgo))),
    db.select().from(rules).where(inArray(rules.childId, ids)),
    db
      .select({ childId: timeRequests.childId })
      .from(timeRequests)
      .where(and(inArray(timeRequests.childId, ids), eq(timeRequests.status, "pending"))),
    db
      .select({ packageName: apps.packageName, label: apps.label, icon: apps.icon })
      .from(apps)
      .where(inArray(apps.childId, ids)),
  ]);

  const zoneOf = new Map(kids.map((k) => [k.id, k.timezone]));
  const iconOf = new Map(iconRows.map((a) => [a.packageName, a]));

  const today = (childId: number) => localDate(zoneOf.get(childId) ?? "UTC");
  const yesterdayFor = (childId: number) =>
    localDate(zoneOf.get(childId) ?? "UTC", new Date(Date.now() - 24 * 60 * 60 * 1000));

  let todayMinutes = 0;
  let yesterdayMinutes = 0;
  let weekMinutes = 0;
  let blockedThisWeek = 0;

  const byDay = new Map<string, number>();
  const byApp = new Map<string, number>();
  const todayByChild = new Map<number, number>();

  for (const row of eventRows) {
    const zone = zoneOf.get(row.childId) ?? "UTC";
    const day = localDate(zone, row.occurredAt);

    weekMinutes += row.minutes;
    byDay.set(day, (byDay.get(day) ?? 0) + row.minutes);

    if (row.blocked) blockedThisWeek++;

    if (day === today(row.childId)) {
      todayMinutes += row.minutes;
      todayByChild.set(row.childId, (todayByChild.get(row.childId) ?? 0) + row.minutes);
    } else if (day === yesterdayFor(row.childId)) {
      yesterdayMinutes += row.minutes;
    }

    if (row.kind === "app" && row.minutes > 0) {
      byApp.set(row.target, (byApp.get(row.target) ?? 0) + row.minutes);
    }
  }

  // the week chart uses the first child's zone; with one child that is exact,
  // and with several it is close enough for a shape at a glance
  const chartZone = kids[0].timezone;
  const week: DayTotal[] = [];
  for (let back = 6; back >= 0; back--) {
    const at = new Date(Date.now() - back * 24 * 60 * 60 * 1000);
    const date = localDate(chartZone, at);
    week.push({
      date,
      label: new Intl.DateTimeFormat("en", { weekday: "short", timeZone: chartZone }).format(at),
      minutes: byDay.get(date) ?? 0,
    });
  }

  const topApps: TopApp[] = [...byApp.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)
    .map(([target, minutes]) => {
      const known = iconOf.get(target);
      return {
        target,
        label: known?.label ?? eventLabel(eventRows, target) ?? target,
        icon: known?.icon ?? null,
        minutes,
      };
    });

  const pendingByChild = new Map<number, number>();
  for (const p of pendingRows) {
    pendingByChild.set(p.childId, (pendingByChild.get(p.childId) ?? 0) + 1);
  }

  const perChild: ChildSummary[] = kids.map((kid) => {
    const forKid = ruleRows.filter((r) => r.childId === kid.id);
    return {
      id: kid.id,
      name: kid.name,
      lastSeenAt: kid.lastSeenAt,
      todayMinutes: todayByChild.get(kid.id) ?? 0,
      ruleCount: forKid.length,
      usedUpCount: forKid.filter(
        (r) => r.dailyMinutes !== null && r.usedMinutes >= r.dailyMinutes + r.bonusMinutes,
      ).length,
      pending: pendingByChild.get(kid.id) ?? 0,
    };
  });

  return {
    childCount: kids.length,
    todayMinutes,
    yesterdayMinutes,
    weekMinutes,
    ruleCount: ruleRows.length,
    blockedThisWeek,
    pendingRequests: pendingRows.length,
    week,
    topApps,
    perChild,
  };
}

/** Fall back to whatever label the device sent with the event. */
function eventLabel(rows: Array<{ target: string; label: string | null }>, target: string) {
  return rows.find((r) => r.target === target && r.label)?.label ?? null;
}

/** Sum of every rule's daily allowance, for context next to today's usage. */
export async function allowanceFor(parentId: number): Promise<number> {
  const [row] = await db
    .select({
      total: sql<number>`coalesce(sum(${rules.dailyMinutes} + ${rules.bonusMinutes}), 0)::int`,
    })
    .from(rules)
    .innerJoin(children, eq(children.id, rules.childId))
    .where(and(eq(children.parentId, parentId), eq(rules.kind, "app")));

  return row?.total ?? 0;
}
