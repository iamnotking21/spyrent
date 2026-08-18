import { sql } from "drizzle-orm";
import { db, users, children, events, rules } from "@/db";
import { Stat } from "@/components/ui";

export default async function AdminHome() {
  const [[u], [c], [r], [e]] = await Promise.all([
    db.select({ n: sql<number>`count(*)::int` }).from(users),
    db.select({ n: sql<number>`count(*)::int` }).from(children),
    db.select({ n: sql<number>`count(*)::int` }).from(rules),
    db.select({ n: sql<number>`count(*)::int` }).from(events),
  ]);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-extrabold">Platform overview</h1>
        <p className="text-ink-700 mt-1">Live counts straight from Postgres.</p>
      </div>
      <div className="grid sm:grid-cols-4 gap-5">
        <Stat label="Accounts" value={String(u?.n ?? 0)} />
        <Stat label="Children" value={String(c?.n ?? 0)} />
        <Stat label="Rules" value={String(r?.n ?? 0)} tone="amber" />
        <Stat label="Events" value={String(e?.n ?? 0)} />
      </div>
    </div>
  );
}
