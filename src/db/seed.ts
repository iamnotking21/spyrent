import "dotenv/config";
import bcrypt from "bcryptjs";
import { db, users, children, apps, rules, events } from "./index";
import { randomToken } from "../lib/utils";

async function main() {
  const hash = (p: string) => bcrypt.hashSync(p, 10);

  const [admin] = await db
    .insert(users)
    .values({
      username: "admin",
      email: "admin@spyrent.app",
      firstName: "Spyrent",
      lastName: "Admin",
      passwordHash: hash("admin12345"),
      role: "admin",
    })
    .onConflictDoNothing()
    .returning();

  const [parent] = await db
    .insert(users)
    .values({
      username: "maria",
      email: "maria@example.com",
      firstName: "Maria",
      lastName: "Cruz",
      passwordHash: hash("parent12345"),
      role: "parent",
    })
    .onConflictDoNothing()
    .returning();

  if (!parent) {
    console.log("seed already applied");
    return;
  }

  const [mia] = await db
    .insert(children)
    .values({
      parentId: parent.id,
      name: "Mia",
      passwordHash: hash("1234"),
      deviceToken: randomToken(16),
      deviceModel: "Galaxy Tab A8",
      lastSeenAt: new Date(),
    })
    .returning();

  const inventory = [
    { packageName: "com.google.android.youtube", label: "YouTube" },
    { packageName: "com.roblox.client", label: "Roblox" },
    { packageName: "org.khanacademy.android", label: "Khan Academy" },
    { packageName: "com.zhiliaoapp.musically", label: "TikTok" },
  ];
  await db.insert(apps).values(inventory.map((a) => ({ ...a, childId: mia.id })));

  await db.insert(rules).values([
    { childId: mia.id, kind: "app" as const, target: "com.google.android.youtube", label: "YouTube", dailyMinutes: 60, usedMinutes: 42, blocked: false },
    { childId: mia.id, kind: "app" as const, target: "com.roblox.client", label: "Roblox", dailyMinutes: 60, usedMinutes: 55, blocked: false },
    { childId: mia.id, kind: "app" as const, target: "com.zhiliaoapp.musically", label: "TikTok", dailyMinutes: null, blocked: true },
    { childId: mia.id, kind: "site" as const, target: "tiktok.com", dailyMinutes: null, blocked: true },
  ]);

  await db.insert(events).values([
    { childId: mia.id, kind: "app" as const, target: "com.google.android.youtube", label: "YouTube", minutes: 42 },
    { childId: mia.id, kind: "app" as const, target: "com.roblox.client", label: "Roblox", minutes: 55 },
    { childId: mia.id, kind: "site" as const, target: "tiktok.com", label: "tiktok.com", minutes: 0, blocked: true },
  ]);

  console.log("seeded. admin/admin12345, maria/parent12345, device token:", mia.deviceToken);
  if (admin) console.log("admin id", admin.id);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
