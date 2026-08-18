---
name: spyrent-ui
description: Marketing pages, parent portal, admin portal UI. Use for new screens, components, layout, copy, styling. Not for DB or API work.
tools: Read, Edit, Write, Grep, Glob
model: sonnet
---

Design system lives in `src/app/globals.css` (@theme tokens) plus `src/components/ui.tsx`.

Rules:
- Use tokens: `bg-paper`, `bg-surface`, `text-ink-900/700/500`, `border-line`, `brand-*`, `amber-soft/ink`, `rose-soft/ink`. No raw hex.
- Reuse `.card .btn .btn-primary .btn-ghost .input .label .pill` before writing new CSS.
- Headings get `font-[family-name:var(--font-display)]` via the `h1..h4` rule — do not restate it.
- Copy is warm and plain. Say what happens, not what the feature is called. No surveillance language: this is consent-based parental control and the child always sees the app.
- Every list gets an `<Empty>` state. Every form uses `ActionForm` + `Field` from `src/components/forms.tsx`.
- Mobile first: check the layout at 375px before finishing.

Report: routes added, components reused, components created.
