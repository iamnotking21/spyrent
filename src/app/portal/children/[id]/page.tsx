import { notFound } from "next/navigation";
import { and, desc, eq, gte, sql } from "drizzle-orm";
import { db, children, rules, apps, events, timeRequests, auditLog } from "@/db";
import { requireUser } from "@/lib/guard";
import {
  upsertRuleAction,
  deleteRuleAction,
  rotateTokenAction,
  removeChildAction,
  answerRequestAction,
} from "@/app/actions";
import { ActionForm, Field } from "@/components/forms";
import { Badge, SectionTitle } from "@/components/ui";
import { fmtDate, fmtMinutes } from "@/lib/utils";
import { describe } from "@/lib/audit";
import { weekFor } from "@/lib/week";
import { WeekChart } from "@/components/week-chart";
import { AppPicker, type PickableApp } from "@/components/app-picker";

export default async function ChildPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const childId = Number(id);
  const s = await requireUser();

  const [child] = await db.select().from(children).where(eq(children.id, childId)).limit(1);
  if (!child) notFound();
  if (s.role !== "admin" && child.parentId !== s.uid) notFound();

  const weekAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);

  // every read the page needs, issued together — the week summary and the
  // per-app totals used to run after this batch, adding two serial round trips
  // to a page that already had everything else in flight
  const [ruleRows, appRows, eventRows, requestRows, auditRows, week, usage] = await Promise.all([
    db.select().from(rules).where(eq(rules.childId, childId)).orderBy(desc(rules.createdAt)),
    db.select().from(apps).where(eq(apps.childId, childId)).orderBy(desc(apps.seenAt)),
    db
      .select()
      .from(events)
      .where(eq(events.childId, childId))
      .orderBy(desc(events.occurredAt))
      .limit(25),
    db
      .select()
      .from(timeRequests)
      .where(and(eq(timeRequests.childId, childId), eq(timeRequests.status, "pending")))
      .orderBy(desc(timeRequests.createdAt)),
    db
      .select()
      .from(auditLog)
      .where(eq(auditLog.childId, childId))
      .orderBy(desc(auditLog.createdAt))
      .limit(15),
    weekFor(childId, child.timezone),
    db
      .select({
        target: events.target,
        minutes: sql<number>`coalesce(sum(${events.minutes}), 0)::int`,
      })
      .from(events)
      .where(and(eq(events.childId, childId), gte(events.occurredAt, weekAgo)))
      .groupBy(events.target),
  ]);

  const minutesByPackage = new Map(usage.map((u) => [u.target, u.minutes]));
  const ruledPackages = new Set(
    ruleRows.filter((r) => r.kind === "app").map((r) => r.target),
  );

  const pickable: PickableApp[] = appRows.map((a) => ({
    packageName: a.packageName,
    label: a.label,
    icon: a.icon,
    minutesThisWeek: minutesByPackage.get(a.packageName) ?? 0,
    ruled: ruledPackages.has(a.packageName),
  }));

  const appRules = ruleRows.filter((r) => r.kind === "app");
  const siteRules = ruleRows.filter((r) => r.kind === "site");

  return (
    <div className="space-y-10">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold">{child.name}</h1>
          <p className="text-ink-700 mt-1">Last seen {fmtDate(child.lastSeenAt)}</p>
        </div>
        <form action={removeChildAction}>
          <input type="hidden" name="childId" value={child.id} />
          <button className="btn btn-ghost text-rose-ink">Remove child</button>
        </form>
      </div>

      {requestRows.length > 0 ? (
        <section className="card p-6 border-amber-soft">
          <SectionTitle
            title="Waiting on you"
            hint="Granting adds the minutes to today only — tomorrow the usual limit is back."
          />
          <ul className="space-y-2">
            {requestRows.map((r) => (
              <li
                key={r.id}
                className="flex flex-wrap items-center justify-between gap-3 rounded-xl bg-amber-soft/40 px-4 py-3"
              >
                <div>
                  <p className="font-semibold">
                    {r.label ?? r.target} — {fmtMinutes(r.minutes)} more
                  </p>
                  <p className="text-xs text-ink-500">asked {fmtDate(r.createdAt)}</p>
                </div>
                <div className="flex items-center gap-2">
                  <form action={answerRequestAction}>
                    <input type="hidden" name="requestId" value={r.id} />
                    <input type="hidden" name="grant" value="true" />
                    <button className="btn btn-primary py-1.5">Give {r.minutes} min</button>
                  </form>
                  <form action={answerRequestAction}>
                    <input type="hidden" name="requestId" value={r.id} />
                    <input type="hidden" name="grant" value="false" />
                    <button className="btn btn-ghost py-1.5">Not now</button>
                  </form>
                </div>
              </li>
            ))}
          </ul>
        </section>
      ) : null}

      <section className="card p-6">
        <SectionTitle title="This week" hint="Screen time reported by the device, by day." />
        <WeekChart days={week} />
      </section>

      <div className="card p-6">
        <SectionTitle
          title="Pairing"
          hint="Type this token into the Spyrent app on the child device."
        />
        <div className="flex flex-wrap items-center gap-3">
          <code className="rounded-xl bg-paper px-4 py-2.5 font-mono text-sm">
            {child.deviceToken}
          </code>
          <form action={rotateTokenAction}>
            <input type="hidden" name="childId" value={child.id} />
            <button className="btn btn-ghost">Generate new token</button>
          </form>
        </div>
      </div>

      <section className="grid lg:grid-cols-2 gap-6 items-start">
        <div className="card p-6">
          <SectionTitle
            title="App limits"
            hint="Busiest apps first. Leave the minutes empty to block one outright."
          />
          <AppPicker apps={pickable} childId={child.id} />

          <ul className="mt-6 space-y-2">
            {appRules.map((r) => (
              <li
                key={r.id}
                className="flex items-center justify-between gap-3 rounded-xl bg-paper px-4 py-3"
              >
                <div className="min-w-0">
                  <p className="font-semibold truncate">{r.label ?? r.target}</p>
                  <p className="text-xs text-ink-500 truncate">{r.target}</p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  {r.dailyMinutes === null ? (
                    <Badge tone="rose">blocked</Badge>
                  ) : (
                    <Badge
                      tone={
                        r.usedMinutes >= r.dailyMinutes + r.bonusMinutes ? "rose" : "amber"
                      }
                    >
                      {fmtMinutes(r.usedMinutes)} / {fmtMinutes(r.dailyMinutes + r.bonusMinutes)}
                      {r.bonusMinutes > 0 ? ` (+${r.bonusMinutes} today)` : ""}
                      {r.usedMinutes >= r.dailyMinutes + r.bonusMinutes ? " · used up" : ""}
                    </Badge>
                  )}
                  <form action={deleteRuleAction}>
                    <input type="hidden" name="ruleId" value={r.id} />
                    <button className="text-sm text-ink-500 hover:text-rose-ink">Remove</button>
                  </form>
                </div>
              </li>
            ))}
            {appRules.length === 0 ? (
              <li className="text-sm text-ink-500">No app rules yet.</li>
            ) : null}
          </ul>
        </div>

        <div className="card p-6">
          <SectionTitle title="Blocked sites" hint="One domain per rule, e.g. tiktok.com." />
          <ActionForm action={upsertRuleAction} submitLabel="Block site">
            <input type="hidden" name="childId" value={child.id} />
            <input type="hidden" name="kind" value="site" />
            <Field label="Domain" name="target" placeholder="tiktok.com" />
            <Field
              label="Daily minutes"
              name="dailyMinutes"
              type="number"
              placeholder="empty = block"
              required={false}
            />
          </ActionForm>

          <ul className="mt-6 space-y-2">
            {siteRules.map((r) => (
              <li
                key={r.id}
                className="flex items-center justify-between gap-3 rounded-xl bg-paper px-4 py-3"
              >
                <p className="font-semibold truncate">{r.target}</p>
                <div className="flex items-center gap-2 shrink-0">
                  {r.dailyMinutes === null ? (
                    <Badge tone="rose">blocked</Badge>
                  ) : (
                    <Badge tone="amber">{fmtMinutes(r.dailyMinutes)}/day</Badge>
                  )}
                  <form action={deleteRuleAction}>
                    <input type="hidden" name="ruleId" value={r.id} />
                    <button className="text-sm text-ink-500 hover:text-rose-ink">Remove</button>
                  </form>
                </div>
              </li>
            ))}
            {siteRules.length === 0 ? (
              <li className="text-sm text-ink-500">Nothing blocked yet.</li>
            ) : null}
          </ul>
        </div>
      </section>

      <section className="card p-6">
        <SectionTitle title="Changes you made" hint="The last fifteen, newest first." />
        {auditRows.length === 0 ? (
          <p className="text-sm text-ink-500">Nothing changed yet.</p>
        ) : (
          <ul className="divide-y divide-line">
            {auditRows.map((a) => (
              <li key={a.id} className="py-2.5 flex justify-between gap-4">
                <p className="text-sm">{describe(a.action, a.detail)}</p>
                <span className="text-xs text-ink-500 shrink-0">{fmtDate(a.createdAt)}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="card p-6">
        <SectionTitle title="Recent activity" hint="Newest first, straight from the device." />
        {eventRows.length === 0 ? (
          <p className="text-sm text-ink-500">Nothing reported yet.</p>
        ) : (
          <ul className="divide-y divide-line">
            {eventRows.map((e) => (
              <li key={e.id} className="py-3 flex items-center justify-between gap-4">
                <div className="min-w-0">
                  <p className="font-medium truncate">{e.label ?? e.target}</p>
                  <p className="text-xs text-ink-500">{fmtDate(e.occurredAt)}</p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <Badge tone="muted">{e.kind}</Badge>
                  {e.blocked ? (
                    <Badge tone="rose">blocked</Badge>
                  ) : (
                    <Badge>{fmtMinutes(e.minutes)}</Badge>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
