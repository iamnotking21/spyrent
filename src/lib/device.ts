import "server-only";
import { db, children } from "@/db";
import { eq } from "drizzle-orm";

/** Resolve the child device behind an `Authorization: Bearer <deviceToken>` header. */
export async function authDevice(req: Request) {
  const header = req.headers.get("authorization") ?? "";
  const token = header.startsWith("Bearer ") ? header.slice(7).trim() : "";
  if (!token) return null;
  const [c] = await db.select().from(children).where(eq(children.deviceToken, token)).limit(1);
  if (!c || !c.active) return null;
  return c;
}

export function json(data: unknown, status = 200) {
  return Response.json(data, { status });
}

export function fail(message: string, status = 400) {
  return Response.json({ ok: false, error: message }, { status });
}
