import { and, eq, notInArray, sql } from "drizzle-orm";
import { db, apps, rules } from "@/db";
import { authDevice, fail, json } from "@/lib/device";

type Incoming = {
  packageName: string;
  label?: string;
  sizeBytes?: number;
  icon?: string;
};

/**
 * Device pushes its installed app inventory.
 *
 * Icons are only sent for apps the server says it is missing — the response
 * carries that list — so a routine sync stays a few kilobytes rather than
 * re-uploading every icon on the device every fifteen minutes.
 */
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
      icon: typeof a.icon === "string" && a.icon.startsWith("data:image/") ? a.icon : null,
      seenAt: new Date(),
    }));

  await db
    .insert(apps)
    .values(values)
    .onConflictDoUpdate({
      target: [apps.childId, apps.packageName],
      set: {
        label: sql`excluded.label`,
        seenAt: new Date(),
        // a sync without icons must not wipe the ones already stored
        icon: sql`coalesce(excluded.icon, ${apps.icon})`,
      },
    });

  // an uninstalled app should stop cluttering the parent's list, but only when
  // the device says this was the full inventory rather than an icon top-up
  if (body?.complete === true) {
    const seen = values.map((v) => v.packageName);
    await db.delete(apps).where(and(eq(apps.childId, c.id), notInArray(apps.packageName, seen)));
  }

  const stored = await db
    .select({ packageName: apps.packageName, icon: apps.icon })
    .from(apps)
    .where(eq(apps.childId, c.id));

  return json({
    ok: true,
    stored: values.length,
    needIcons: stored.filter((a) => !a.icon).map((a) => a.packageName),
  });
}

/** Device asks which apps the server already knows about. */
export async function GET(req: Request) {
  const c = await authDevice(req);
  if (!c) return fail("unauthorized", 401);

  const rows = await db
    .select({
      packageName: apps.packageName,
      label: apps.label,
      hasIcon: sql<boolean>`${apps.icon} is not null`,
    })
    .from(apps)
    .where(eq(apps.childId, c.id));

  const ruleRows = await db.select().from(rules).where(eq(rules.childId, c.id));

  return json({ ok: true, apps: rows, rules: ruleRows });
}
