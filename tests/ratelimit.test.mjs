/**
 * Sign-in throttling. Exercises the rate limiter directly, since the login
 * form is a server action and cannot be posted to from a script.
 *   node tests/ratelimit.test.mjs
 */
import "dotenv/config";
import assert from "node:assert/strict";
import { neon } from "@neondatabase/serverless";

const sql = neon(process.env.DATABASE_URL);
const key = `probe_${Date.now()}|203.0.113.7`;

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

const { checkLoginAllowed, recordLoginFailure, clearLoginFailures, clientIp } =
  await import("../src/lib/rate-limit.ts");

async function run() {
  console.log("sign-in throttling");

  await check("a fresh identifier is allowed", async () => {
    const r = await checkLoginAllowed(key);
    assert.equal(r.allowed, true);
  });

  await check("seven failures do not lock the account out", async () => {
    for (let i = 0; i < 7; i++) await recordLoginFailure(key);
    const r = await checkLoginAllowed(key);
    assert.equal(r.allowed, true, "locked too early — a typo streak would lock real parents out");
  });

  await check("the eighth failure locks it", async () => {
    await recordLoginFailure(key);
    const r = await checkLoginAllowed(key);
    assert.equal(r.allowed, false);
    // ceil() of a just-set 15 minute lockout can read 901, so allow the rounding
    assert.ok(r.retryAfterSeconds > 0 && r.retryAfterSeconds <= 15 * 60 + 5);
  });

  await check("a correct password clears the record", async () => {
    await clearLoginFailures(key);
    const r = await checkLoginAllowed(key);
    assert.equal(r.allowed, true);
    const rows = await sql`select 1 from login_attempts where identifier = ${key}`;
    assert.equal(rows.length, 0);
  });

  await check("a stale window forgives old failures", async () => {
    for (let i = 0; i < 8; i++) await recordLoginFailure(key);
    assert.equal((await checkLoginAllowed(key)).allowed, false);

    // pretend the window opened twenty minutes ago
    await sql`
      update login_attempts
      set window_start = now() - interval '20 minutes', locked_until = now() - interval '1 minute'
      where identifier = ${key}`;

    assert.equal((await checkLoginAllowed(key)).allowed, true);
    await recordLoginFailure(key);
    const [row] = await sql`select attempts from login_attempts where identifier = ${key}`;
    assert.equal(row.attempts, 1, "the counter should restart, not continue");
  });

  await check("one address cannot lock a different account", async () => {
    const victim = `victim_${Date.now()}|203.0.113.7`;
    assert.equal((await checkLoginAllowed(victim)).allowed, true);
    await sql`delete from login_attempts where identifier = ${victim}`;
  });

  await check("the client address is read from the proxy header", async () => {
    const h = new Headers({ "x-forwarded-for": "198.51.100.4, 10.0.0.1" });
    assert.equal(clientIp(h), "198.51.100.4");
    assert.equal(clientIp(new Headers()), "unknown");
  });

  await sql`delete from login_attempts where identifier like ${"probe_%"} or identifier like ${"victim_%"}`;
  console.log(failures ? `\n${failures} failing` : "\nall passing");
  process.exit(failures ? 1 : 0);
}

run();
