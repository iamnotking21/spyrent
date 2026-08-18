import { and, eq, lt, sql } from "drizzle-orm";
import { db, loginAttempts } from "@/db";

/** How many failures are tolerated before the identifier is locked out. */
const MAX_ATTEMPTS = 8;
/** Failures older than this are forgiven. */
const WINDOW_MINUTES = 15;
/** How long a locked identifier stays locked. */
const LOCKOUT_MINUTES = 15;

export type Throttle = { allowed: true } | { allowed: false; retryAfterSeconds: number };

/**
 * Ask whether this identifier may attempt a sign-in.
 * The identifier should combine username and client IP so that one noisy
 * network cannot lock a victim out of their own account on its own.
 */
export async function checkLoginAllowed(identifier: string): Promise<Throttle> {
  const [row] = await db
    .select()
    .from(loginAttempts)
    .where(eq(loginAttempts.identifier, identifier))
    .limit(1);

  if (!row) return { allowed: true };

  const now = Date.now();

  if (row.lockedUntil && row.lockedUntil.getTime() > now) {
    return {
      allowed: false,
      retryAfterSeconds: Math.ceil((row.lockedUntil.getTime() - now) / 1000),
    };
  }

  return { allowed: true };
}

/** Count a failed attempt, locking the identifier once it runs out of rope. */
export async function recordLoginFailure(identifier: string): Promise<void> {
  const now = new Date();
  const windowFloor = new Date(now.getTime() - WINDOW_MINUTES * 60_000);

  // a stale window is a fresh start
  await db
    .delete(loginAttempts)
    .where(
      and(eq(loginAttempts.identifier, identifier), lt(loginAttempts.windowStart, windowFloor)),
    );

  await db
    .insert(loginAttempts)
    .values({ identifier, attempts: 1, windowStart: now })
    .onConflictDoUpdate({
      target: loginAttempts.identifier,
      set: {
        attempts: sql`${loginAttempts.attempts} + 1`,
        lockedUntil: sql`
          case when ${loginAttempts.attempts} + 1 >= ${MAX_ATTEMPTS}
          then now() + interval '${sql.raw(String(LOCKOUT_MINUTES))} minutes'
          else null end`,
      },
    });
}

/** A good password wipes the slate. */
export async function clearLoginFailures(identifier: string): Promise<void> {
  await db.delete(loginAttempts).where(eq(loginAttempts.identifier, identifier));
}

/** Best-effort client address behind Vercel's proxy. */
export function clientIp(headers: Headers): string {
  const forwarded = headers.get("x-forwarded-for");
  if (forwarded) return forwarded.split(",")[0]!.trim();
  return headers.get("x-real-ip") ?? "unknown";
}
