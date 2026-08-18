import { NextResponse, type NextRequest } from "next/server";
import { jwtVerify } from "jose";
import { neon } from "@neondatabase/serverless";

import { AUTH_SECRET, DATABASE_URL } from "@/lib/env";

const secret = new TextEncoder().encode(AUTH_SECRET);

/**
 * Gate protected routes before anything renders.
 *
 * Two jobs a page-level guard cannot do:
 *  - redirect() in a layout fires after the tree has rendered, so the 307 body
 *    still carries the rendered HTML.
 *  - a signed cookie outlives an account being disabled, so the account's
 *    current state has to be read per request, not trusted from the token.
 */
export async function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;

  const bounce = (to: string) => {
    const url = req.nextUrl.clone();
    url.pathname = to;
    url.search = "";
    const res = NextResponse.redirect(url);
    if (to === "/login") res.cookies.delete("spyrent_session");
    return res;
  };

  const token = req.cookies.get("spyrent_session")?.value;
  if (!token) return bounce("/login");

  let uid: number | null = null;
  let role: string | null = null;
  try {
    const { payload } = await jwtVerify(token, secret);
    uid = typeof payload.uid === "number" ? payload.uid : null;
    role = typeof payload.role === "string" ? payload.role : null;
  } catch {
    return bounce("/login");
  }
  if (!uid || !role) return bounce("/login");

  const sql = neon(DATABASE_URL);
  const rows = await sql`select active, role from users where id = ${uid} limit 1`;
  const account = rows[0] as { active: boolean; role: string } | undefined;

  if (!account || !account.active) return bounce("/login");
  if (pathname.startsWith("/admin") && account.role !== "admin") return bounce("/portal");

  return NextResponse.next();
}

export const config = {
  matcher: ["/portal/:path*", "/admin/:path*"],
};
