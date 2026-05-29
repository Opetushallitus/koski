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
make lint                # Run all linters (eslint + prettier + scalastyle)
make eslint              # JavaScript/TypeScript linting
make scalastyle          # Scala style check

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

### Frontend Tests
- **Mocha tests**: `web/test/` - run with `make fronttest`
- **Playwright tests**: `web/test/playwright/` - run with `make integrationtest`
- **Valpas Jest tests**: `valpas-web/test/` - run with `make valpas-fronttest-*`

### Test Users
Test users are defined in `src/main/scala/fi/oph/koski/koskiuser/MockUsers.scala`
Default test user: username "pää", password "pää"
Test students for Koski local development are defined in `src/main/scala/fi/oph/koski/henkilo/KoskiSpecificMockOppijat.scala`
Test students for Valpas local development are defined in `src/main/scala/fi/oph/koski/valpas/opiskeluoikeusfixture/ValpasMockOppijat.scala`

**Important:** New mock oppijat in `KoskiSpecificMockOppijat` and `ValpasMockOppijat` must be added at the **end** of the list, not in the middle. Oppija OIDs are generated sequentially, so inserting in the middle shifts all subsequent OIDs and breaks fixture data and UI tests.

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
Localizations are managed through the Lokalisointipalvelu service.
Add new localization keys for Koski in `src/main/resources/localization/koski-default-texts.json`
Add new localization keys for Valpas in `src/main/resources/valpas/localization/valpas-default-texts.json`

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
