/**
 * Site time budgets end to end over HTTP.
 *   node tests/site-budget.test.mjs [baseUrl]
 *
 * A site rule with dailyMinutes set (not an outright block) went untracked on
 * the device: the policy response never carried usedMinutes for sites, so the
 * app had no way to know how much of the budget was already spent.
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
  console.log("site time budgets");

  const [u] = await sql`
    insert into users (username, email, first_name, last_name, password_hash, role)
    values (${`sb_${stamp}`}, ${`sb_${stamp}@example.com`}, 'Site', 'Probe', 'x', 'parent')
    returning id`;
  const [c] = await sql`
    insert into children (parent_id, name, password_hash, device_token, timezone)
    values (${u.id}, 'SiteKid', 'x', ${`sb_tok_${stamp}`}, 'UTC') returning id, device_token`;
  const domain = `budget-${stamp}.example`;

  await sql`
    insert into rules (child_id, kind, target, label, daily_minutes, used_minutes, blocked)
    values (${c.id}, 'site', ${domain}, ${domain}, 2, 0, false)`;

  const auth = { authorization: `Bearer ${c.device_token}`, "content-type": "application/json" };

  await check("the policy response carries usedMinutes for a site", async () => {
    const res = await fetch(`${base}/api/v1/policy`, { headers: auth });
    const body = await res.json();
    const site = body.sites.find((s) => s.domain === domain);
    assert.ok(site, "the site rule is missing from the policy");
    assert.equal(site.dailyMinutes, 2);
    assert.equal(site.usedMinutes, 0);
    assert.equal(site.blocked, false);
  });

  await check("reporting site usage raises usedMinutes", async () => {
    await fetch(`${base}/api/v1/events`, {
      method: "POST",
      headers: auth,
      body: JSON.stringify({ events: [{ kind: "site", target: domain, minutes: 1 }] }),
    });

    const res = await fetch(`${base}/api/v1/policy`, { headers: auth });
    const body = await res.json();
    const site = body.sites.find((s) => s.domain === domain);
    assert.equal(site.usedMinutes, 1);
  });

  await check("once used minutes reach the limit the site reads as spent", async () => {
    await fetch(`${base}/api/v1/events`, {
      method: "POST",
      headers: auth,
      body: JSON.stringify({ events: [{ kind: "site", target: domain, minutes: 1 }] }),
    });

    const res = await fetch(`${base}/api/v1/policy`, { headers: auth });
    const body = await res.json();
    const site = body.sites.find((s) => s.domain === domain);
    assert.equal(site.usedMinutes, 2);
    assert.equal(site.usedMinutes >= site.dailyMinutes, true, "the device has no way to see this is spent");
  });

  await check("a granted bonus extends today's site budget", async () => {
    await sql`update rules set bonus_minutes = 5 where child_id = ${c.id} and target = ${domain}`;

    const res = await fetch(`${base}/api/v1/policy`, { headers: auth });
    const body = await res.json();
    const site = body.sites.find((s) => s.domain === domain);
    assert.equal(site.dailyMinutes, 7, "dailyMinutes should include the bonus, same as apps");
  });

  await sql`delete from users where id = ${u.id}`;
  console.log(failures ? `\n${failures} failing` : "\nall passing");
  process.exit(failures ? 1 : 0);
}

run();
