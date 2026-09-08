# AGENTS.md — Frontend

> Supplements the root `AGENTS.md`; root rules apply.

## Context

- **Stack**: Vue 3, TypeScript, Vite, Vue Router, Pinia, Axios.
- **Source**: `src/`; views: `src/views/`; API clients: `src/api/`.
- **Proxy**: `/api` → `http://localhost:8080`.

## Commands

- Run: `npm run dev`
- Build/type-check: `npm run build`
- Lint: `npm run lint`
- Format: `npm run format`

## Conventions

Use two-space indentation, single quotes, no semicolons, and a 100-character print width. Keep request functions in `src/api/`, typed with exported interfaces; place routed pages in `src/views/`. Run build and lint before completion.

Read root `docs/2-designs/api_contract.md` before changing API usage.
