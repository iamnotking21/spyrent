/**
 * "More time" requests, end to end over HTTP.
 *   node tests/requests.test.mjs [baseUrl]
 */
import "dotenv/config";
import assert from "node:assert/strict";
import { neon } from "@neondatabase/serverless";

const base = (process.argv[2] ?? "http://localhost:3000").replace(/\/$/, "");
const sql = neon(process.env.DATABASE_URL);
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

async function run() {
  console.log("more-time requests");

  const [u] = await sql`
    insert into users (username, email, first_name, last_name, password_hash, role)
    values (${`req_${stamp}`}, ${`req_${stamp}@example.com`}, 'Req', 'Probe', 'x', 'parent')
    returning id`;
  const [c] = await sql`
    insert into children (parent_id, name, password_hash, device_token, timezone)
    values (${u.id}, 'ReqKid', 'x', ${`req_tok_${stamp}`}, 'UTC') returning id, device_token`;
  const target = `com.req.${stamp}`;
  const [rule] = await sql`
    insert into rules (child_id, kind, target, label, daily_minutes, used_minutes, blocked, reset_on)
    values (${c.id}, 'app', ${target}, 'Req App', 60, 60, false,
            to_char(now(), 'YYYY-MM-DD'))
    returning id`;

  const auth = { authorization: `Bearer ${c.device_token}`, "content-type": "application/json" };
  const ask = (body) =>
    fetch(`${base}/api/v1/requests`, { method: "POST", headers: auth, body: JSON.stringify(body) });

  await check("an unpaired device cannot ask", async () => {
    const res = await fetch(`${base}/api/v1/requests`, {
      method: "POST",
      headers: { authorization: "Bearer nonsense", "content-type": "application/json" },
      body: JSON.stringify({ target, minutes: 15 }),
    });
    assert.equal(res.status, 401);
  });

  let requestId;

  await check("the device can ask for more time", async () => {
    const body = await (await ask({ target, label: "Req App", minutes: 15 })).json();
    assert.equal(body.ok, true);
    assert.equal(body.status, "pending");
    requestId = body.requestId;
  });

  await check("asking twice keeps one open request", async () => {
    const body = await (await ask({ target, minutes: 15 })).json();
    assert.equal(body.requestId, requestId);
    assert.equal(body.alreadyOpen, true);
    const rows = await sql`
      select id from time_requests where child_id = ${c.id} and status = 'pending'`;
    assert.equal(rows.length, 1);
  });

  await check("a wild number of minutes is clamped", async () => {
    await sql`delete from time_requests where child_id = ${c.id}`;
    const body = await (await ask({ target, minutes: 9999 })).json();
    const [row] = await sql`select minutes from time_requests where id = ${body.requestId}`;
    assert.ok(row.minutes <= 120, `stored ${row.minutes} minutes`);
  });

  await check("granting adds a bonus without moving the standing limit", async () => {
    const [req] = await sql`
      select * from time_requests where child_id = ${c.id} order by id desc limit 1`;
    await sql`update time_requests set status = 'granted', answered_at = now() where id = ${req.id}`;
    await sql`
      update rules set bonus_minutes = bonus_minutes + ${req.minutes}, blocked = false
      where id = ${rule.id}`;

    const [row] = await sql`select daily_minutes, bonus_minutes from rules where id = ${rule.id}`;
    assert.equal(row.daily_minutes, 60, "the standing limit was changed");
    assert.equal(row.bonus_minutes, req.minutes);

    const policy = await (
      await fetch(`${base}/api/v1/policy`, { headers: { authorization: auth.authorization } })
    ).json();
    const app = policy.apps.find((a) => a.packageName === target);
    assert.equal(app.dailyMinutes, 60 + req.minutes, "the device did not see today's extra time");
  });

  await check("tomorrow the bonus is gone", async () => {
    await sql`update rules set reset_on = '2000-01-01' where id = ${rule.id}`;
    await fetch(`${base}/api/v1/policy`, { headers: { authorization: auth.authorization } });

    const [row] = await sql`select daily_minutes, bonus_minutes, used_minutes from rules where id = ${rule.id}`;
    assert.equal(row.bonus_minutes, 0, "granted minutes leaked into the next day");
    assert.equal(row.daily_minutes, 60);
    assert.equal(row.used_minutes, 0);
  });

  await sql`delete from users where id = ${u.id}`;
  console.log(failures ? `\n${failures} failing` : "\nall passing");
  process.exit(failures ? 1 : 0);
}

run();
