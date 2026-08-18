import { requireUser } from "@/lib/guard";
import { statsFor } from "@/lib/stats";
import { addChildAction } from "@/app/actions";
import { ActionForm, Field } from "@/components/forms";
import { Empty, SectionTitle } from "@/components/ui";
import { PushToggle } from "@/components/push-toggle";
import { WeekChart } from "@/components/week-chart";
import { ChildRows, StatRow, TopApps } from "@/components/stats";

export default async function PortalHome() {
  const s = await requireUser();
  const stats = await statsFor(s.uid);

  return (
    <div className="space-y-10">
      <div>
        <h1 className="text-3xl font-extrabold">Hello, {s.name.split(" ")[0]}</h1>
        <p className="text-ink-700 mt-1">
          {stats.childCount === 0
            ? "Add a child to get started."
            : "Here is how things stand across the family."}
        </p>
      </div>

      {stats.childCount > 0 ? <StatRow stats={stats} /> : null}

      {stats.childCount > 0 ? (
        <section className="grid lg:grid-cols-[1.3fr_1fr] gap-6 items-start">
          <div className="card p-6">
            <SectionTitle title="The last seven days" hint="Screen time reported by every device." />
            <WeekChart days={stats.week} />
          </div>

          <div className="card p-6">
            <SectionTitle title="Where the time went" hint="Busiest apps this week." />
            <TopApps apps={stats.topApps} />
          </div>
        </section>
      ) : null}

      <section className="grid lg:grid-cols-[1.4fr_1fr] gap-6 items-start">
        <div className="space-y-4">
          <h2 className="text-lg font-bold">Your children</h2>
          {stats.childCount === 0 ? (
            <Empty
              title="No child devices yet"
              hint="Add a child profile, then pair their device with the code you get."
            />
          ) : (
            <ChildRows rows={stats.perChild} />
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
