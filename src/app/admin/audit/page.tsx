import { desc, eq } from "drizzle-orm";
import { db, auditLog, users, children } from "@/db";
import { Empty } from "@/components/ui";
import { describe } from "@/lib/audit";
import { fmtDate } from "@/lib/utils";

export const metadata = { title: "Audit · Spyrent admin" };

export default async function AuditPage() {
  const rows = await db
    .select({
      id: auditLog.id,
      action: auditLog.action,
      detail: auditLog.detail,
      createdAt: auditLog.createdAt,
      actorFirst: users.firstName,
      actorLast: users.lastName,
      childName: children.name,
    })
    .from(auditLog)
    .leftJoin(users, eq(users.id, auditLog.actorId))
    .leftJoin(children, eq(children.id, auditLog.childId))
    .orderBy(desc(auditLog.createdAt))
    .limit(200);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-extrabold">Audit log</h1>
        <p className="text-ink-700 mt-1">
          Who changed what, newest first. Kept for every account on the platform.
        </p>
      </div>

      {rows.length === 0 ? (
        <Empty title="Nothing logged yet" hint="Changes to rules, children and accounts land here." />
      ) : (
        <div className="card p-6">
          <ul className="divide-y divide-line">
            {rows.map((a) => (
              <li key={a.id} className="py-3 flex flex-wrap justify-between gap-2">
                <div className="min-w-0">
                  <p className="font-medium">{describe(a.action, a.detail)}</p>
                  <p className="text-xs text-ink-500">
                    {a.actorFirst ? `${a.actorFirst} ${a.actorLast}` : "a deleted account"}
                    {a.childName ? ` · ${a.childName}` : ""}
                  </p>
                </div>
                <span className="text-xs text-ink-500 shrink-0">{fmtDate(a.createdAt)}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
