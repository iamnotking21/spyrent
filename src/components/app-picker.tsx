"use client";

import { useActionState, useMemo, useState } from "react";
import { useFormStatus } from "react-dom";
import { upsertRuleAction } from "@/app/actions";
import { fmtMinutes } from "@/lib/utils";

type State = { error?: string; notice?: string } | undefined;

export type PickableApp = {
  packageName: string;
  label: string;
  icon: string | null;
  minutesThisWeek: number;
  ruled: boolean;
};

function Submit() {
  const { pending } = useFormStatus();
  return (
    <button type="submit" className="btn btn-primary w-full" disabled={pending}>
      {pending ? "Saving…" : "Save limit"}
    </button>
  );
}

function Fallback({ label }: { label: string }) {
  return (
    <span
      aria-hidden
      className="w-9 h-9 rounded-lg bg-brand-50 text-brand-700 grid place-items-center text-sm font-bold shrink-0"
    >
      {label.slice(0, 1).toUpperCase()}
    </span>
  );
}

/**
 * Picking an app to limit. The old control was a bare dropdown of package
 * labels, which told a parent nothing about what their child actually uses —
 * so this leads with the busiest apps and shows the icons from the device.
 */
export function AppPicker({ apps, childId }: { apps: PickableApp[]; childId: number }) {
  const [query, setQuery] = useState("");
  const [selected, setSelected] = useState<string | null>(null);
  const [showAll, setShowAll] = useState(false);
  const [state, action] = useActionState<State, FormData>(upsertRuleAction, undefined);

  const matches = useMemo(() => {
    const needle = query.trim().toLowerCase();
    const filtered = needle
      ? apps.filter(
          (a) =>
            a.label.toLowerCase().includes(needle) ||
            a.packageName.toLowerCase().includes(needle),
        )
      : apps;

    // busiest first: that is what a parent came here to deal with
    return [...filtered].sort(
      (a, b) => b.minutesThisWeek - a.minutesThisWeek || a.label.localeCompare(b.label),
    );
  }, [apps, query]);

  const collapsedCount = 8;
  const shown = query || showAll ? matches : matches.slice(0, collapsedCount);
  const hiddenCount = query ? 0 : Math.max(0, matches.length - collapsedCount);

  const chosen = apps.find((a) => a.packageName === selected) ?? null;

  if (apps.length === 0) {
    return (
      <p className="text-sm text-ink-500">
        Nothing yet. The list fills in once the device has checked in — that happens within
        about fifteen minutes of pairing.
      </p>
    );
  }

  return (
    <div className="space-y-3">
      <input
        type="search"
        className="input"
        placeholder={`Search ${apps.length} apps on the device`}
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        aria-label="Search installed apps"
      />

      <ul className="space-y-1.5 max-h-96 overflow-y-auto pr-1">
        {shown.map((app) => {
          const isSelected = app.packageName === selected;
          return (
            <li key={app.packageName}>
              <button
                type="button"
                onClick={() => setSelected(isSelected ? null : app.packageName)}
                aria-pressed={isSelected}
                className={`w-full flex items-center gap-3 rounded-xl px-3 py-2.5 text-left transition-colors ${
                  isSelected ? "bg-brand-50 ring-1 ring-brand-500" : "bg-paper hover:bg-brand-50"
                }`}
              >
                {app.icon ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={app.icon}
                    alt=""
                    width={36}
                    height={36}
                    className="w-9 h-9 rounded-lg shrink-0"
                  />
                ) : (
                  <Fallback label={app.label} />
                )}

                <span className="min-w-0 flex-1">
                  <span className="block font-semibold truncate">{app.label}</span>
                  <span className="block text-xs text-ink-500">
                    {app.minutesThisWeek > 0
                      ? `${fmtMinutes(app.minutesThisWeek)} this week`
                      : "not used this week"}
                    {app.ruled ? " · already limited" : ""}
                  </span>
                </span>
              </button>
            </li>
          );
        })}

        {shown.length === 0 ? (
          <li className="text-sm text-ink-500 px-1 py-2">Nothing matches that.</li>
        ) : null}
      </ul>

      {hiddenCount > 0 ? (
        <button
          type="button"
          onClick={() => setShowAll(true)}
          className="text-sm font-semibold text-brand-700 hover:text-brand-600"
        >
          Show {hiddenCount} more app{hiddenCount === 1 ? "" : "s"}
        </button>
      ) : null}

      {showAll && !query ? (
        <button
          type="button"
          onClick={() => setShowAll(false)}
          className="text-sm text-ink-500 hover:text-ink-700"
        >
          Show fewer
        </button>
      ) : null}

      {chosen ? (
        <form action={action} className="rounded-xl border border-line p-4 space-y-3">
          <input type="hidden" name="childId" value={childId} />
          <input type="hidden" name="kind" value="app" />
          <p className="text-sm">
            <span className="font-semibold">{chosen.label}</span>
            <span className="block text-xs text-ink-500 truncate">{chosen.packageName}</span>
          </p>

          <input type="hidden" name="target" value={chosen.packageName} />
          <input type="hidden" name="label" value={chosen.label} />

          <div>
            <label className="label" htmlFor="dailyMinutes">
              Minutes a day
            </label>
            <input
              id="dailyMinutes"
              name="dailyMinutes"
              type="number"
              min={0}
              className="input"
              placeholder="60 — leave empty to block it outright"
            />
          </div>

          {state?.error ? (
            <p className="text-sm font-medium text-rose-ink bg-rose-soft rounded-xl px-3 py-2">
              {state.error}
            </p>
          ) : null}

          <Submit />
        </form>
      ) : (
        <p className="text-xs text-ink-500">Pick an app to set its limit.</p>
      )}
    </div>
  );
}
