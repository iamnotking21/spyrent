import Link from "next/link";
import { and, desc, eq, gte, inArray, sql } from "drizzle-orm";
import { db, children, rules, events } from "@/db";
import { requireUser } from "@/lib/guard";
import { addChildAction } from "@/app/actions";
import { ActionForm, Field } from "@/components/forms";
import { Badge, Empty, Stat } from "@/components/ui";
import { PushToggle } from "@/components/push-toggle";
import { fmtDate, fmtMinutes } from "@/lib/utils";

export default async function PortalHome() {
  const s = await requireUser();
  const kids = await db
    .select()
    .from(children)
    .where(eq(children.parentId, s.uid))
    .orderBy(desc(children.createdAt));

  const since = new Date(Date.now() - 24 * 60 * 60 * 1000);
  const ids = kids.map((k) => k.id);

  const todayMinutes = ids.length
    ? (
        await db
          .select({ total: sql<number>`coalesce(sum(${events.minutes}), 0)::int` })
          .from(events)
          .where(and(gte(events.occurredAt, since), inArray(events.childId, ids)))
      )[0]?.total ?? 0
    : 0;

  const ruleCount = ids.length
    ? (
        await db
          .select({ n: sql<number>`count(*)::int` })
          .from(rules)
          .where(inArray(rules.childId, ids))
      )[0]?.n ?? 0
    : 0;

  return (
    <div className="space-y-10">
      <div>
        <h1 className="text-3xl font-extrabold">Hello, {s.name.split(" ")[0]}</h1>
        <p className="text-ink-700 mt-1">Here is how the last 24 hours went.</p>
      </div>

      <div className="grid sm:grid-cols-3 gap-5">
        <Stat label="Children" value={String(kids.length)} />
        <Stat label="Screen time today" value={fmtMinutes(todayMinutes)} tone="amber" />
        <Stat label="Active rules" value={String(ruleCount)} />
      </div>

      <section className="grid lg:grid-cols-[1.4fr_1fr] gap-6 items-start">
        <div className="space-y-4">
          <h2 className="text-lg font-bold">Your children</h2>
          {kids.length === 0 ? (
            <Empty
              title="No child devices yet"
              hint="Add a child profile, then pair their device with the code you get."
            />
          ) : (
            <div className="grid sm:grid-cols-2 gap-4">
              {kids.map((k) => (
                <Link key={k.id} href={`/portal/children/${k.id}`} className="card p-5 block hover:border-brand-500 transition-colors">
                  <div className="flex items-start justify-between">
                    <h3 className="text-lg font-bold">{k.name}</h3>
                    <Badge tone={k.lastSeenAt ? "brand" : "muted"}>
                      {k.lastSeenAt ? "paired" : "waiting"}
                    </Badge>
                  </div>
                  <p className="text-sm text-ink-500 mt-2">Last seen {fmtDate(k.lastSeenAt)}</p>
                  <p className="text-sm text-brand-700 font-semibold mt-4">Open dashboard →</p>
                </Link>
              ))}
            </div>
          )}
        </div>

        <div className="space-y-6">
        <PushToggle vapidKey={process.env.NEXT_PUBLIC_VAPID_PUBLIC_KEY ?? ""} />

        <div className="card p-6">
          <h2 className="text-lg font-bold">Add a child</h2>
          <p className="text-sm text-ink-500 mt-1 mb-5">
            The PIN is what they type on their device to unlock settings.
          </p>
          <ActionForm action={addChildAction} submitLabel="Add child">
            <Field label="Name" name="name" placeholder="Mia" />
            <Field label="Device PIN" name="password" type="password" placeholder="4+ characters" />
          </ActionForm>
        </div>
        </div>
      </section>
    </div>
  );
}
