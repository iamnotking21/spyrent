import Link from "next/link";
import { desc, eq, sql } from "drizzle-orm";
import { db, children, users, rules, timeRequests } from "@/db";
import { Badge, Empty } from "@/components/ui";
import { fmtDate } from "@/lib/utils";

export const metadata = { title: "Children · Spyrent admin" };

export default async function AdminChildren() {
  const rows = await db
    .select({
      id: children.id,
      name: children.name,
      timezone: children.timezone,
      deviceModel: children.deviceModel,
      lastSeenAt: children.lastSeenAt,
      active: children.active,
      parent: sql<string>`${users.firstName} || ' ' || ${users.lastName}`,
      parentEmail: users.email,
      ruleCount: sql<number>`(select count(*)::int from ${rules} where ${rules.childId} = ${children.id})`,
      pending: sql<number>`(
        select count(*)::int from ${timeRequests}
        where ${timeRequests.childId} = ${children.id} and ${timeRequests.status} = 'pending'
      )`,
    })
    .from(children)
    .innerJoin(users, eq(users.id, children.parentId))
    .orderBy(desc(children.lastSeenAt));

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-extrabold">Children</h1>
        <p className="text-ink-700 mt-1">
          Every paired device on the platform. Open one to see or change its rules.
        </p>
      </div>

      {rows.length === 0 ? (
        <Empty title="No children yet" hint="They appear here once a parent adds one." />
      ) : (
        <div className="card p-0 overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="text-left text-ink-500 border-b border-line">
              <tr>
                <th className="px-5 py-3 font-semibold">Child</th>
                <th className="px-5 py-3 font-semibold">Parent</th>
                <th className="px-5 py-3 font-semibold">Device</th>
                <th className="px-5 py-3 font-semibold">Zone</th>
                <th className="px-5 py-3 font-semibold">Rules</th>
                <th className="px-5 py-3 font-semibold">Last seen</th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-line">
              {rows.map((c) => (
                <tr key={c.id}>
                  <td className="px-5 py-3 font-medium">
                    {c.name}
                    {c.pending > 0 ? (
                      <span className="ml-2">
                        <Badge tone="amber">{c.pending} waiting</Badge>
                      </span>
                    ) : null}
                  </td>
                  <td className="px-5 py-3 text-ink-700">
                    {c.parent}
                    <span className="block text-xs text-ink-500">{c.parentEmail}</span>
                  </td>
                  <td className="px-5 py-3 text-ink-700">
                    {c.deviceModel ?? <span className="text-ink-500">not paired</span>}
                  </td>
                  <td className="px-5 py-3 text-ink-500">{c.timezone}</td>
                  <td className="px-5 py-3">{c.ruleCount}</td>
                  <td className="px-5 py-3 text-ink-500">{fmtDate(c.lastSeenAt)}</td>
                  <td className="px-5 py-3 text-right">
                    <Link
                      href={`/portal/children/${c.id}`}
                      className="text-ink-500 hover:text-brand-700 font-medium"
                    >
                      Open
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
