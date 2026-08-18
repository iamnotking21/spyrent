import { eq } from "drizzle-orm";
import { db, children } from "@/db";
import { fail, json } from "@/lib/device";

/** Child device exchanges the pairing token for its profile. */
export async function POST(req: Request) {
  const body = await req.json().catch(() => null);
  const token = String(body?.token ?? "").trim();
  const deviceModel = body?.deviceModel ? String(body.deviceModel) : null;
  if (!token) return fail("token is required");

  const [c] = await db.select().from(children).where(eq(children.deviceToken, token)).limit(1);
  if (!c || !c.active) return fail("unknown or disabled token", 401);

  await db
    .update(children)
    .set({ deviceModel, lastSeenAt: new Date() })
    .where(eq(children.id, c.id));

  return json({ ok: true, child: { id: c.id, name: c.name } });
}
