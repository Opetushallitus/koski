# Koski - CLAUDE.md

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
- No `return` statements
- Class names: PascalCase
- Use `@Description`, `@KoodistoUri`, and other schema annotations for data models

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

If I have IntelliJ IDEA MCP configured: Use it for building, running and debugging backend tests.
If a specific configuration for the single test or suite is not available, ask me to create it manually.

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

- Any data from the repositories I'm working with or my usage of Claude Code must not be shared with anyone
- Never commit files containing secrets (`.env`, credentials)
- Always before making a commit, ask for me to confirm and review the changes
- TypeScript types in `web/app/types/fi/oph/koski/` are auto-generated - don't edit manually
- The application uses CAS for authentication in production
- Audit logging is required for all user actions involving personal data

## Instructions for working with specific features

- **Digital certificates**, digitaaliset todistukset, digitodistukset, see: `documentation/todistus.md`.
  - When adding new major features to certificates, also update the `documentation/todistus.md` file.
