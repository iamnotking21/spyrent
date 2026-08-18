# Spyrent

Consent-based parental controls: app time budgets, site blocking, and honest usage history for families.
Rebuilt from the original Android + PHP stack onto a single serverless web app.

- **Marketing site** — `/`
- **Parent portal** — `/portal` (children, rules, activity, device pairing)
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
| POST | `/api/v1/apps` | Upsert the installed app inventory |
| POST | `/api/v1/events` | Report usage; minutes also tick rule budgets |

## Data model

`users` (parent/admin) → `children` (one per device) → `apps`, `rules`, `events`.
`rules.kind` is `app` (target = package name) or `site` (target = domain).
`dailyMinutes = null` means a hard block; otherwise it is a daily budget tracked in `usedMinutes`.
`audit_log` records admin actions.

## Legacy

The original Android sources stay under `study/` for reference: `SpyrentV1` (parent app) and
`ChildApp` (child app), both talking to `spyrent.online/res_api/*.php`.
`.claude/agents/spyrent-android.md` holds the old-endpoint → new-endpoint mapping.

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
- `tests/isolation.test.mjs` — one parent cannot read another parent's children, rules or
  activity; a device token only ever returns its own child
- `tests/session.test.mjs` — disabling or deleting an account ends its live session; forged
  and wrongly-signed cookies are refused

They run against the database in `DATABASE_URL` and delete every row they create.
Point them at a Neon branch if you would rather not touch production data.

## Security notes

- Sessions are JWTs in an httpOnly cookie, but `active` and `role` are read from the database
  on every protected request. Disabling an account in the admin portal takes effect at once
  rather than whenever the token happens to expire.
- Protected routes are gated in `src/middleware.ts`, before rendering. A guard that redirects
  from a layout runs too late: the redirect response still carries the rendered HTML.
- `AUTH_SECRET` and `DATABASE_URL` are required at boot in production (`src/lib/env.ts`).
  There is no fallback secret outside development.
- Rate limiting on sign-in is **not** implemented yet — worth adding before real users.
