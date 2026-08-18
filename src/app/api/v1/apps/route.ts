import { db, apps, rules } from "@/db";
import { authDevice, fail, json } from "@/lib/device";
import { eq } from "drizzle-orm";

type Incoming = { packageName: string; label?: string; sizeBytes?: number };

/** Device pushes its installed app inventory. Full replace-by-upsert. */
export async function POST(req: Request) {
  const c = await authDevice(req);
  if (!c) return fail("unauthorized", 401);

  const body = await req.json().catch(() => null);
  const list: Incoming[] = Array.isArray(body?.apps) ? body.apps : [];
  if (!list.length) return fail("apps array is required");

  const values = list
    .filter((a) => a?.packageName)
    .map((a) => ({
      childId: c.id,
      packageName: String(a.packageName),
      label: String(a.label ?? a.packageName),
      sizeBytes: Number.isFinite(a.sizeBytes) ? Number(a.sizeBytes) : null,
      seenAt: new Date(),
    }));

  await db
    .insert(apps)
    .values(values)
    .onConflictDoUpdate({
      target: [apps.childId, apps.packageName],
      set: { seenAt: new Date() },
    });

  return json({ ok: true, stored: values.length });
}

/** Device asks which apps the server already knows about. */
export async function GET(req: Request) {
  const c = await authDevice(req);
  if (!c) return fail("unauthorized", 401);
  const rows = await db.select().from(apps).where(eq(apps.childId, c.id));
  const ruleRows = await db.select().from(rules).where(eq(rules.childId, c.id));
  return json({ ok: true, apps: rows, rules: ruleRows });
}
