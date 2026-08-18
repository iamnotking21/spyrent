/**
 * Cross-tenant checks against a running server.
 *   node tests/isolation.test.mjs [baseUrl]
 * Creates two parents with a child each, then tries to cross the line.
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

const cookieFor = async (uid, role, name) =>
  `spyrent_session=${await new SignJWT({ uid, role, name })
    .setProtectedHeader({ alg: "HS256" })
    .setIssuedAt()
    .setExpirationTime("1h")
    .sign(secret)}`;

async function seedTenant(tag) {
  const [u] = await sql`
    insert into users (username, email, first_name, last_name, password_hash, role)
    values (${`iso_${tag}_${stamp}`}, ${`iso_${tag}_${stamp}@example.com`}, ${tag}, 'Test', 'x', 'parent')
    returning id`;
  const [c] = await sql`
    insert into children (parent_id, name, password_hash, device_token)
    values (${u.id}, ${`kid_${tag}`}, 'x', ${`tok_${tag}_${stamp}`})
    returning id, device_token`;
  await sql`
    insert into rules (child_id, kind, target, label, blocked)
    values (${c.id}, 'site', ${`secret-${tag}.example`}, ${`secret-${tag}.example`}, true)`;
  return { userId: u.id, childId: c.id, token: c.device_token };
}

async function run() {
  console.log("tenant isolation");
  const a = await seedTenant("a");
  const b = await seedTenant("b");
  const cookieA = await cookieFor(a.userId, "parent", "Parent A");

  await check("parent A cannot open parent B's child page", async () => {
    const res = await fetch(`${base}/portal/children/${b.childId}`, {
      headers: { cookie: cookieA },
      redirect: "manual",
    });
    const body = await res.text();
    assert.equal(res.status, 404, `expected 404, got ${res.status}`);
    assert.ok(!body.includes("secret-b.example"), "parent B's rule leaked into the body");
  });

  await check("parent A cannot see parent B's children on the overview", async () => {
    const res = await fetch(`${base}/portal`, { headers: { cookie: cookieA } });
    const body = await res.text();
    assert.ok(!body.includes("kid_b"), "parent B's child leaked");
  });

  await check("parent A cannot see parent B's activity", async () => {
    const res = await fetch(`${base}/portal/activity`, { headers: { cookie: cookieA } });
    const body = await res.text();
    assert.ok(!body.includes("secret-b.example"), "parent B's data leaked");
  });

  await check("device token A returns only child A's policy", async () => {
    const res = await fetch(`${base}/api/v1/policy`, {
      headers: { authorization: `Bearer ${a.token}` },
    });
    const body = await res.json();
    assert.equal(body.child.id, a.childId);
    const domains = body.sites.map((s) => s.domain);
    assert.ok(domains.includes("secret-a.example"));
    assert.ok(!domains.includes("secret-b.example"));
  });

  await check("a device cannot post events for another child", async () => {
    await fetch(`${base}/api/v1/events`, {
      method: "POST",
      headers: { authorization: `Bearer ${a.token}`, "content-type": "application/json" },
      body: JSON.stringify({ events: [{ kind: "site", target: "secret-b.example", minutes: 9 }] }),
    });
    const [row] = await sql`
      select child_id from events where target = 'secret-b.example' order by id desc limit 1`;
    assert.equal(row?.child_id, a.childId, "event was written against the wrong child");
  });

  await check("an unknown device token is rejected", async () => {
    const res = await fetch(`${base}/api/v1/policy`, {
      headers: { authorization: "Bearer definitely-not-a-token" },
    });
    assert.equal(res.status, 401);
  });

  await check("a disabled child's token stops working", async () => {
    await sql`update children set active = false where id = ${a.childId}`;
    const res = await fetch(`${base}/api/v1/policy`, {
      headers: { authorization: `Bearer ${a.token}` },
    });
    assert.equal(res.status, 401);
    await sql`update children set active = true where id = ${a.childId}`;
  });

  await sql`delete from users where username like ${`iso_%_${stamp}`}`;
  console.log(failures ? `\n${failures} failing` : "\nall passing");
  process.exit(failures ? 1 : 0);
}

run();
