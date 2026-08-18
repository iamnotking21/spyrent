/**
 * Push fan-out. The sender is injected, so this covers the part we own —
 * who gets notified, what payload goes out, and what happens to a
 * subscription the push service says is gone. The encryption itself is
 * web-push's job and is not re-tested here.
 *   node tests/push.test.mjs
 */
import "dotenv/config";
import assert from "node:assert/strict";
import crypto from "node:crypto";
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

const { notifyUser } = await import("../src/lib/push.ts");

/** Stands in for the browser's push service. */
function fakeSender({ gone = [] } = {}) {
  const sent = [];
  const send = async (sub, payload) => {
    if (gone.includes(sub.endpoint)) {
      const err = new Error("gone");
      err.statusCode = 410;
      throw err;
    }
    sent.push({ endpoint: sub.endpoint, payload });
  };
  return { send, sent };
}

async function run() {
  console.log("push notifications");

  const [parent] = await sql`
    insert into users (username, email, first_name, last_name, password_hash, role)
    values (${`push_${stamp}`}, ${`push_${stamp}@example.com`}, 'Push', 'Probe', 'x', 'parent')
    returning id`;
  const [other] = await sql`
    insert into users (username, email, first_name, last_name, password_hash, role)
    values (${`push2_${stamp}`}, ${`push2_${stamp}@example.com`}, 'Other', 'Parent', 'x', 'parent')
    returning id`;

  const keys = () => {
    const ecdh = crypto.createECDH("prime256v1");
    ecdh.generateKeys();
    return {
      p256dh: ecdh.getPublicKey().toString("base64url"),
      auth: crypto.randomBytes(16).toString("base64url"),
    };
  };

  const subscribe = async (userId, endpoint) => {
    const k = keys();
    await sql`
      insert into push_subscriptions (user_id, endpoint, p256dh, auth)
      values (${userId}, ${endpoint}, ${k.p256dh}, ${k.auth})`;
  };

  await check("a parent with no subscription is a no-op", async () => {
    const fake = fakeSender();
    assert.equal(await notifyUser(parent.id, { title: "x", body: "y" }, fake.send), 0);
    assert.equal(fake.sent.length, 0);
  });

  await check("every device of that parent is notified", async () => {
    await subscribe(parent.id, `https://push.example/${stamp}/phone`);
    await subscribe(parent.id, `https://push.example/${stamp}/laptop`);

    const fake = fakeSender();
    const sent = await notifyUser(
      parent.id,
      {
        title: "Mia is asking for more time",
        body: "YouTube — 15 more minutes",
        url: "/portal/children/1",
      },
      fake.send,
    );

    assert.equal(sent, 2);
    assert.equal(fake.sent.length, 2);
  });

  await check("the payload carries the title, body and destination", async () => {
    const fake = fakeSender();
    await notifyUser(
      parent.id,
      { title: "Mia is asking", body: "YouTube", url: "/portal/children/1" },
      fake.send,
    );

    const payload = JSON.parse(fake.sent[0].payload);
    assert.equal(payload.title, "Mia is asking");
    assert.equal(payload.body, "YouTube");
    assert.equal(payload.url, "/portal/children/1");
  });

  await check("another parent's devices are left alone", async () => {
    await subscribe(other.id, `https://push.example/${stamp}/stranger`);

    const fake = fakeSender();
    await notifyUser(parent.id, { title: "x", body: "y" }, fake.send);

    assert.ok(
      !fake.sent.some((s) => s.endpoint.endsWith("/stranger")),
      "a different parent was notified",
    );
  });

  await check("a subscription the service says is gone is dropped", async () => {
    const dead = `https://push.example/${stamp}/dead`;
    await subscribe(parent.id, dead);

    const fake = fakeSender({ gone: [dead] });
    const sent = await notifyUser(parent.id, { title: "x", body: "y" }, fake.send);

    assert.equal(sent, 2, "the live devices should still have been notified");

    const rows = await sql`
      select endpoint from push_subscriptions where user_id = ${parent.id}`;
    assert.ok(!rows.some((r) => r.endpoint === dead), "the dead subscription is still stored");
    assert.equal(rows.length, 2, "a live subscription was pruned by mistake");
  });

  await sql`delete from users where id in (${parent.id}, ${other.id})`;
  console.log(failures ? `\n${failures} failing` : "\nall passing");
  process.exit(failures ? 1 : 0);
}

run();
