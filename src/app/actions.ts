"use server";

import { redirect } from "next/navigation";
import { headers } from "next/headers";
import { revalidatePath } from "next/cache";
import { and, eq } from "drizzle-orm";
import { db, users, children, rules, auditLog } from "@/db";
import { hashPassword, login, destroySession, getSession } from "@/lib/auth";
import { requireUser, requireAdmin } from "@/lib/guard";
import { createParentAccount } from "@/lib/accounts";
import { beginPasswordReset, completePasswordReset } from "@/lib/password-reset";
import { mailConfigured, sendResetEmail } from "@/lib/mail";
import {
  checkLoginAllowed,
  clearLoginFailures,
  clientIp,
  recordLoginFailure,
} from "@/lib/rate-limit";
import { randomToken } from "@/lib/utils";

type State = { error?: string; notice?: string } | undefined;

export async function loginAction(_: State, form: FormData): Promise<State> {
  const username = String(form.get("username") ?? "").trim();
  const password = String(form.get("password") ?? "");
  if (!username || !password) return { error: "Fill in both fields." };

  // key on username *and* address, so one noisy network cannot lock a
  // stranger out of their own account
  const ip = clientIp(await headers());
  const key = `${username.toLowerCase()}|${ip}`;

  const throttle = await checkLoginAllowed(key);
  if (!throttle.allowed) {
    const minutes = Math.ceil(throttle.retryAfterSeconds / 60);
    return { error: `Too many attempts. Try again in ${minutes} minute${minutes === 1 ? "" : "s"}.` };
  }

  const s = await login(username, password);
  if (!s) {
    await recordLoginFailure(key);
    return { error: "Wrong username or password." };
  }

  await clearLoginFailures(key);
  redirect(s.role === "admin" ? "/admin" : "/portal");
}

export async function registerAction(_: State, form: FormData): Promise<State> {
  const get = (k: string) => String(form.get(k) ?? "").trim();
  const password = String(form.get("password") ?? "");

  const result = await createParentAccount({
    username: get("username"),
    email: get("email"),
    firstName: get("firstName"),
    lastName: get("lastName"),
    password,
  });

  if (!result.ok) return { error: result.error };

  await login(get("username"), password);
  redirect("/portal");
}

export async function logoutAction() {
  await destroySession();
  redirect("/");
}

export async function addChildAction(_: State, form: FormData): Promise<State> {
  const s = await requireUser();
  const name = String(form.get("name") ?? "").trim();
  const password = String(form.get("password") ?? "");
  if (!name) return { error: "Give the child profile a name." };
  if (password.length < 4) return { error: "Device PIN needs at least 4 characters." };

  const dupe = await db
    .select({ id: children.id })
    .from(children)
    .where(and(eq(children.parentId, s.uid), eq(children.name, name)));
  if (dupe.length) return { error: "You already have a child with that name." };

  await db.insert(children).values({
    parentId: s.uid,
    name,
    passwordHash: await hashPassword(password),
    deviceToken: randomToken(16),
  });
  revalidatePath("/portal");
  return undefined;
}

async function ownChild(childId: number) {
  const s = await requireUser();
  const [c] = await db.select().from(children).where(eq(children.id, childId)).limit(1);
  if (!c) return null;
  if (s.role !== "admin" && c.parentId !== s.uid) return null;
  return c;
}

export async function removeChildAction(form: FormData) {
  const childId = Number(form.get("childId"));
  const c = await ownChild(childId);
  if (!c) return;
  await db.delete(children).where(eq(children.id, childId));
  revalidatePath("/portal");
}

export async function upsertRuleAction(_: State, form: FormData): Promise<State> {
  const childId = Number(form.get("childId"));
  const kind = String(form.get("kind")) as "app" | "site";
  const target = String(form.get("target") ?? "").trim().toLowerCase();
  const rawMinutes = String(form.get("dailyMinutes") ?? "").trim();
  const dailyMinutes = rawMinutes === "" ? null : Math.max(0, Number(rawMinutes));

  const c = await ownChild(childId);
  if (!c) return { error: "Child not found." };
  if (!target) return { error: kind === "app" ? "Pick an app." : "Type a domain." };
  if (dailyMinutes !== null && Number.isNaN(dailyMinutes)) return { error: "Minutes must be a number." };

  await db
    .insert(rules)
    .values({
      childId,
      kind,
      target,
      label: String(form.get("label") ?? "") || target,
      dailyMinutes,
      blocked: dailyMinutes === null,
    })
    .onConflictDoUpdate({
      target: [rules.childId, rules.kind, rules.target],
      set: { dailyMinutes, blocked: dailyMinutes === null },
    });

  revalidatePath(`/portal/children/${childId}`);
  return undefined;
}

export async function deleteRuleAction(form: FormData) {
  const ruleId = Number(form.get("ruleId"));
  const [r] = await db.select().from(rules).where(eq(rules.id, ruleId)).limit(1);
  if (!r) return;
  const c = await ownChild(r.childId);
  if (!c) return;
  await db.delete(rules).where(eq(rules.id, ruleId));
  revalidatePath(`/portal/children/${r.childId}`);
}

export async function rotateTokenAction(form: FormData) {
  const childId = Number(form.get("childId"));
  const c = await ownChild(childId);
  if (!c) return;
  await db.update(children).set({ deviceToken: randomToken(16) }).where(eq(children.id, childId));
  revalidatePath(`/portal/children/${childId}`);
}

export async function setUserActiveAction(form: FormData) {
  const admin = await requireAdmin();
  const userId = Number(form.get("userId"));
  const active = String(form.get("active")) === "true";
  await db.update(users).set({ active }).where(eq(users.id, userId));
  await db.insert(auditLog).values({
    actorId: admin.uid,
    action: active ? "user.enable" : "user.disable",
    detail: `user #${userId}`,
  });
  revalidatePath("/admin/users");
}

export async function currentSession() {
  return getSession();
}


export async function requestResetAction(_: State, form: FormData): Promise<State> {
  const email = String(form.get("email") ?? "").trim();
  if (!email.includes("@")) return { error: "Type the email you signed up with." };

  // throttled on the same counter as sign-in, so this cannot be used to
  // hammer the mail service or to probe for registered addresses
  const key = `reset:${email.toLowerCase()}|${clientIp(await headers())}`;
  const throttle = await checkLoginAllowed(key);
  if (!throttle.allowed) {
    return { error: "Too many requests. Try again a little later." };
  }
  await recordLoginFailure(key);

  const token = await beginPasswordReset(email);
  if (token) {
    const base = process.env.NEXT_PUBLIC_APP_URL ?? "";
    await sendResetEmail(email, `${base}/reset/${token}`);
  }

  // the same answer whether or not the account exists
  return {
    notice: mailConfigured
      ? "If that address has an account, a reset link is on its way."
      : "Ask whoever administers your Spyrent to send you a reset link.",
  };
}

export async function resetPasswordAction(_: State, form: FormData): Promise<State> {
  const token = String(form.get("token") ?? "");
  const password = String(form.get("password") ?? "");
  const confirm = String(form.get("confirm") ?? "");

  if (password !== confirm) return { error: "Those two passwords do not match." };

  const result = await completePasswordReset(token, password);
  if (!result.ok) return { error: result.error };

  redirect("/login?reset=1");
}

/** Admin hands a parent a reset link when there is no outbound mail. */
export async function issueResetLinkAction(_: State, form: FormData): Promise<State> {
  const admin = await requireAdmin();
  const email = String(form.get("email") ?? "");

  const token = await beginPasswordReset(email);
  if (!token) return { error: "No active account with that email." };

  await db.insert(auditLog).values({
    actorId: admin.uid,
    action: "user.reset_link",
    detail: email,
  });

  // shown once and never stored, so it cannot leak from the table later
  return { notice: `${process.env.NEXT_PUBLIC_APP_URL ?? ""}/reset/${token}` };
}
