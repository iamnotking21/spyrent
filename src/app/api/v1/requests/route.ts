import { and, desc, eq } from "drizzle-orm";
import { db, rules, timeRequests } from "@/db";
import { authDevice, fail, json } from "@/lib/device";

/** The device asks for more time on one app. */
export async function POST(req: Request) {
  const c = await authDevice(req);
  if (!c) return fail("unauthorized", 401);

  const body = await req.json().catch(() => null);
  const target = String(body?.target ?? "").trim().toLowerCase();
  const minutes = Math.min(120, Math.max(5, Math.round(Number(body?.minutes ?? 15))));
  if (!target) return fail("target is required");

  const [rule] = await db
    .select()
    .from(rules)
    .where(and(eq(rules.childId, c.id), eq(rules.kind, "app"), eq(rules.target, target)))
    .limit(1);

  // one open request per app, so a child cannot bury the parent in taps
  const [open] = await db
    .select()
    .from(timeRequests)
    .where(
      and(
        eq(timeRequests.childId, c.id),
        eq(timeRequests.target, target),
        eq(timeRequests.status, "pending"),
      ),
    )
    .limit(1);

  if (open) return json({ ok: true, status: "pending", requestId: open.id, alreadyOpen: true });

  const [created] = await db
    .insert(timeRequests)
    .values({
      childId: c.id,
      ruleId: rule?.id ?? null,
      target,
      label: body?.label ? String(body.label) : (rule?.label ?? target),
      minutes,
    })
    .returning();

  return json({ ok: true, status: "pending", requestId: created.id });
}

/** The device polls for answers to what it asked. */
export async function GET(req: Request) {
  const c = await authDevice(req);
  if (!c) return fail("unauthorized", 401);

  const rows = await db
    .select()
    .from(timeRequests)
    .where(eq(timeRequests.childId, c.id))
    .orderBy(desc(timeRequests.createdAt))
    .limit(20);

  return json({
    ok: true,
    requests: rows.map((r) => ({
      id: r.id,
      target: r.target,
      label: r.label,
      minutes: r.minutes,
      status: r.status,
    })),
  });
}
