/**
 * Dashboard statistics.
 *   node tests/stats.test.mjs
 */
import "dotenv/config";
import assert from "node:assert/strict";
import { neon } from "@neondatabase/serverless";

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

const { statsFor } = await import("../src/lib/stats.ts");

async function run() {
  console.log("dashboard statistics");

  const mkParent = async (tag) => {
    const [u] = await sql`
      insert into users (username, email, first_name, last_name, password_hash, role)
      values (${`st_${tag}_${stamp}`}, ${`st_${tag}_${stamp}@example.com`}, ${tag}, 'Probe', 'x', 'parent')
      returning id`;
    return u.id;
  };

  const mkChild = async (parentId, name, timezone = "UTC") => {
    const [c] = await sql`
      insert into children (parent_id, name, password_hash, device_token, timezone)
      values (${parentId}, ${name}, 'x', ${`st_tok_${name}_${stamp}`}, ${timezone})
      returning id`;
    return c.id;
  };

  const parentA = await mkParent("a");
  const parentB = await mkParent("b");
  const kid1 = await mkChild(parentA, `kid1_${stamp}`);
  const kid2 = await mkChild(parentA, `kid2_${stamp}`);
  const stranger = await mkChild(parentB, `kidb_${stamp}`);

  const event = async (childId, target, minutes, hoursAgo, blocked = false) => {
    await sql`
      insert into events (child_id, kind, target, label, minutes, blocked, occurred_at)
      values (${childId}, 'app', ${target}, ${target}, ${minutes}, ${blocked},
              now() - (${hoursAgo} || ' hours')::interval)`;
  };

  await check("a parent with no children gets zeroes, not a crash", async () => {
    const empty = await mkParent("empty");
    const stats = await statsFor(empty);
    assert.equal(stats.childCount, 0);
    assert.equal(stats.todayMinutes, 0);
    assert.deepEqual(stats.topApps, []);
    await sql`delete from users where id = ${empty}`;
  });

  await check("today counts only today, across every child", async () => {
    // one hour ago is today in UTC unless the run straddles midnight
    await event(kid1, "com.a", 30, 1);
    await event(kid2, "com.a", 20, 2);
    await event(kid1, "com.b", 45, 26); // yesterday

    const stats = await statsFor(parentA);
    assert.equal(stats.childCount, 2);
    assert.equal(stats.todayMinutes, 50, "today should be 30 + 20");
    assert.equal(stats.weekMinutes, 95, "the week should include yesterday");
  });

  await check("another parent's children are not counted", async () => {
    await event(stranger, "com.stranger", 500, 1);

    const stats = await statsFor(parentA);
    assert.equal(stats.todayMinutes, 50, "a stranger's usage leaked in");
    assert.ok(
      !stats.topApps.some((a) => a.target === "com.stranger"),
      "a stranger's app appeared in the top list",
    );
    assert.ok(!stats.perChild.some((c) => c.id === stranger), "a stranger's child appeared");
  });

  await check("the busiest apps are ranked by minutes", async () => {
    await event(kid1, "com.busiest", 200, 3);

    const stats = await statsFor(parentA);
    assert.equal(stats.topApps[0].target, "com.busiest");
    assert.equal(stats.topApps[0].minutes, 200);
    assert.ok(stats.topApps.length <= 5, "the list should stay short");
  });

  await check("blocks this week are counted", async () => {
    await event(kid1, "com.blocked", 0, 5, true);
    const stats = await statsFor(parentA);
    assert.equal(stats.blockedThisWeek, 1);
  });

  await check("pending requests are counted per child", async () => {
    await sql`
      insert into time_requests (child_id, target, label, minutes, status)
      values (${kid2}, 'com.a', 'A', 15, 'pending')`;

    const stats = await statsFor(parentA);
    assert.equal(stats.pendingRequests, 1);
    assert.equal(stats.perChild.find((c) => c.id === kid2).pending, 1);
    assert.equal(stats.perChild.find((c) => c.id === kid1).pending, 0);
  });

  await check("the week has seven days, oldest first", async () => {
    const stats = await statsFor(parentA);
    assert.equal(stats.week.length, 7);
    const dates = stats.week.map((d) => d.date);
    assert.deepEqual([...dates].sort(), dates, "days are out of order");
  });

  await check("a rule that is used up is flagged on the child", async () => {
    await sql`
      insert into rules (child_id, kind, target, label, daily_minutes, used_minutes, blocked)
      values (${kid1}, 'app', ${`com.spent.${stamp}`}, 'Spent', 30, 30, false)`;

    const stats = await statsFor(parentA);
    const summary = stats.perChild.find((c) => c.id === kid1);
    assert.equal(summary.ruleCount, 1);
    assert.equal(summary.usedUpCount, 1);
  });

  await sql`delete from users where id in (${parentA}, ${parentB})`;
  console.log(failures ? `\n${failures} failing` : "\nall passing");
  process.exit(failures ? 1 : 0);
}

run();
