import Link from "next/link";
import { cx, fmtMinutes } from "@/lib/utils";
import type { ParentStats, TopApp } from "@/lib/stats";

/** A number with a plain-language line under it. */
export function StatTile({
  label,
  value,
  hint,
  tone = "brand",
}: {
  label: string;
  value: string;
  hint?: string;
  tone?: "brand" | "amber" | "rose" | "muted";
}) {
  const tones = {
    brand: "bg-brand-50 text-brand-700",
    amber: "bg-amber-soft text-amber-ink",
    rose: "bg-rose-soft text-rose-ink",
    muted: "bg-paper text-ink-500",
  } as const;

  return (
    <div className="card p-5">
      <div className={cx("pill mb-3", tones[tone])}>{label}</div>
      <div className="text-3xl font-bold font-[family-name:var(--font-display)]">{value}</div>
      {hint ? <p className="text-sm text-ink-500 mt-1.5">{hint}</p> : null}
    </div>
  );
}

/** Today against yesterday, said in words rather than a percentage. */
function trend(today: number, yesterday: number): string {
  if (yesterday === 0 && today === 0) return "Nothing reported yet";
  if (yesterday === 0) return "First day with anything reported";

  const diff = today - yesterday;
  if (Math.abs(diff) < 5) return "About the same as yesterday";
  return diff > 0
    ? `${fmtMinutes(diff)} more than yesterday`
    : `${fmtMinutes(-diff)} less than yesterday`;
}

export function TopApps({ apps }: { apps: TopApp[] }) {
  if (apps.length === 0) {
    return <p className="text-sm text-ink-500">No app time reported this week.</p>;
  }

  const peak = apps[0].minutes || 1;

  return (
    <ul className="space-y-3">
      {apps.map((app) => (
        <li key={app.target} className="flex items-center gap-3">
          {app.icon ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={app.icon} alt="" width={32} height={32} className="w-8 h-8 rounded-lg shrink-0" />
          ) : (
            <span
              aria-hidden
              className="w-8 h-8 rounded-lg bg-brand-50 text-brand-700 grid place-items-center text-xs font-bold shrink-0"
            >
              {app.label.slice(0, 1).toUpperCase()}
            </span>
          )}

          <span className="min-w-0 flex-1">
            <span className="flex justify-between gap-3 text-sm mb-1">
              <span className="font-semibold truncate">{app.label}</span>
              <span className="text-ink-500 shrink-0">{fmtMinutes(app.minutes)}</span>
            </span>
            <span className="block h-1.5 rounded-full bg-line overflow-hidden">
              <span
                className="block h-full rounded-full bg-brand-500"
                style={{ width: `${Math.max(4, Math.round((app.minutes / peak) * 100))}%` }}
              />
            </span>
          </span>
        </li>
      ))}
    </ul>
  );
}

export function ChildRows({ rows }: { rows: ParentStats["perChild"] }) {
  return (
    <div className="grid sm:grid-cols-2 gap-4">
      {rows.map((child) => (
        <Link
          key={child.id}
          href={`/portal/children/${child.id}`}
          className="card p-5 block hover:border-brand-500 transition-colors"
        >
          <div className="flex items-start justify-between gap-3">
            <h3 className="text-lg font-bold">{child.name}</h3>
            {child.pending > 0 ? (
              <span className="pill bg-amber-soft text-amber-ink">
                {child.pending} waiting
              </span>
            ) : child.lastSeenAt ? (
              <span className="pill bg-brand-50 text-brand-700">paired</span>
            ) : (
              <span className="pill bg-paper text-ink-500">waiting to pair</span>
            )}
          </div>

          <p className="text-2xl font-bold mt-3 font-[family-name:var(--font-display)]">
            {fmtMinutes(child.todayMinutes)}
            <span className="text-sm font-medium text-ink-500"> today</span>
          </p>

          <p className="text-sm text-ink-500 mt-1">
            {child.ruleCount === 0
              ? "No limits set yet"
              : `${child.ruleCount} ${child.ruleCount === 1 ? "limit" : "limits"}${
                  child.usedUpCount > 0 ? ` · ${child.usedUpCount} used up` : ""
                }`}
          </p>

          <p className="text-sm text-brand-700 font-semibold mt-4">Open dashboard →</p>
        </Link>
      ))}
    </div>
  );
}

export function StatRow({ stats }: { stats: ParentStats }) {
  return (
    <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-5">
      <StatTile
        label="Screen time today"
        value={fmtMinutes(stats.todayMinutes)}
        hint={trend(stats.todayMinutes, stats.yesterdayMinutes)}
        tone="amber"
      />
      <StatTile
        label="This week"
        value={fmtMinutes(stats.weekMinutes)}
        hint={`About ${fmtMinutes(Math.round(stats.weekMinutes / 7))} a day`}
      />
      <StatTile
        label="Limits in place"
        value={String(stats.ruleCount)}
        hint={
          stats.blockedThisWeek > 0
            ? `${stats.blockedThisWeek} ${stats.blockedThisWeek === 1 ? "block" : "blocks"} this week`
            : "Nothing blocked this week"
        }
      />
      <StatTile
        label="Waiting on you"
        value={String(stats.pendingRequests)}
        hint={stats.pendingRequests === 0 ? "No requests to answer" : "Open a child to answer"}
        tone={stats.pendingRequests > 0 ? "rose" : "muted"}
      />
    </div>
  );
}
