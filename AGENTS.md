# Koski - Agent Instructions

## Project Overview

Koski is a Finnish national education data repository system that stores and manages student records (opiskeluoikeus), qualifications, and achievements across all education levels. It includes Valpas, a compulsory education monitoring service.

**Primary language**: Finnish (commit messages, documentation, UI text)
**Code language**: Finnish is preferred but English can be used where appropriate or where English has already been used in that context (variable names, function names, comments)

## Tech Stack

### Backend
- **Scala 2.13** with Scalatra web framework
- **PostgreSQL 15** database
- **OpenSearch** for indexing/search
- **Slick** for database access
- **Flyway** for database migrations
- **Maven** for build
- **IntelliJ IDEA** for development environment and running/debugging test configurations

### Frontend
- **Koski UI** (`web/`): TypeScript, React 16, Webpack, Bacon.js, LESS
- **Valpas UI** (`valpas-web/`): TypeScript, React 19, Parcel bundler
- Package manager: **pnpm**

## Quick Commands

```bash
# Start development databases (PostgreSQL + OpenSearch)
make docker-dbs-arm64    # Apple Silicon

# Build and run
make build               # Build entire application
make run                 # Run application (http://localhost:7021/koski/virkailija)
make watch               # Watch frontend for changes

make front               # One-shot frontend build in prod mode (non-watching; use after editing web/app/ to refresh bundles)

# Testing
make test                # Run all tests
make backtest            # Backend tests only
make fronttest           # Frontend (Mocha) tests only
make integrationtest     # Playwright integration tests

# Code quality
make lint                # Run all linters (eslint + prettier)
make eslint              # JavaScript/TypeScript linting
make prettier            # Prettier formatting check (web/app)

# Other useful commands
make ts-types            # Regenerate TypeScript types from Scala schema
make reset-raportointikanta  # Reset reporting database
```

## Project Structure

```
src/main/scala/fi/oph/koski/   # Backend Scala code
├── schema/                     # Data models (Opiskeluoikeus, Suoritus, etc.)
├── opiskeluoikeus/            # Study right management
├── oppija/                    # Student management
├── henkilo/                   # Person data (from external registry)
├── organisaatio/              # Organization management
├── validation/                # Data validation
├── raportit/                  # Reports
├── tiedonsiirto/              # Data transfers
├── valpas/                    # Compulsory education monitoring
├── koskiuser/                 # Authentication/authorization
└── ...                        # 70+ other modules

web/app/                       # Koski frontend
├── editor/                    # Main editor components
├── omattiedot/                # Student self-service
├── uusiopiskeluoikeus/        # New study right creation
├── types/fi/oph/koski/        # Generated TypeScript types (don't edit manually)
└── ...

valpas-web/src/                # Valpas frontend (separate React app)
```

## Code Style

### General
- **Indentation**: 2 spaces (all languages)
- **Line endings**: LF
- **Trailing whitespace**: None
- **Final newline**: Required

### Scala
- No `println` statements
- No `return` statements (including mid-function early returns — use idiomatic control flow instead)
- Class names: PascalCase
- Use `@Description`, `@KoodistoUri`, and other schema annotations for data models
- When adding a new parameter with a default value to an existing function, check all call sites and evaluate whether they should pass the real value instead of relying on the default. The compiler won't warn about missing values.

### TypeScript/JavaScript
- Use TypeScript for new code
- Prettier for formatting
- ESLint for linting
- Don't edit files in `web/app/types/fi/oph/koski/` - these are generated

### Commit Messages
- Written in **Finnish**
- Often reference ticket numbers (e.g., `TOR-2400`, `TOR-XXXX`), ask for it if not yet known
- Format: Short description of what was done in the first line, more elaborate description and context below
- Examples:
  - `Päivitä puppeteer-versio kansalaisen smoketesteissä`
  - `Lisää uudet esimerkkioppijat testeihin`
  - `fix: try recursive pnpm install`

## Testing

### IntelliJ IDEA MCP

If I have IntelliJ IDEA MCP configured: Use it for building, running and debugging backend tests. Do this instead of the
command line approach detailed below and don't fall back by default to using command line when IntelliJ IDEA MCP server calls
fail.

Use maxLinesCount: 100, truncateMode: "END" parameters. The default output won't fit in context.

If a specific configuration for the single test or suite is not available, ask me to run it first time manually,
and then later it is available.

### Backend Tests
Location: `src/test/scala/fi/oph/koski/`

Run specific test suite:
```bash
mvn test -Dsuites="fi.oph.koski.schema.SchemaSpec"
```

**Important:** `BackwardCompatibilitySpec` compares each documentation `Example` against a stored JSON snapshot under `src/test/resources/backwardcompatibility/`, matched by sanitized example name. When you **rename or change the data of an `Example`** (e.g. in `documentation/Examples*.scala`), regenerate its snapshot: run `BackwardCompatibilitySpec` locally — it writes a new dated file — and commit it. CI fails if the snapshot is missing (it refuses to write on CI). If you renamed the example, also delete the now-orphaned old snapshot.

### Frontend Tests
- **Mocha tests**: `web/test/` - run with `make fronttest`
- **Playwright tests**: `web/test/e2e/` - run with `make integrationtest`
- **Valpas Jest tests**: `valpas-web/test/` - run with `make valpas-fronttest-*`

### Test Users
Test users are defined in `src/main/scala/fi/oph/koski/koskiuser/MockUsers.scala`
Default test user: username "pää", password "pää"
Test students for Koski local development are defined in `src/main/scala/fi/oph/koski/henkilo/KoskiSpecificMockOppijat.scala`
Test students for Valpas local development are defined in `src/main/scala/fi/oph/koski/valpas/opiskeluoikeusfixture/ValpasMockOppijat.scala`

**Important:** New mock oppijat in `KoskiSpecificMockOppijat` and `ValpasMockOppijat` must be added at the **end** of the list, not in the middle. Oppija OIDs are generated sequentially, so inserting in the middle shifts all subsequent OIDs and breaks fixture data and UI tests.

Because the OIDs are deterministic, Playwright/e2e tests can hardcode a fixture oppija's OID (the existing `perusopetus-v2-*.spec.ts` specs do this). To find the OID for an oppija that doesn't pin one explicitly, resolve it once against the running app, e.g. `curl -u pää:pää -X POST localhost:7021/koski/api/henkilo/search -H 'Content-Type: application/json' -d '{"query":"Sukunimi"}'`.

## Database

### Local Development
Three databases are used:
- `koski` - main application data
- `valpas` - Valpas-specific data
- `raportointikanta` - reporting database

Test versions: `koski_test`, `valpas_test`, `raportointikanta_test`

### Migrations
Located in: `src/main/resources/db/migration/`
Migrations run automatically on application startup.

**Important:** When adding a new migration file, update the expected file count in `src/test/scala/fi/oph/koski/migration/MigrationSpec.scala` (`MigrationSpec`).

### Connect to local DB
```bash
psql -h localhost koski oph
```

## Dependency vulnerabilities and pnpm overrides

When Trivy or an advisory flags a transitive dependency, **do not reach for a `pnpm.overrides` entry first.** An override is permanent residue: it needs its own Renovate PRs forever, and an exact pin turns a self-healing transitive dep into one that can only be fixed by hand. In TOR-2696, three such pins (`"js-yaml": "4.3.0"`) were themselves holding a HIGH-severity version in place — the override *was* the vulnerability.

Work through this order:

1. **Is the lockfile just stale?** Check the parent's declared range (`npm view <parent>@<version> dependencies`). If it already permits a fixed version, the fix is a lockfile bump — `pnpm -C <dir> update <pkg> --lockfile-only --ignore-scripts` — with no manifest change. This covers the large majority of findings; 17 of the 18 overrides audited in TOR-2696 were this case.
2. **Has the parent shipped a release that permits the fix?** Then bump the parent, not the child.
3. **Only when the parent's range genuinely cannot reach a fixed version** does an override earn its place. Example: `copy-webpack-plugin@13.0.1` declares `serialize-javascript: ^6.0.2` and the fix is in 7.x — no resolution reaches it. Prefer a caret range (`^7.0.3`) over an exact pin, and never an unbounded `>=`.

Removing an override is not enough on its own: pnpm keeps a lockfile resolution that still satisfies its parent, so the package also needs an explicit `pnpm update`.

**If you are adding a third override in as many months, stop.** The question is not "which version do I pin" but "why is the lockfile never refreshed?" Renovate's `lockFileMaintenance` is the only mechanism that reaches dependencies absent from every `package.json` — Renovate will not create overrides for you, and the `transitiveRemediation` option that once did has been removed with no replacement. That job went silent between 2026-06-08 and 2026-08-07, which is what produced the override pile-up. Check the Dependency Dashboard (issue #3939) and the Mend job logs before adding a workaround.

## Key Patterns

### Data Model
- `Opiskeluoikeus` - Study right (main entity)
- `Suoritus` - Completion/achievement
- `Oppija` - Student (stored in external person registry)
- Use schema annotations for validation and documentation

### External Integrations
- **Oppijanumerorekisteri** - Person data
- **Organisaatiopalvelu** - Organization data
- **Koodistopalvelu** - Code values
- **ePerusteet** - Curriculum data
- **Virta** - University data
- **YTR** - Matriculation exam data

### Configuration
- Default config: `src/main/resources/reference.conf`
- Override with: `-Dconfig.resource=myconfig.conf`
- Local dev runs with mocked external services by default

## Common Tasks

### Adding a new field to schema
1. Update Scala case class in `schema/`
2. Run `make ts-types` to regenerate TypeScript types
3. Add database migration if persisted
4. Update validation if needed

### Updating localizations
Localizations are served by the Lokalisointipalvelu service, which gets its data from Tolgee.
The repo mirrors it in two files: `src/main/resources/mockdata/lokalisointi/koski.json` (all
languages — what local dev and tests read) and
`src/main/resources/localization/koski-default-texts.json` (Finnish only — the key list and the
fallback). Valpas has the same pair under `src/main/resources/valpas/`.

- **New key** → add it to `koski-default-texts.json`. On startup the app creates in the service
  any key present there but missing from it — but only with the Finnish text, publishing `sv`
  and `en` as empty strings. Swedish and English for a new key must therefore be added in Tolgee
  as well; writing them straight into the mockdata file works locally and is dropped by the next
  refresh.
- **Changed text on an existing key** → do **both**: commit the new value to the repo *and* edit
  it in Tolgee. Neither half is enough on its own. `LocalizationRepository.init` only *creates*
  keys missing from the service (`localization.create`) and never updates the value of one that
  already exists — so a repo-only edit never reaches production and the next
  `scripts/fetch_prod_localizations.sh` run silently reverts it, while a Tolgee-only edit leaves
  local dev and tests on the old text until someone refreshes.

See the header comment in `scripts/fetch_prod_localizations.sh` for what a refresh overwrites.

### Generating database documentation
```bash
make db-docs
make view-db-docs
```

## Important Notes

- Any data from the repositories I'm working with or my usage of agent tooling must not be shared with anyone
- Never commit files containing secrets (`.env`, credentials)
- Always before making a commit, ask for me to confirm and review the changes
- Always before making a commit, run Prettier if any frontend files have been modified
- TypeScript types in `web/app/types/fi/oph/koski/` are auto-generated - don't edit manually
- The application uses CAS for authentication in production
- Audit logging is required for all user actions involving personal data

## Maintaining these instructions

AGENTS.md (and the files it references, such as `documentation/todistus.md`) is a shared resource for the whole team and for future agent sessions. Keep it concise and readable for humans — not just for agents.

### Suggesting new entries

While working, if a non-obvious convention, gotcha, workflow, or piece of project context surfaces that would help **teammates and future agent sessions**, proactively suggest documenting it here. Good candidates:

- Pitfalls that took time to diagnose (ordering constraints, hidden coupling, etc.)
- Conventions that aren't obvious from reading the code
- Workflows that span multiple tools, repos, or services
- Decisions and their rationale where a future reader might otherwise undo them

Skip what is already obvious from the code, commit history, or other documentation.

### Where to put the entry

- **Short, cross-cutting rules**: add to AGENTS.md inside the most relevant existing section. Add new top-level sections sparingly.
- **Feature-specific or longer guidance** (more than a short paragraph): create or extend a dedicated file under `documentation/`, and add a one-line pointer under "Instructions for working with specific features" (see the `documentation/todistus.md` entry as the template).

### Style

- Concise and skimmable — bullets or short paragraphs.
- Written for a human teammate first; an agent will read it just fine either way.
- State the rule or fact first; add a brief reason only if it isn't obvious.
- Prefer updating an existing entry over adding a sibling one. Avoid duplication; link instead.

### Process

- Suggest the addition to the user before editing. Do not modify AGENTS.md or its referenced docs autonomously.
- In the commit body, mention the motivating context so future readers can judge whether the entry still applies.

## Instructions for working with specific features

- **Digital certificates**, digitaaliset todistukset, digitodistukset, see: `documentation/todistus.md`.
  - When adding new major features to certificates, also update the `documentation/todistus.md` file.

- **JSON Schema Viewer** (the `/koski/json-schema-viewer/` schema browser, a vendored & Koski-patched library), see: `documentation/json-schema-viewer.md`.
  - The vendored assets in `web/static/json-schema-viewer/` carry Koski-local patches not present upstream; edits require `make front` to take effect.

## GenAI tool usage guidelines

These guidelines apply to all GenAI tools (e.g. ChatGPT, Claude, IntelliJ IDEA GenAI features, Microsoft Copilot, Cursor) used in this project. AI assistants acting on behalf of the user must follow them.

### Purpose and benefits
GenAI tools are used to assist a human, not to act autonomously. Intended benefits:
- Speeding up routine tasks (e.g. generating automated tests)
- Helping locate and identify defects (e.g. explaining complex program logic in plain language)
- Enabling one-off operations (e.g. quickly learning a command-line debugging tool)
- Supporting professional development and motivation by enabling new ways of working

### Restrictions and risk management
- **Human review is mandatory.** Any feature that performs work or generates code directly into production without clear human review and testing is **strictly forbidden**.
- **Avoid experimental features.** GenAI tools evolve rapidly; do not use new, experimental capabilities before they have stabilized.
- **Protect confidential data.** Use tool settings that prevent confidential OPH data from being sent to model training pipelines.
- **Minimize IPR risk.** Enable tool features that mitigate IPR violations, such as automatic duplicate detection and blocking of GPL-licensed code suggestions.
- **Use judgment when adopting new tools.** Consult with other specialists before introducing tools not already established in the project.

### Summary for AI assistants
- Act as an assistant to the human, never as an autonomous executor of tasks.
- Ensure confidential content stays protected and IPR risks are minimized.
- Treat all AI-generated output as work that must be reviewed by the user with the same scrutiny as code from a junior developer — do not assume it is correct or safe to apply without confirmation.
- When a fix pattern repeats across tickets, question the pattern rather than applying it again — a recurring workaround usually means an upstream mechanism is broken.
