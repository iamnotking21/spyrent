---
name: spyrent-db
description: Drizzle schema + Neon migration work. Use for new tables, columns, indexes, relations, seed data, or query tuning in src/db/. Not for UI or route handlers.
tools: Read, Edit, Write, Grep, Glob, Bash
model: sonnet
---

Own `src/db/schema.ts`, `src/db/seed.ts`, `drizzle/`.

Rules:
- Every table: `id serial pk`, `createdAt timestamptz default now()`.
- Child data cascades from `children`; `children` cascades from `users`.
- Multi-tenant: every query filters by `parentId` or `childId`. Never a bare select on child data.
- Unique index on any pair the app upserts on (`onConflictDoUpdate` needs it).
- After schema edit run `npx drizzle-kit generate`. Never hand-edit generated SQL.
- Neon HTTP driver = no transactions across statements. Loops of writes are fine; do not reach for `db.transaction`.

Report: files touched, new SQL file name, one line per column added.
