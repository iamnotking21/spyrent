---
name: spyrent-deploy
description: Deploy Spyrent to Vercel with Neon Postgres. Use for first deploy, env var setup, or migration runs against a live database.
---

# Deploy

1. **Neon** — create project `spyrent`, copy the *pooled* connection string.
2. **Local env** — `.env` with `DATABASE_URL` and `AUTH_SECRET` (32+ random chars: `openssl rand -base64 32`).
3. **Migrate** — `npm run db:push`, then `npm run db:seed` for demo data.
4. **Vercel** — import the GitHub repo, framework Next.js, add both env vars to Production and Preview, deploy.
5. **Smoke test**
   - `/` loads, `/register` creates a parent, `/portal` shows the dashboard.
   - `curl -X POST $URL/api/v1/pair -H 'content-type: application/json' -d '{"token":"<deviceToken>"}'` returns `{"ok":true}`.
   - `curl $URL/api/v1/policy -H 'authorization: Bearer <deviceToken>'` returns the rule set.

Notes:
- Neon HTTP driver is serverless-safe; no connection pooling code needed.
- Seed prints the demo device token. Rotate it from the child page before real use.
