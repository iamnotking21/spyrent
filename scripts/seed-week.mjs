/**
 * Gives the demo child a week of history so the chart has something to show.
 *   node scripts/seed-week.mjs
 */
import "dotenv/config";
import { neon } from "@neondatabase/serverless";

const sql = neon(process.env.DATABASE_URL);

const [child] = await sql`
  select c.id from children c
  join users u on u.id = c.parent_id
  where u.username = 'maria'
  order by c.id limit 1`;

if (!child) {
  console.error("demo child not found — run npm run db:seed first");
  process.exit(1);
}

const days = [
  { back: 6, youtube: 55, roblox: 40, khan: 20 },
  { back: 5, youtube: 60, roblox: 60, khan: 10 },
  { back: 4, youtube: 35, roblox: 25, khan: 45 },
  { back: 3, youtube: 60, roblox: 55, khan: 15 },
  { back: 2, youtube: 48, roblox: 30, khan: 30 },
  { back: 1, youtube: 62, roblox: 58, khan: 12 },
];

// clear anything older than today so repeated runs stay tidy
await sql`
  delete from events
  where child_id = ${child.id} and occurred_at < date_trunc('day', now())`;

for (const day of days) {
  const at = new Date(Date.now() - day.back * 24 * 60 * 60 * 1000);
  await sql`
    insert into events (child_id, kind, target, label, minutes, blocked, occurred_at) values
    (${child.id}, 'app', 'com.google.android.youtube', 'YouTube', ${day.youtube}, false, ${at}),
    (${child.id}, 'app', 'com.roblox.client', 'Roblox', ${day.roblox}, false, ${at}),
    (${child.id}, 'app', 'org.khanacademy.android', 'Khan Academy', ${day.khan}, false, ${at})`;
}

console.log(`added ${days.length} days of history for child #${child.id}`);
