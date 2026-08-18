"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { and, eq } from "drizzle-orm";
import { db, users, children, rules, auditLog } from "@/db";
import { hashPassword, login, destroySession, getSession } from "@/lib/auth";
import { requireUser, requireAdmin } from "@/lib/guard";
import { randomToken } from "@/lib/utils";

type State = { error?: string } | undefined;

export async function loginAction(_: State, form: FormData): Promise<State> {
  const username = String(form.get("username") ?? "").trim();
  const password = String(form.get("password") ?? "");
  if (!username || !password) return { error: "Fill in both fields." };

  const s = await login(username, password);
  if (!s) return { error: "Wrong username or password." };
  redirect(s.role === "admin" ? "/admin" : "/portal");
}

export async function registerAction(_: State, form: FormData): Promise<State> {
  const get = (k: string) => String(form.get(k) ?? "").trim();
  const username = get("username");
  const email = get("email").toLowerCase();
  const firstName = get("firstName");
  const lastName = get("lastName");
  const password = String(form.get("password") ?? "");

  if (!username || !email || !firstName || !lastName) return { error: "All fields are required." };
  if (password.length < 8) return { error: "Password needs at least 8 characters." };

  const clash = await db.select({ id: users.id }).from(users).where(eq(users.username, username));
  if (clash.length) return { error: "That username is taken." };
  const clashMail = await db.select({ id: users.id }).from(users).where(eq(users.email, email));
  if (clashMail.length) return { error: "That email is already registered." };

  await db.insert(users).values({
    username,
    email,
    firstName,
    lastName,
    passwordHash: await hashPassword(password),
    role: "parent",
  });

  await login(username, password);
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

