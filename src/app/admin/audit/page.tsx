import { desc } from "drizzle-orm";
import { db, auditLog } from "@/db";
import { Empty } from "@/components/ui";
import { fmtDate } from "@/lib/utils";

export default async function AuditPage() {
  const rows = await db.select().from(auditLog).orderBy(desc(auditLog.createdAt)).limit(100);

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-extrabold">Audit log</h1>
      {rows.length === 0 ? (
        <Empty title="Nothing logged yet" hint="Admin actions such as disabling an account land here." />
      ) : (
        <div className="card p-6">
          <ul className="divide-y divide-line">
            {rows.map((a) => (
              <li key={a.id} className="py-3 flex justify-between gap-4">
                <div>
                  <p className="font-medium">{a.action}</p>
                  <p className="text-xs text-ink-500">{a.detail}</p>
                </div>
                <span className="text-xs text-ink-500">{fmtDate(a.createdAt)}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
