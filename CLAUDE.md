# Spyrent — agent brief

Next.js 15 App Router. Neon Postgres via Drizzle. Vercel. Free tier only — never add a paid service.

## Layout
- `src/app/(marketing)` public site · `src/app/portal` parent · `src/app/admin` admin · `src/app/api/v1` device API
- `src/db/schema.ts` single source of truth · `src/lib/auth.ts` session · `src/lib/guard.ts` route guards · `src/lib/device.ts` device auth
- `src/components/ui.tsx` primitives · `src/components/forms.tsx` `ActionForm` + `Field`
- `mobile/` Android child app (Kotlin). The legacy apps were deleted; they live in git history at `9c35419`.

## Rules
- Child data always filtered by `parentId` (parent) — admin bypass only via `requireAdmin`.
- Server actions: validate, ownership-check, write, `revalidatePath`. Return `{ error }` or `undefined`.
- Device API: bearer token only, `{ ok }` envelope, additive changes only.
- Styling: `@theme` tokens in `globals.css`. No raw hex, no new CSS framework.
- Copy: warm, plain, non-surveillance. The child can always see the app is installed.
- Verify with `DATABASE_URL=postgresql://u:p@localhost/db npx next build`.

## Delegation
`spyrent-db` schema · `spyrent-api` routes/actions · `spyrent-ui` screens · `spyrent-android` legacy mapping.
Vertical feature? Use the `spyrent-feature` skill and keep its order.
