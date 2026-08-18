---
name: spyrent-feature
description: Add a full vertical feature to Spyrent (schema + API + portal UI) in the house order and style. Use when asked to add a capability that touches more than one layer.
---

# Ship a Spyrent feature

Order is fixed. Do not skip ahead.

1. **Schema** — `spyrent-db` agent. Add table/columns, then `npx drizzle-kit generate`.
2. **Server action or endpoint** — `spyrent-api` agent. Ownership check first, validation second, write third, `revalidatePath` last.
3. **UI** — `spyrent-ui` agent. Reuse `Card/Stat/Badge/Empty` and `ActionForm/Field`. Add an empty state.
4. **Verify** — `DATABASE_URL=postgresql://u:p@localhost/db npx next build`. Type errors are blocking; lint is skipped by config.
5. **Report** — file list, new route, new env var (if any).

Guard rails:
- Never query child data without a `parentId`/`childId` filter.
- Never add a paid dependency or a service outside the free tier (Neon + Vercel only).
- Keep the device API backward compatible: add fields, never rename them.
