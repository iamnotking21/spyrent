import "server-only";
import { db, children } from "@/db";
import { eq } from "drizzle-orm";

/** The raw device token from an `Authorization: Bearer <token>` header. */
export function bearerToken(req: Request): string | null {
  const header = req.headers.get("authorization") ?? "";
  const token = header.startsWith("Bearer ") ? header.slice(7).trim() : "";
  return token || null;
}

/**
 * Resolve the child device behind the bearer token.
 *
 * Costs a round trip, so the hot endpoints (policy, events) skip this and fold
 * the token lookup into their own statement instead. Use it where a route
 * genuinely needs the child row before it can do anything else.
 */
export async function authDevice(req: Request) {
  const token = bearerToken(req);
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
