import { and, eq, sql } from "drizzle-orm";
import { db, events, rules } from "@/db";
import { authDevice, fail, json } from "@/lib/device";

type Incoming = {
  kind: "app" | "site";
  target: string;
  label?: string;
  minutes?: number;
  blocked?: boolean;
  occurredAt?: string;
};

/** Device reports usage. Minutes also tick the matching rule budget. */
export async function POST(req: Request) {
  const c = await authDevice(req);
  if (!c) return fail("unauthorized", 401);

  const body = await req.json().catch(() => null);
  const list: Incoming[] = Array.isArray(body?.events) ? body.events : body ? [body] : [];
  const clean = list.filter((e) => e?.target && (e.kind === "app" || e.kind === "site"));
  if (!clean.length) return fail("no valid events");

  await db.insert(events).values(
    clean.map((e) => ({
      childId: c.id,
      kind: e.kind,
      target: String(e.target).toLowerCase(),
      label: e.label ? String(e.label) : null,
      minutes: Math.max(0, Math.round(Number(e.minutes ?? 0))),
      blocked: Boolean(e.blocked),
      occurredAt: e.occurredAt ? new Date(e.occurredAt) : new Date(),
    })),
  );

  for (const e of clean) {
    const mins = Math.max(0, Math.round(Number(e.minutes ?? 0)));
    if (!mins) continue;
    await db
      .update(rules)
      .set({ usedMinutes: sql`${rules.usedMinutes} + ${mins}` })
      .where(
        and(
          eq(rules.childId, c.id),
          eq(rules.kind, e.kind),
          eq(rules.target, String(e.target).toLowerCase()),
        ),
      );
  }

  return json({ ok: true, stored: clean.length });
}
