# Spyrent

Consent-based parental controls: app time budgets, site blocking, and honest usage history for families.
Rebuilt from the original Android + PHP stack onto a single serverless web app.

- **Marketing site** — `/`
- **Parent portal** — `/portal` (statistics, children, rules, activity, device pairing)
- **Admin portal** — `/admin` (accounts, platform counts, audit log)
- **Device API** — `/api/v1/*` (consumed by the Android child app)

## Stack

| Layer | Choice | Cost |
|---|---|---|
| Framework | Next.js 15 (App Router, server actions) | free |
| Database | Neon serverless Postgres | free tier |
| ORM | Drizzle | free |
| Auth | JWT in an httpOnly cookie (`jose`) + bcrypt | free |
| Styling | Tailwind v4 tokens, no UI dependency | free |
| Hosting | Vercel | free tier |

## Run it

```bash
npm install
cp .env.example .env      # fill DATABASE_URL + AUTH_SECRET
npm run db:push           # create tables in Neon
npm run db:seed           # demo parent, child, rules, events
npm run dev
```

Seed logins: `admin` / `admin12345` (admin), `maria` / `parent12345` (parent).
The seed prints a device token for the demo child.

## Device API

All device calls except pairing send `Authorization: Bearer <deviceToken>`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/pair` | Swap a pairing token for the child profile |
| POST | `/api/v1/heartbeat` | Mark the device alive |
| GET | `/api/v1/policy` | Fetch every app and site rule to enforce |
| GET | `/api/v1/apps` | Read the stored app inventory and rules |
| POST | `/api/v1/apps` | Upsert the installed app inventory; replies with the packages still missing an icon |
| POST | `/api/v1/events` | Report usage; minutes also tick rule budgets |
| POST | `/api/v1/requests` | Ask for more time on one app |
| GET | `/api/v1/requests` | Check what the parent answered |

## Data model

`users` (parent/admin) → `children` (one per device) → `apps`, `rules`, `events`.
`rules.kind` is `app` (target = package name) or `site` (target = domain).
`dailyMinutes = null` means a hard block; otherwise it is a daily budget tracked in `usedMinutes`.
`audit_log` records who changed what — rules, children, pairing codes, answered requests and
admin actions. Parents see their own entries on the child page; admins see all of them.

## Legacy

The original stack was two Android apps talking to `spyrent.online/res_api/*.php`. Both are
gone from the working tree — they were unbuildable on a modern JDK and nothing depends on them
any more. They remain in git history at commit `9c35419` if the old behaviour ever needs
checking, and `.claude/agents/spyrent-android.md` still holds the old-endpoint to new-endpoint
mapping.

## Deploy

See `.claude/skills/spyrent-deploy/SKILL.md`. Short version: import the repo on Vercel, set
`DATABASE_URL` and `AUTH_SECRET`, deploy.

## AI agents

`.claude/agents/` — `spyrent-db`, `spyrent-api`, `spyrent-ui`, `spyrent-android`.
`.claude/skills/` — `spyrent-feature` (vertical slice workflow), `spyrent-deploy`.

## Demo without an Android device

`scripts/simulate-device.mjs` speaks the same API as the child app: it pairs, uploads an app
inventory, then reports usage on a loop so the portal moves while you talk.

```bash
node scripts/simulate-device.mjs <deviceToken>
node scripts/simulate-device.mjs <deviceToken> https://spyrent.vercel.app
```

The token comes from the seed output or the Pairing card on any child page.

It runs until interrupted. On Windows it survives a `timeout` wrapper, so stop it with Ctrl+C
or it will keep inflating the demo numbers in the background.

To put the demo child back to a clean story:

```bash
node scripts/reset-demo.mjs
```

## Tests

Start the dev server first — two of the suites drive it over HTTP.

```bash
npm test
```

- `tests/accounts.test.mjs` — validation, bcrypt storage, duplicate username and email
- `tests/requests.test.mjs` — asking for more time, one open request per app, and that a
  granted bonus expires overnight instead of raising the limit for good
- `tests/push.test.mjs` — every device of the right parent is notified, nobody else is, and a
  subscription the push service reports as gone is dropped rather than retried forever
- `tests/audit.test.mjs` — changes are recorded against the right child, read as plain
  sentences, never take an action down with them, and stay private to that parent
- `tests/stats.test.mjs` — dashboard figures count the right days and children, rank the
  busiest apps, and never include another parent
- `tests/isolation.test.mjs` — one parent cannot read another parent's children, rules or
  activity; a device token only ever returns its own child
- `tests/cron.test.mjs` — the daily budget reset clears spent minutes once, refuses an
  unauthenticated call, and does not wipe minutes spent later the same day
- `tests/ratelimit.test.mjs` — sign-in lockout after repeated failures, window expiry, and that
  one address cannot lock a stranger out of their account
- `tests/session.test.mjs` — disabling or deleting an account ends its live session; forged
  and wrongly-signed cookies are refused

They run against the database in `DATABASE_URL` and delete every row they create.
Point them at a Neon branch if you would rather not touch production data.

## Daily reset

Budgets are cleared once a day by `/api/cron/reset`, scheduled in `vercel.json`. Vercel sends
`CRON_SECRET` as a bearer token; without that variable the route refuses to run in production,
since an open reset endpoint is a child's way around every limit. The job runs at midnight UTC —
families far from that meridian will see the day roll over at an odd hour until it is made
timezone-aware.

## Security notes

- Sessions are JWTs in an httpOnly cookie, but `active` and `role` are read from the database
  on every protected request. Disabling an account in the admin portal takes effect at once
  rather than whenever the token happens to expire.
- Protected routes are gated in `src/middleware.ts`, before rendering. A guard that redirects
  from a layout runs too late: the redirect response still carries the rendered HTML.
- `AUTH_SECRET` and `DATABASE_URL` are required at boot in production (`src/lib/env.ts`).
  There is no fallback secret outside development.
- Sign-in is throttled: eight failures against the same username-and-address pair lock it for
  fifteen minutes. The counter lives in Postgres, not in memory — serverless instances do not
  share memory, so an in-process map would reset on every cold start and throttle nothing.

## Notifications

Parents can turn on browser notifications from the portal, so a request reaches them without
having to look. This is the Web Push standard with VAPID keys you generate yourself — no
Firebase, no account, nothing to pay for:

```bash
npx web-push generate-vapid-keys
```

Put the pair in `NEXT_PUBLIC_VAPID_PUBLIC_KEY` and `VAPID_PRIVATE_KEY`. Without them the
toggle simply does not appear and requests still show up in the portal.

Works in Chrome, Firefox and Edge, and on iOS 16.4+ once the site is added to the home screen.

## How a parent knows what to block

The child app uploads its launchable apps — package name, the label the device shows, and a
96px icon — at pairing and then on every background sync, so an app installed next week turns
up on its own. Uninstalled apps drop off the list.

Icons travel once: the upload replies with the packages it still lacks an icon for, and the
device sends only those. A routine sync is therefore a few kilobytes rather than a re-upload
of every icon on the phone.

In the portal the app list leads with whatever the child used most this week, with icons and a
search box, so a parent picks "Roblox" from what is actually on the device rather than typing
`com.roblox.client` from memory.

## Asking for more time

A child who runs out of minutes can ask from the lock screen. The request lands on the
child's page in the portal, where granting it adds the minutes as a **bonus for today**
rather than raising the standing limit — so tomorrow the usual rule applies without anyone
remembering to undo anything. Repeated taps do not stack up: one open request per app.
