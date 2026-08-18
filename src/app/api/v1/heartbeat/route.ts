import { eq } from "drizzle-orm";
import { db, children } from "@/db";
import { authDevice, fail, json } from "@/lib/device";

export async function POST(req: Request) {
  const c = await authDevice(req);
  if (!c) return fail("unauthorized", 401);
  await db.update(children).set({ lastSeenAt: new Date() }).where(eq(children.id, c.id));
  return json({ ok: true, serverTime: new Date().toISOString() });
}
