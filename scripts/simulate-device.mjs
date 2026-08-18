/**
 * Pretends to be the Android child app so the portal can be demoed
 * without flashing a device.
 *
 *   node scripts/simulate-device.mjs <deviceToken> [baseUrl]
 *
 * Pairs, uploads an app inventory, then reports usage every few seconds.
 */

const token = process.argv[2];
const base = (process.argv[3] ?? "http://localhost:3000").replace(/\/$/, "");

if (!token) {
  console.error("usage: node scripts/simulate-device.mjs <deviceToken> [baseUrl]");
  process.exit(1);
}

const auth = { authorization: `Bearer ${token}`, "content-type": "application/json" };

const INVENTORY = [
  { packageName: "com.google.android.youtube", label: "YouTube" },
  { packageName: "com.roblox.client", label: "Roblox" },
  { packageName: "org.khanacademy.android", label: "Khan Academy" },
  { packageName: "com.zhiliaoapp.musically", label: "TikTok" },
  { packageName: "com.instagram.android", label: "Instagram" },
];

async function call(path, init) {
  const res = await fetch(`${base}${path}`, init);
  const body = await res.json().catch(() => ({}));
  if (!res.ok || body.ok === false) {
    throw new Error(`${path} -> ${res.status} ${JSON.stringify(body)}`);
  }
  return body;
}

async function main() {
  const paired = await call("/api/v1/pair", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ token, deviceModel: "Pixel 7a (simulated)" }),
  });
  console.log(`paired as ${paired.child.name} (child #${paired.child.id})`);

  await call("/api/v1/apps", {
    method: "POST",
    headers: auth,
    body: JSON.stringify({ apps: INVENTORY }),
  });
  console.log(`uploaded ${INVENTORY.length} installed apps`);

  const policy = await call("/api/v1/policy", { headers: auth });
  console.log(`policy: ${policy.apps.length} app rules, ${policy.sites.length} site rules`);
  for (const a of policy.apps) {
    console.log(
      `  ${a.packageName} — ${a.blocked ? "BLOCKED" : `${a.usedMinutes}/${a.dailyMinutes} min`}`,
    );
  }

  console.log("\nreporting usage every 4s — Ctrl+C to stop\n");

  const openable = policy.apps.filter((a) => !a.blocked);
  let i = 0;

  setInterval(async () => {
    try {
      const pick = openable[i++ % Math.max(openable.length, 1)] ?? {
        packageName: "com.roblox.client",
      };
      const minutes = 1 + Math.floor(Math.random() * 4);
      const label = INVENTORY.find((a) => a.packageName === pick.packageName)?.label;

      await call("/api/v1/events", {
        method: "POST",
        headers: auth,
        body: JSON.stringify({
          events: [{ kind: "app", target: pick.packageName, label, minutes }],
        }),
      });
      console.log(`reported ${minutes} min on ${label ?? pick.packageName}`);

      const fresh = await call("/api/v1/policy", { headers: auth });
      const rule = fresh.apps.find((a) => a.packageName === pick.packageName);
      if (rule && rule.dailyMinutes !== null && rule.usedMinutes >= rule.dailyMinutes) {
        console.log(`  budget spent — device would lock ${label ?? pick.packageName}`);
      }

      await call("/api/v1/heartbeat", { method: "POST", headers: auth });
    } catch (err) {
      console.error("tick failed:", err.message);
    }
  }, 4000);
}

main().catch((err) => {
  console.error(err.message);
  process.exit(1);
});
