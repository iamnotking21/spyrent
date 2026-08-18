/**
 * Audit trail.
 *   node tests/audit.test.mjs [baseUrl]
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

const { record, describe } = await import("../src/lib/audit.ts");

const cookieFor = async (uid, role) =>
  `spyrent_session=${await new SignJWT({ uid, role, name: "Audit Probe" })
    .setProtectedHeader({ alg: "HS256" })
    .setIssuedAt()
    .setExpirationTime("1h")
    .sign(secret)}`;

async function run() {
  console.log("audit trail");

  const mkParent = async (tag) => {
    const [u] = await sql`
      insert into users (username, email, first_name, last_name, password_hash, role)
      values (${`aud_${tag}_${stamp}`}, ${`aud_${tag}_${stamp}@example.com`}, ${tag}, 'Probe', 'x', 'parent')
      returning id`;
    const [c] = await sql`
      insert into children (parent_id, name, password_hash, device_token)
      values (${u.id}, ${`kid_${tag}_${stamp}`}, 'x', ${`aud_tok_${tag}_${stamp}`})
      returning id`;
    return { userId: u.id, childId: c.id };
  };

  const a = await mkParent("a");
  const b = await mkParent("b");

  await check("an entry is stored against the child", async () => {
    await record({
      actorId: a.userId,
      childId: a.childId,
      action: "rule.set",
      detail: "com.example.app (60 min)",
    });
    const rows = await sql`select * from audit_log where child_id = ${a.childId}`;
    assert.equal(rows.length, 1);
    assert.equal(rows[0].action, "rule.set");
  });

  await check("entries read as plain sentences", () => {
    assert.equal(describe("rule.block", "tiktok.com"), "Blocked tiktok.com");
    assert.equal(describe("request.granted", "YouTube (+15 min)"), "Granted more time on YouTube (+15 min)");
    assert.equal(describe("child.token_rotated", null), "Generated a new pairing code");
    // an unknown action must still render something rather than blowing up
    assert.ok(describe("something.new", "x").length > 0);
  });

  await check("a failed write does not throw", async () => {
    // a child id that cannot exist violates the foreign key
    await record({ actorId: a.userId, childId: 2_000_000_000, action: "rule.set", detail: "x" });
  });

  await check("one parent cannot read another parent's history", async () => {
    await record({
      actorId: b.userId,
      childId: b.childId,
      action: "rule.block",
      detail: `secret-audit-${stamp}.example`,
    });

    const res = await fetch(`${base}/portal/children/${b.childId}`, {
      headers: { cookie: await cookieFor(a.userId, "parent") },
      redirect: "manual",
    });
    const body = await res.text();

    assert.equal(res.status, 404);
    assert.ok(!body.includes(`secret-audit-${stamp}`), "another parent's audit entry leaked");
  });

  await check("a parent sees their own child's history", async () => {
    const res = await fetch(`${base}/portal/children/${a.childId}`, {
      headers: { cookie: await cookieFor(a.userId, "parent") },
    });
    const body = await res.text();
    assert.equal(res.status, 200);
    assert.ok(body.includes("Changes you made"), "the history section is missing");
    assert.ok(body.includes("com.example.app"), "the entry is not shown");
  });

  await sql`delete from users where id in (${a.userId}, ${b.userId})`;
  console.log(failures ? `\n${failures} failing` : "\nall passing");
  process.exit(failures ? 1 : 0);
}

run();
