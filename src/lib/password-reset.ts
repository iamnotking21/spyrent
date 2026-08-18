import { and, eq, gt, isNull } from "drizzle-orm";
import { createHash, randomBytes } from "node:crypto";
import bcrypt from "bcryptjs";
import { db, users, passwordResets } from "../db/index";

const TOKEN_TTL_MINUTES = 60;

/** Tokens are compared by hash, never stored in the clear. */
function digest(token: string) {
  return createHash("sha256").update(token).digest("hex");
}

/**
 * Start a reset. Returns the token when the account exists, and null when it
 * does not — the caller must show the same message either way, or this becomes
 * a way to discover which emails are registered.
 */
export async function beginPasswordReset(email: string): Promise<string | null> {
  const [user] = await db
    .select()
    .from(users)
    .where(eq(users.email, email.trim().toLowerCase()))
    .limit(1);

  if (!user || !user.active) return null;

  const token = randomBytes(32).toString("hex");

  await db.insert(passwordResets).values({
    userId: user.id,
    tokenHash: digest(token),
    expiresAt: new Date(Date.now() + TOKEN_TTL_MINUTES * 60_000),
  });

  return token;
}

export type ResetOutcome =
  | { ok: true }
  | { ok: false; error: string };

/** Spend a token and set the new password. */
export async function completePasswordReset(
  token: string,
  newPassword: string,
): Promise<ResetOutcome> {
  if (newPassword.length < 8) return { ok: false, error: "Password needs at least 8 characters." };

  const [row] = await db
    .select()
    .from(passwordResets)
    .where(
      and(
        eq(passwordResets.tokenHash, digest(token)),
        isNull(passwordResets.usedAt),
        gt(passwordResets.expiresAt, new Date()),
      ),
    )
    .limit(1);

  if (!row) return { ok: false, error: "That link has expired or was already used." };

  await db
    .update(users)
    .set({ passwordHash: await bcrypt.hash(newPassword, 10) })
    .where(eq(users.id, row.userId));

  await db
    .update(passwordResets)
    .set({ usedAt: new Date() })
    .where(eq(passwordResets.id, row.id));

  return { ok: true };
}
