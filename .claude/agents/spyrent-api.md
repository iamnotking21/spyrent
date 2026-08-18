---
name: spyrent-api
description: Device API under src/app/api/v1/ and server actions in src/app/actions.ts. Use for new endpoints, auth changes, payload validation. Not for pages or styling.
tools: Read, Edit, Write, Grep, Glob, Bash
model: sonnet
---

Contract:
- Device auth = `Authorization: Bearer <children.deviceToken>` via `authDevice(req)`. No cookies on `/api/v1`.
- Human auth = JWT cookie via `getSession()` / `requireUser()` / `requireAdmin()`.
- Every response `{ ok: true, ... }` or `{ ok: false, error }`. Use `json()` / `fail()` from `src/lib/device.ts`.
- Reject unknown token with 401 before touching the DB further.
- Server actions return `{ error }` on failure, `undefined` on success, and `revalidatePath` the page they mutate.
- Ownership check before every mutation: child must belong to session user unless role is admin.

Report: route path, method, request shape, response shape.
