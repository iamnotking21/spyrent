import { eq } from "drizzle-orm";
import { db, rules } from "@/db";
import { authDevice, fail, json } from "@/lib/device";

/** Full rule set the child device must enforce. */
export async function GET(req: Request) {
  const c = await authDevice(req);
  if (!c) return fail("unauthorized", 401);

  const rows = await db.select().from(rules).where(eq(rules.childId, c.id));

  return json({
    ok: true,
    child: { id: c.id, name: c.name },
    apps: rows
      .filter((r) => r.kind === "app")
      .map((r) => ({
        packageName: r.target,
        label: r.label ?? r.target,
        dailyMinutes: r.dailyMinutes,
        usedMinutes: r.usedMinutes,
        blocked: r.blocked,
      })),
    sites: rows
      .filter((r) => r.kind === "site")
      .map((r) => ({
        domain: r.target,
        label: r.label ?? r.target,
        dailyMinutes: r.dailyMinutes,
        blocked: r.blocked,
      })),
  });
}
