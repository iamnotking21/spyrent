import { fmtMinutes } from "@/lib/utils";

export type DayTotal = { date: string; label: string; minutes: number };

/**
 * Seven days of screen time. Deliberately plain bars rather than a chart
 * library: it is one number a day, and a parent reads it at a glance.
 */
export function WeekChart({ days }: { days: DayTotal[] }) {
  const peak = Math.max(...days.map((d) => d.minutes), 1);
  const total = days.reduce((sum, d) => sum + d.minutes, 0);
  const average = Math.round(total / (days.length || 1));

  return (
    <div>
      <div className="flex items-end gap-2 h-40" role="img" aria-label="Screen time over the last seven days">
        {days.map((day) => {
          const height = Math.max(4, Math.round((day.minutes / peak) * 100));
          const isPeak = day.minutes === peak && peak > 0;
          return (
            <div key={day.date} className="flex-1 flex flex-col items-center justify-end gap-2">
              <span className="text-xs text-ink-500 tabular-nums">
                {day.minutes > 0 ? fmtMinutes(day.minutes) : ""}
              </span>
              <div
                className={`w-full rounded-lg ${isPeak ? "bg-amber-ink" : "bg-brand-500"}`}
                style={{ height: `${height}%` }}
                title={`${day.label}: ${fmtMinutes(day.minutes)}`}
              />
              <span className="text-xs text-ink-500">{day.label}</span>
            </div>
          );
        })}
      </div>

      <p className="text-sm text-ink-700 mt-4">
        {total === 0
          ? "Nothing reported this week."
          : `${fmtMinutes(total)} across the week, about ${fmtMinutes(average)} a day.`}
      </p>
    </div>
  );
}
