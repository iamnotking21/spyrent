/**
 * Puts the demo child back to a believable story.
 *   node scripts/reset-demo.mjs
 * Only touches the seeded demo parent's data.
 */
import "dotenv/config";
import { neon } from "@neondatabase/serverless";

const sql = neon(process.env.DATABASE_URL);

const [child] = await sql`
  select c.id, c.name from children c
  join users u on u.id = c.parent_id
  where u.username = 'maria'
  order by c.id limit 1`;

if (!child) {
  console.error("demo child not found — run npm run db:seed first");
  process.exit(1);
}

await sql`delete from events where child_id = ${child.id}`;

await sql`update rules set used_minutes = 42 where child_id = ${child.id} and target = 'com.google.android.youtube'`;
await sql`update rules set used_minutes = 55 where child_id = ${child.id} and target = 'com.roblox.client'`;
await sql`update rules set used_minutes = 0  where child_id = ${child.id} and daily_minutes is null`;

await sql`
  insert into events (child_id, kind, target, label, minutes, blocked, occurred_at) values
  (${child.id}, 'app',  'com.google.android.youtube', 'YouTube',  42, false, now() - interval '3 hours'),
  (${child.id}, 'app',  'com.roblox.client',          'Roblox',   55, false, now() - interval '2 hours'),
  (${child.id}, 'app',  'org.khanacademy.android',    'Khan Academy', 18, false, now() - interval '5 hours'),
  (${child.id}, 'site', 'tiktok.com',                 'tiktok.com', 0, true,  now() - interval '90 minutes'),
  (${child.id}, 'app',  'com.zhiliaoapp.musically',   'TikTok',      0, true,  now() - interval '40 minutes')`;

await sql`update children set last_seen_at = now() where id = ${child.id}`;

console.log(`reset ${child.name}: YouTube 42/60, Roblox 55/60, TikTok blocked, 5 events`);
