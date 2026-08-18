import { desc, eq, sql } from "drizzle-orm";
import { db, users, children } from "@/db";
import { setUserActiveAction } from "@/app/actions";
import { Badge } from "@/components/ui";
import { ResetLinkButton } from "@/components/reset-link";
import { fmtDate } from "@/lib/utils";

export default async function AdminUsers() {
  const rows = await db
    .select({
      id: users.id,
      username: users.username,
      email: users.email,
      firstName: users.firstName,
      lastName: users.lastName,
      role: users.role,
      active: users.active,
      createdAt: users.createdAt,
      kids: sql<number>`(select count(*)::int from ${children} where ${children.parentId} = ${users.id})`,
    })
    .from(users)
    .orderBy(desc(users.createdAt));

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-extrabold">Accounts</h1>
        <p className="text-ink-700 mt-1">Disable an account to lock it out immediately.</p>
      </div>

      <div className="card p-0 overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-ink-500 border-b border-line">
            <tr>
              <th className="px-5 py-3 font-semibold">Name</th>
              <th className="px-5 py-3 font-semibold">Username</th>
              <th className="px-5 py-3 font-semibold">Email</th>
              <th className="px-5 py-3 font-semibold">Children</th>
              <th className="px-5 py-3 font-semibold">Joined</th>
              <th className="px-5 py-3 font-semibold">Status</th>
              <th className="px-5 py-3" />
            </tr>
          </thead>
          <tbody className="divide-y divide-line">
            {rows.map((u) => (
              <tr key={u.id}>
                <td className="px-5 py-3 font-medium">
                  {u.firstName} {u.lastName}
                </td>
                <td className="px-5 py-3 text-ink-700">{u.username}</td>
                <td className="px-5 py-3 text-ink-700">{u.email}</td>
                <td className="px-5 py-3">{u.kids}</td>
                <td className="px-5 py-3 text-ink-500">{fmtDate(u.createdAt)}</td>
                <td className="px-5 py-3">
                  {u.role === "admin" ? (
                    <Badge tone="amber">admin</Badge>
                  ) : u.active ? (
                    <Badge>active</Badge>
                  ) : (
                    <Badge tone="rose">disabled</Badge>
                  )}
                </td>
                <td className="px-5 py-3 text-right">
                  <div className="flex items-center justify-end gap-4">
                    <ResetLinkButton email={u.email} />
                    <form action={setUserActiveAction}>
                      <input type="hidden" name="userId" value={u.id} />
                      <input type="hidden" name="active" value={String(!u.active)} />
                      <button className="text-ink-500 hover:text-brand-700 font-medium">
                        {u.active ? "Disable" : "Enable"}
                      </button>
                    </form>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
