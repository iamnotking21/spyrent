import "server-only";
import { redirect } from "next/navigation";
import { getSession, type Session } from "./auth";

export async function requireUser(): Promise<Session> {
  const s = await getSession();
  if (!s) redirect("/login");
  return s;
}

export async function requireAdmin(): Promise<Session> {
  const s = await requireUser();
  if (s.role !== "admin") redirect("/portal");
  return s;
}
