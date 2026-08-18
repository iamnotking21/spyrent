import { eq } from "drizzle-orm";
import { db, users } from "../db/index";
import bcrypt from "bcryptjs";

export type NewParent = {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
};

export type CreateResult =
  | { ok: true; id: number }
  | { ok: false; error: string };

/**
 * Validate and insert a parent account. Kept out of the server action so it
 * can be exercised directly by tests.
 */
export async function createParentAccount(input: NewParent): Promise<CreateResult> {
  const username = input.username.trim();
  const email = input.email.trim().toLowerCase();
  const firstName = input.firstName.trim();
  const lastName = input.lastName.trim();

  if (!username || !email || !firstName || !lastName) {
    return { ok: false, error: "All fields are required." };
  }
  if (!email.includes("@")) {
    return { ok: false, error: "That email looks wrong." };
  }
  if (input.password.length < 8) {
    return { ok: false, error: "Password needs at least 8 characters." };
  }

  const byName = await db.select({ id: users.id }).from(users).where(eq(users.username, username));
  if (byName.length) return { ok: false, error: "That username is taken." };

  const byMail = await db.select({ id: users.id }).from(users).where(eq(users.email, email));
  if (byMail.length) return { ok: false, error: "That email is already registered." };

  const [row] = await db
    .insert(users)
    .values({
      username,
      email,
      firstName,
      lastName,
      passwordHash: await bcrypt.hash(input.password, 10),
      role: "parent",
    })
    .returning({ id: users.id });

  return { ok: true, id: row.id };
}
