# AGENTS.md

## Project

- **Name**: DongRuanEnvironment
- **Stack**: Spring Boot 3.2 / Java 17 / MyBatis-Plus / MySQL 8; Vue 3 / TypeScript / Vite / Pinia
- **Rule**: This file is an index. All project context, constraints, and task records live in `docs/`.

## Session Start

`.codex/hooks.json` runs a lightweight status check only. It never starts or waits for services. If port 8080 is not listening, start the backend manually with `pwsh -ExecutionPolicy Bypass -File .\init.ps1` or `bash init.sh` when needed.

## Commands

- **Backend build/test**: `cd back && ./mvnw test`
- **Frontend build**: `cd front && npm run build`
- **Frontend lint**: `cd front && npm run lint`
- **Frontend dev server**: `cd front && npm run dev`

The frontend proxies `/api` to `http://localhost:8080`; run the backend before testing API pages.

## Boundaries

| Before you… | Read this first |
|---|---|
| Understand requirements | `docs/1-requirements/` |
| Change API or database behavior | `docs/2-designs/` |
| Make non-trivial changes | `docs/3-constraints/` |
| Work on a feature | `docs/4-tasks/CURRENT_PLAN.md` and its `spec.md` |

## Workflow

1. Read `CURRENT_PLAN.md` and the active task specification.
2. Read the relevant module guide: `back/AGENTS.md` or `front/AGENTS.md`.
3. Keep API and schema contracts in `docs/2-designs/` synchronized with implementation.
4. Run the focused backend or frontend checks before declaring work complete.

## Modules

- `back/`: Spring Boot API on port 8080.
- `front/`: Vue single-page application, served by Vite.
