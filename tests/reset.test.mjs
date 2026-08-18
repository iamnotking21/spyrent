/**
 * Password reset. Exercises the library directly; the pages are thin wrappers.
 *   node tests/reset.test.mjs
 */
import "dotenv/config";
import assert from "node:assert/strict";
import { neon } from "@neondatabase/serverless";
import bcrypt from "bcryptjs";

const sql = neon(process.env.DATABASE_URL);
const stamp = Date.now();
const email = `reset_${stamp}@example.com`;

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

const { beginPasswordReset, completePasswordReset } = await import("../src/lib/password-reset.ts");

async function run() {
  console.log("password reset");

  const [u] = await sql`
    insert into users (username, email, first_name, last_name, password_hash, role)
    values (${`reset_${stamp}`}, ${email}, 'Reset', 'Probe', ${bcrypt.hashSync("original-password", 10)}, 'parent')
    returning id`;

  let token;

  await check("an unknown address yields no token", async () => {
    assert.equal(await beginPasswordReset(`nobody_${stamp}@example.com`), null);
  });

  await check("a known address yields a token", async () => {
    token = await beginPasswordReset(email);
    assert.ok(token && token.length >= 32);
  });

  await check("the token is stored hashed, never in the clear", async () => {
    const [row] = await sql`select token_hash from password_resets where user_id = ${u.id}`;
    assert.notEqual(row.token_hash, token, "the raw token is sitting in the table");
    assert.match(row.token_hash, /^[0-9a-f]{64}$/);
  });

  await check("a wrong token is refused", async () => {
    const r = await completePasswordReset("0".repeat(64), "brand-new-password");
    assert.equal(r.ok, false);
  });

  await check("a short password is refused", async () => {
    const r = await completePasswordReset(token, "short");
    assert.equal(r.ok, false);
    assert.match(r.error, /8 characters/);
  });

  await check("the token sets the new password", async () => {
    const r = await completePasswordReset(token, "brand-new-password");
    assert.equal(r.ok, true, JSON.stringify(r));
    const [row] = await sql`select password_hash from users where id = ${u.id}`;
    assert.ok(bcrypt.compareSync("brand-new-password", row.password_hash));
    assert.ok(!bcrypt.compareSync("original-password", row.password_hash));
  });

  await check("the same token cannot be spent twice", async () => {
    const r = await completePasswordReset(token, "another-new-password");
    assert.equal(r.ok, false, "a used token still worked");
  });

  await check("an expired token is refused", async () => {
    const fresh = await beginPasswordReset(email);
    await sql`
      update password_resets set expires_at = now() - interval '1 minute'
      where user_id = ${u.id} and used_at is null`;
    const r = await completePasswordReset(fresh, "yet-another-password");
    assert.equal(r.ok, false);
  });

  await check("a disabled account cannot start a reset", async () => {
    await sql`update users set active = false where id = ${u.id}`;
    assert.equal(await beginPasswordReset(email), null);
    await sql`update users set active = true where id = ${u.id}`;
  });

  await sql`delete from users where id = ${u.id}`;
  console.log(failures ? `\n${failures} failing` : "\nall passing");
  process.exit(failures ? 1 : 0);
}

run();
