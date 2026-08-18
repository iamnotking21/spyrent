/**
 * Exercises account creation against the real database.
 *   node --experimental-strip-types tests/accounts.test.mjs
 * Cleans up every row it makes.
 */
import "dotenv/config";
import assert from "node:assert/strict";
import { neon } from "@neondatabase/serverless";

const sql = neon(process.env.DATABASE_URL);
const stamp = Date.now();
const uname = `t_user_${stamp}`;
const mail = `t_${stamp}@example.com`;

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

const { createParentAccount } = await import("../src/lib/accounts.ts");

async function run() {
  console.log("account creation");

  await check("rejects a short password", async () => {
    const r = await createParentAccount({
      username: uname, email: mail, firstName: "T", lastName: "U", password: "short",
    });
    assert.equal(r.ok, false);
    assert.match(r.error, /8 characters/);
  });

  await check("rejects a blank name", async () => {
    const r = await createParentAccount({
      username: uname, email: mail, firstName: "  ", lastName: "U", password: "longenough1",
    });
    assert.equal(r.ok, false);
  });

  await check("rejects a malformed email", async () => {
    const r = await createParentAccount({
      username: uname, email: "not-an-email", firstName: "T", lastName: "U", password: "longenough1",
    });
    assert.equal(r.ok, false);
  });

  await check("creates the account", async () => {
    const r = await createParentAccount({
      username: uname, email: mail, firstName: "Test", lastName: "User", password: "longenough1",
    });
    assert.equal(r.ok, true, JSON.stringify(r));
    assert.ok(r.id > 0);
  });

  await check("stores a bcrypt hash, never the password", async () => {
    const [row] = await sql`select password_hash, role from users where username = ${uname}`;
    assert.match(row.password_hash, /^\$2[aby]\$/);
    assert.notEqual(row.password_hash, "longenough1");
    assert.equal(row.role, "parent");
  });

  await check("refuses a duplicate username", async () => {
    const r = await createParentAccount({
      username: uname, email: `other_${stamp}@example.com`, firstName: "T", lastName: "U", password: "longenough1",
    });
    assert.equal(r.ok, false);
    assert.match(r.error, /taken/);
  });

  await check("refuses a duplicate email, case-insensitively", async () => {
    const r = await createParentAccount({
      username: `other_${stamp}`, email: mail.toUpperCase(), firstName: "T", lastName: "U", password: "longenough1",
    });
    assert.equal(r.ok, false);
    assert.match(r.error, /already registered/);
  });

  await sql`delete from users where username like ${"t_user_%"} or username like ${"other_%"}`;
  console.log(failures ? `\n${failures} failing` : "\nall passing");
  process.exit(failures ? 1 : 0);
}

run();
