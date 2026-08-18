import { desc, eq, inArray } from "drizzle-orm";
import { db, children, events } from "@/db";
import { requireUser } from "@/lib/guard";
import { Badge, Empty } from "@/components/ui";
import { fmtDate, fmtMinutes } from "@/lib/utils";

export default async function ActivityPage() {
  const s = await requireUser();
  const kids = await db.select().from(children).where(eq(children.parentId, s.uid));
  const ids = kids.map((k) => k.id);
  const rows = ids.length
    ? await db
        .select()
        .from(events)
        .where(inArray(events.childId, ids))
        .orderBy(desc(events.occurredAt))
        .limit(100)
    : [];
  const nameOf = new Map(kids.map((k) => [k.id, k.name]));

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-extrabold">Activity</h1>
        <p className="text-ink-700 mt-1">Last 100 events across every child device.</p>
      </div>

      {rows.length === 0 ? (
        <Empty title="Quiet so far" hint="Once a paired device reports usage, it shows up here." />
      ) : (
        <div className="card p-6">
          <ul className="divide-y divide-line">
            {rows.map((e) => (
              <li key={e.id} className="py-3 flex items-center justify-between gap-4">
                <div className="min-w-0">
                  <p className="font-medium truncate">{e.label ?? e.target}</p>
                  <p className="text-xs text-ink-500">
                    {nameOf.get(e.childId)} · {fmtDate(e.occurredAt)}
                  </p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <Badge tone="muted">{e.kind}</Badge>
                  {e.blocked ? <Badge tone="rose">blocked</Badge> : <Badge>{fmtMinutes(e.minutes)}</Badge>}
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
