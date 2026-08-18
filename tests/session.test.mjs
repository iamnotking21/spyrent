/**
 * Session revocation checks against a running server.
 *   node tests/session.test.mjs [baseUrl]
 */
import "dotenv/config";
import assert from "node:assert/strict";
import { neon } from "@neondatabase/serverless";
import { SignJWT } from "jose";

const base = (process.argv[2] ?? "http://localhost:3000").replace(/\/$/, "");
const sql = neon(process.env.DATABASE_URL);
const secret = new TextEncoder().encode(process.env.AUTH_SECRET);
const stamp = Date.now();

let failures = 0;
async function check(name, fn) {
  try {
    await fn();
    console.log(`  ok   ${name}`);
  } catch (err) {
    failures++;
    console.log(`  FAIL ${name}\n       ${err.message}`);
  }
}

const cookie = async (uid, role) =>
  `spyrent_session=${await new SignJWT({ uid, role, name: "Probe User" })
    .setProtectedHeader({ alg: "HS256" })
    .setIssuedAt()
    .setExpirationTime("1h")
    .sign(secret)}`;

const status = async (path, c) =>
  (await fetch(`${base}${path}`, { headers: c ? { cookie: c } : {}, redirect: "manual" })).status;

async function run() {
  console.log("session handling");
  const [u] = await sql`
    insert into users (username, email, first_name, last_name, password_hash, role)
    values (${`sess_${stamp}`}, ${`sess_${stamp}@example.com`}, 'Probe', 'User', 'x', 'parent')
    returning id`;
  const c = await cookie(u.id, "parent");

  await check("a valid session reaches the portal", async () => {
    assert.equal(await status("/portal", c), 200);
  });

  await check("disabling the account ends the session immediately", async () => {
    await sql`update users set active = false where id = ${u.id}`;
    assert.equal(await status("/portal", c), 307);
    await sql`update users set active = true where id = ${u.id}`;
  });

  await check("a token claiming admin does not grant admin", async () => {
    const forged = await cookie(u.id, "admin");
    assert.equal(await status("/admin", forged), 307);
  });

  await check("a deleted account cannot use its old cookie", async () => {
    await sql`delete from users where id = ${u.id}`;
    assert.equal(await status("/portal", c), 307);
  });

  await check("a cookie signed with the wrong key is rejected", async () => {
    const wrong = new TextEncoder().encode("a-different-secret-entirely-0000");
    const bad = `spyrent_session=${await new SignJWT({ uid: 1, role: "admin", name: "x" })
      .setProtectedHeader({ alg: "HS256" })
      .setIssuedAt()
      .setExpirationTime("1h")
      .sign(wrong)}`;
    assert.equal(await status("/admin", bad), 307);
  });

  await check("no session at all is redirected without leaking HTML", async () => {
    const res = await fetch(`${base}/admin/users`, { redirect: "manual" });
    const body = await res.text();
    assert.equal(res.status, 307);
    assert.ok(body.length < 100, `redirect body was ${body.length} bytes`);
  });

  await sql`delete from users where username like ${`sess_%`}`;
  console.log(failures ? `\n${failures} failing` : "\nall passing");
  process.exit(failures ? 1 : 0);
}

run();
