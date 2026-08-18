/**
 * Daily budget reset.
 *   node tests/cron.test.mjs [baseUrl]
 * Set CRON_SECRET in .env to exercise the authenticated path.
 */
import "dotenv/config";
import assert from "node:assert/strict";
import { neon } from "@neondatabase/serverless";

const base = (process.argv[2] ?? "http://localhost:3000").replace(/\/$/, "");
const sql = neon(process.env.DATABASE_URL);
const secret = process.env.CRON_SECRET;
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

const call = (headers = {}) => fetch(`${base}/api/cron/reset`, { headers });

async function run() {
  console.log("daily reset");

  const [u] = await sql`
    insert into users (username, email, first_name, last_name, password_hash, role)
    values (${`cron_${stamp}`}, ${`cron_${stamp}@example.com`}, 'Cron', 'Probe', 'x', 'parent')
    returning id`;
  const [c] = await sql`
    insert into children (parent_id, name, password_hash, device_token)
    values (${u.id}, 'CronKid', 'x', ${`cron_tok_${stamp}`}) returning id`;
  const [r] = await sql`
    insert into rules (child_id, kind, target, label, daily_minutes, used_minutes, blocked, reset_on)
    values (${c.id}, 'app', ${`com.cron.${stamp}`}, 'Cron App', 60, 59, false, '2000-01-01')
    returning id`;

  if (secret) {
    await check("a request with no secret is refused", async () => {
      const res = await call();
      assert.equal(res.status, 401);
      const [row] = await sql`select used_minutes from rules where id = ${r.id}`;
      assert.equal(row.used_minutes, 59, "minutes were reset by an unauthenticated call");
    });

    await check("a wrong secret is refused", async () => {
      const res = await call({ authorization: "Bearer not-the-secret" });
      assert.equal(res.status, 401);
    });
  } else {
    console.log("  skip CRON_SECRET is unset — auth path not exercised");
  }

  await check("the reset clears spent minutes", async () => {
    const res = await call(secret ? { authorization: `Bearer ${secret}` } : {});
    const body = await res.json();
    assert.equal(body.ok, true, JSON.stringify(body));
    const [row] = await sql`select used_minutes, reset_on from rules where id = ${r.id}`;
    assert.equal(row.used_minutes, 0);
    assert.equal(row.reset_on, new Date().toISOString().slice(0, 10));
  });

  await check("running it twice in a day changes nothing more", async () => {
    await sql`update rules set used_minutes = 12 where id = ${r.id}`;
    const res = await call(secret ? { authorization: `Bearer ${secret}` } : {});
    const body = await res.json();
    assert.equal(body.rulesReset, 0, "a second run should skip rules already reset today");
    const [row] = await sql`select used_minutes from rules where id = ${r.id}`;
    assert.equal(row.used_minutes, 12, "a same-day rerun must not wipe minutes spent since");
  });

  await sql`delete from users where id = ${u.id}`;
  console.log(failures ? `\n${failures} failing` : "\nall passing");
  process.exit(failures ? 1 : 0);
}

run();
