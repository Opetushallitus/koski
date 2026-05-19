# Feasibility & design: continuous-incremental Raportointikanta updates

## Context

Today raportointikanta is rebuilt as a periodic batch job. Even the existing "incremental" mode (`GENERATE_RAPORTOINTIKANTA=update`) is really a blue/green load: it clones the entire `public` schema into an `etl` schema, applies queued changes, rebuilds every precomputed table, and atomically renames schemas. Above 50 000 queued updates it falls back to a full reload.

We want to know whether raportointikanta can instead be **continuously** updated, so that an opiskeluoikeus change shows up in the `r_*` tables within ~15 minutes. Targets:
- ≤ 200 000 OO updates / 24 h (~2.3/s avg, bursts higher)
- Production scale: ~5 M `r_opiskeluoikeus`, hundreds of millions of `r_osasuoritus`
- Reports keep running against the same raportointikanta RDS instance the loader writes to
- Schema-change / new-field rollouts still need a way to do a one-shot full rebuild or backfill

**Important topology fact**: raportointikanta already runs on its own RDS instance (CDK: `KoskiRaportointiDatabaseStack.ts`, datasource: `RaportointiDatabaseConfig` in `KoskiApplication.scala:98`). Reports already read from a different RDS than primary OLTP. So the design question is **"what do we add inside the existing raportointikanta RDS to make updates continuous"**, not "do we need a reporting database".

**Conclusion: feasible — and most of the building blocks already exist.** A continuous worker drains the existing `paivitetty_opiskeluoikeus` queue on primary Koski RDS (populated by the V84 trigger), pulls each affected OO over the network, runs the existing `buildKoskiRow` / `buildYtrRow` converters, and upserts `r_*` rows in-place on raportointikanta RDS. Per-OO failure handling, ONR fetches, precomputed-table maintenance, autovacuum tuning, and partitioning are the main new pieces.

**The 900 s ceiling on synchronous queries (Valpas / virkailija reports) is the single most important constraint.** It bounds worst-case bloat exposure to ~0.1 % of `r_osasuoritus` per query. Combined with the snapshot-clone pattern for Lampi exports (which exceed the ceiling), this means we don't need Aurora and probably don't need a live replica in Phase 1 — vanilla RDS plus autovacuum tuning, partitioning, and bounded backpressure-for-async-only is enough.

This document describes the recommended approach (network-read worker; see §1) and then documents the logical-replication variant (§2) as an optional upgrade for later, plus the rejected JSON-only alternative (§3).

---

## TL;DR — key decisions, implementation plan, open questions

### Key design decisions (recommended defaults)

1. **Continuous worker drains the existing `paivitetty_opiskeluoikeus` queue on primary Koski RDS** (populated by today's V84 trigger), runs `buildKoskiRow` / `buildYtrRow`, and writes `r_*` rows on raportointikanta RDS. **No logical replication** in Phase 1 — §2 documents that as an optional upgrade. (§1)
2. **Per-OO Slick transaction**: one OO update = one atomic transaction across all child `r_*` tables, so readers never see partial state. (§1.1)
3. **New ECS Fargate service** `raportointikantaContinuousWorkerTaskDefinition`, mirroring the existing `raportointikantaLoaderTaskDefinition` pattern. Single-slot `WorkerLeaseElector`. Tick every ~60 s. (§1.1)
4. **YTR change-capture trigger** missing today (V90 only handles aikaleima) — a new Flyway migration is on the critical path. (§1, "Existing pieces…")
5. **ONR-derived tables** (`r_henkilo`, `r_kotikuntahistoria`) — first-touch fetch inside the per-OO transaction + periodic `HenkiloRefreshScheduler` for ONR-side changes. Logical replication of the local `henkilo` table is NOT enough — ONR is the real source. (§1.1b)
6. **No blue/green schema swap** under continuous mode — writes go directly to `public`. Blue/green machinery preserved only for the full-reload escape hatch. (§1.2)
7. **Precomputed tables maintained per-OO / per-oppija** via a new `PrecomputedTable` trait + dirty-oppija mini-queue for cross-OO aggregates (PaallekkaisetOpiskeluoikeudet, Oppivelvollisuustiedot). Oppivelvollisuustiedot also gets a scheduled nightly full rebuild for config-driven changes. (§1.3)
8. **Lampi / Vipunen exports run on an ephemeral RDS snapshot-restore clone**, not on production raportointikanta RDS — keeps the 1h47m export from pinning autovacuum and bloating tables for Valpas. (§1.8)
9. **Backpressure scoped to async / queueable reports only** (massaluovutus, scheduled jobs). **Never applied to interactive Valpas** — UX requires consistent availability. The 900 s query-timeout ceiling already bounds bloat exposure to ~0.1 % of `r_osasuoritus` per query, which is acceptable. (§1.5pre, §1.5)
10. **Partition the heavy tables** for autovacuum scaling and query pruning — exact partition key is an **open investigation** (birth-year vs koulutustoimija vs two-level). (§1.6)
11. **Full regeneration keeps the existing blue/green machinery** — build into `etl` schema with the index-deferred optimization (~20 h, vs ~6 days for online), atomic swap at the end. Continuous mode keeps `public` fresh during the build. Pre-swap drain of long async queries (~15 min) makes the schema rename safe under the 900 s reader ceiling. Online rebuild on `public` is impractical at 5 M-OO scale because indices must stay inline. (§1.4 Flavor B)
12. **Column backfills tracked in a new `r_backfill_status` metadata table** so Lampi/Vipunen consumers can detect "this column isn't fully populated yet" rather than silently exporting NULLs. (§1.4 Flavor A)
13. **No live read replica in Phase 1**. With the 900 s ceiling + snapshot-clone for Lampi + partitioning, the bloat math works out. Add a replica later only if measurements show CPU/IOPS saturation. (§1.5a)
14. **No Aurora migration** as part of this project. The 900 s ceiling makes vanilla RDS streaming replicas (if ever needed) perfectly serviceable. Evaluate Aurora independently. (§1.5a)
15. **DBT not adopted** as either the primary transformation layer or the aggregate-table maintenance layer. Same SQL-can't-replicate-Scala blockers as §4 (schema-version drift, 18-variant ADT type matching), plus DBT's batch orientation conflicts with the per-OO transactional consistency property §1.1 relies on. Sibling Ovara uses DBT successfully for a similar reporting-DB pattern, but Ovara's source JSON is current-shape (from Lampi-siirtaja), its dw layer is temporally-accumulating, and its consistency contract is eventual-within-a-window — none of which fits Koski's interactive-Valpas audience. (§5)
16. **Per-oppija (not per-OO) atomic transaction is the hard consistency invariant**: end-user features must always show derived data computed from the same opiskeluoikeus generation as the visible base rows. Drives the per-oppija worker tick batching and the removal of the dirty-oppija mini-queue / second-pass logic. Relaxed for analysis paths only. (§1.1c)
17. **Greenfield reference design documented as §6, forward-looking only.** Variant A (pragmatic OPH-norm: oppija-document store for Valpas + DBT-built BI store) and Variant B (no-constraints ideal: Kafka + Debezium + DynamoDB / managed search + Snowflake / BigQuery). Today's §1 path doesn't strand work toward either variant — the continuous worker, change-queue, and snapshot-clone are directly reusable as the Valpas-side half of Variant A. (§6)

### Implementation plan (phased rollout)

Full execution roadmap with investigation dependencies, owner roles, and exit criteria lives in "Execution roadmap: investigations & development work" below. Phase summary:

0. **Phase 0** — Pre-work: PG version upgrade; kick off non-blocking investigations (I3, I4, I5, I6, I7, I10, I11, I12).
1. **Phase 1** — Foundations: config flag, queue extensions, YTR queue-population trigger Flyway migration, `r_backfill_status` table, load-limiter skeleton.
2. **Phase 2** — Migrate Lampi/Vipunen exports to the snapshot-clone pattern (§1.8). Prerequisite for safe continuous mode.
3. **Phase 3** — Build the continuous worker + `PrecomputedTable` trait + refresh schedulers + consistency checker. Preprod drift-watch ~2 weeks.
4. **Phase 4** — Cut over in production with nightly full reload as safety net.
5. **Phase 5** — Partition the heavy r_* tables (key per §1.6 / investigation I1).
6. **Phase 6** — *(Only if measured need)* live streaming replica (§1.5a).
7. **Phase 7** — *(Only if measured need)* §2 logical-replication upgrade.
8. **Phase 8** — *(Only if measured need)* Lampi-dump consumer for periodic refresh (§1.1b "Future option").

### Highest-impact open questions

(Full list in "Open decisions" at the bottom of this doc.)

- **Partition key** for `r_osasuoritus` / `r_paatason_suoritus` / `r_opiskeluoikeus` (§1.6, decision #3): birth-year, koulutustoimija, oppilaitos, or two-level. Needs profiling of real Valpas vs raportit query patterns.
- **Diff-aware per-OO updates** (§1.6a, decision #4): measure how often OO updates leave osasuoritus/aikajakso rows unchanged; decide whether to optimize away the dead tuples.
- **Lampi behavior during column backfills** (§1.4 Flavor A, decision #5): block until complete, skip in-progress columns, or sidecar-file approach?
- **§2 logical replication upgrade timing** (decision #11): defer until backfill cadence justifies; migration is purely additive from §1.
- **Aurora migration** (decision #12): evaluate on its own merits, not as part of this project.
- **Lampi as upstream for periodic refresh** (decision #12 / §1.1b "Future option" / Phase 8): when ONR refresh-load becomes operationally painful, move the bulk refresh path to Lampi-dump consumption; first-touch stays on live ONR. Worth a Lampi-team conversation now to confirm SLA / format / schema stability.
- **Postgres-side JSON → r_* transformation** (decision #13 / §4): wholesale rewrite rejected — schema-version drift is solved by Scala deserialization; type pattern-matching reproduces poorly in SQL; expected wins are small. **Adopted in narrow form**: GENERATED columns for trivial-extract additive columns in §1.4 Flavor A.

---

## Existing pieces we can reuse

Most of the per-OO mutation path already exists.

- **Change-capture trigger on primary** (`src/main/resources/db/migration/V84__create_paivitetty_opiskeluoikeus_trigger.sql`): Postgres `AFTER INSERT OR UPDATE` trigger on `opiskeluoikeus` writes every change (including soft-deletes / mitätöinti, which are UPDATEs) into `paivitetty_opiskeluoikeus`. Verify: `SuostumuksenPeruutusService.etsiPoistetut` reads from a separate table; check that the trigger path covers it or enqueue explicitly when that flow is used.
- **YTR change-capture: NOT installed today**. A `ytr_paivitetty_opiskeluoikeus` queue table exists (V88 migration) but no queue-population trigger is wired on `ytr_opiskeluoikeus` (V90 only adds the `aikaleima` trigger). Add the missing trigger (a YTR-side equivalent of V84) so the worker can drain YTR updates the same way.
- **Queue service**: `PäivitetytOpiskeluoikeudetJonoService.scala` — read/mark-processed/cleanup helpers all present. Extend with `yritykset` / `viimeisin_virhe` columns for the per-OO failure tracking the continuous worker needs.
- **Per-OO row builder**: `OpiskeluoikeusLoaderRowBuilder.buildKoskiRow(row, masterOid)` in `src/main/scala/fi/oph/koski/raportointikanta/OpiskeluoikeusLoaderRowBuilder.scala`. Pure function of one OO → all its R-table rows. Reuse as-is. **One change needed**: replace `OpiskeluoikeusLoaderRowBuilder.suoritusIds` (a shared in-process `AtomicLong`) with Postgres sequences owned by raportointikanta RDS (`r_paatason_suoritus_id_seq`, `r_osasuoritus_id_seq`, plus the analogous YTR `*_id_seq`s) so concurrent workers / restarts can't collide.
- **Per-YTR-OO row builder**: `OpiskeluoikeusLoaderRowBuilder.buildYtrRow(row, masterOid)` (same file, line 86). Targets `r_ytr_tutkintokokonaisuuden_suoritus`, `r_ytr_tutkintokerran_suoritus`, `r_ytr_kokeen_suoritus`, `r_ytr_tutkintokokonaisuuden_kokeen_suoritus`. Reuse as-is.
- **Per-OO upserts**: `RaportointiDatabase.updateOpiskeluoikeudet / updatePäätasonSuoritukset / updateOsasuoritukset / …` (RaportointiDatabase.scala lines 294–378). Already do delete-by-oid then insert-by-oid per R-table. Reuse. **Gap**: equivalent per-OO upsert methods do not currently exist for the YTR R-tables (only the bulk `loadYtrOsasuoritukset` at line 383). Add `updateYtrOsasuoritukset(...)` mirroring the Koski variants, plus a top-level `updateYtrOpiskeluoikeus(oid, …)` that wraps the four YTR R-tables in one transaction.
- **Mitätöity / poistettu partitioning**: `IncrementalUpdateOpiskeluoikeusLoader.updateBatch` (lines 64–76). The partition logic into `olemassaolevat / mitätöidyt / poistetut` is correct as-is; move it into the continuous worker.
- **Scheduling primitives**: `WorkerLeaseElector` (used by `RaportointikantaService`) and `GlobalIntervalScheduler` / `PerustiedotSyncScheduler` as templates for an interval worker.
- **Backpressure pattern**: `MassaluovutusService.systemIsOverloaded` (MassaluovutusService.scala:120) — gates work on `application.replicaDatabase.replayLag > kyselyt.backpressureLimits.maxDatabaseReplayLag` plus `DatabaseLoadLimiter.checkOverloading` (EBS byte balance, with `stopAt`/`continueAt` hysteresis). Drop-in template for raportointikanta backpressure.
- **ECS Fargate task pattern**: `KoskiApplicationStack.ts` already defines `raportointikantaLoaderTaskDefinition` and `ytrDataLoaderTaskDefinition` Fargate task definitions, with `StringParameter`-published ARNs and a Lambda trigger. The new continuous worker is one more task definition in the same style — see `/home/ahtiainen/koski-aws-infra-vm`.

---

## Execution roadmap: investigations & development work

This chapter sequences the open investigations called out elsewhere in this plan against the development phases, so a team of human + AI engineers can pick up the work in dependency order. It is the canonical execution plan; the TL;DR "Implementation plan" above is a one-liner summary of these phases.

### How to read it

Items prefixed **🅸** are **investigations** — they produce a decision or measurement, not code. Items prefixed **🅳** are **development tasks** — they produce code, migrations, or CDK.

Each phase has prerequisite investigations (gates) that must finish before the phase's development tasks start. Investigations themselves can mostly run in parallel from day 1.

### Investigation backlog

| ID | Investigation | Source | Gates phase | Deliverable | Effort | Owner role |
|---|---|---|---|---|---|---|
| I1 | Partition-key profiling for `r_osasuoritus` / `r_paatason_suoritus` / `r_opiskeluoikeus`: representative Valpas and kt/oppilaitos-scoped raportit query plans against production-like data; per-partition row counts under birth-year, kt, oppilaitos, two-level. | §1.6, decision #3 | 5 | Decision memo: chosen partition key + bucket plan (1-year vs 2-year vs 5-year for birth-year), per-partition row-count estimates, EXPLAIN profiles. | 1–2 weeks | DB-savvy engineer |
| I2 | How often do OO updates actually change `r_osasuoritus` / aikajakso content vs leave it identical | §1.6a, decision #4 | 3 (worker write strategy) | Short report + chosen worker write strategy: today's DELETE-all+INSERT-all (Option D), per-row diff (Option A), or stable composite PKs (Option C). | A few days | Backend eng |
| I3 | Lampi-team conversation: dump publication SLA, format (Parquet/CSV/JSON-lines), schema stability, turvakielto handling, per-source granularity (ONR vs Organisaatio vs Koodisto), audit / authoritative-source semantics, inbound-consumption permissions. | §1.1b "Future option", decision #12 | 8 | Meeting notes + go/no-go criteria for Phase 8. Open inbound-S3 permissions question. | 1 conversation + follow-up | Tech lead |
| I4 | V84 trigger-coverage audit: enumerate every code path that mutates `opiskeluoikeus` (including `SuostumuksenPeruutusService.etsiPoistetut` and YTR-side writes) and verify the trigger fires for each. | "Existing pieces…", decision #6 | 1 | Audit doc; if gaps found, explicit `jono.lisää(oid)` calls (or trigger fix) land in Phase 1. | A few days | Backend eng |
| I5 | Lampi-during-backfill behavior: (a) block until complete, (b) skip in-progress columns, (c) sidecar file listing in-progress columns. | §1.4 Flavor A, decision #5 | 1 (`r_backfill_status` consumer semantics) | Behavior decision recorded; orchestrator pseudocode for the chosen flavor. | 1 conversation | Tech lead + Lampi/Vipunen consumers |
| I6 | ONR-refresh staleness target — confirm "≤ 24 h" (today's nightly-batch baseline) or set tighter SLA for `HenkiloRefreshScheduler`. | §1.1b, decision #9 | 3 | Numeric staleness target (e.g. 4 h / 12 h / 24 h). | 1 conversation | Tech lead |
| I7 | Long-report routing audit: every currently-synchronous report whose p95 exceeds threshold; decide for each: stays-sync / async-queued / snapshot-clone. | §1.5, decision #2 | 3 (backpressure plumbing) | Mapping table: report → execution mode. Requires slow-query log access. | A few days | Backend eng + product |
| I8 | ONR connectivity from new continuous-worker Fargate task — SG / IAM / Secrets Manager wiring for OAuth2 credentials, mirroring main Koski task. | §1.1b, decision #10 | 3 | CDK diff in `koski-aws-infra-vm` ready to merge with Phase 3. | A few days | Platform eng |
| I9 | YTR burst absorption — sample observed `ytr_opiskeluoikeus` insertion rate from `YtrDownloadService`; confirm tick + batch settings drain within the 15-min target. | §1.1, decision #8 | 3 | Confirmed `batchSize` / tick cadence for YTR path. | A few hours | Backend eng |
| I10 | Aurora evaluation — independent track. Cost, ops appetite, read-scaling needs once 900 s ceiling is in place. | §1.5a, decision #14 | none | Standalone go/no-go memo, not gating this project. | ~1 week | Platform eng / arch |
| I11 | Lampi/Vipunen consumers' snapshot expectations — confirm snapshot-clone model is acceptable replacement for "uploaded after every full load". | §1.8, decision #7 | 2 | Confirmation + any "authoritative-as-of" contract changes. | 1 conversation | Tech lead + Lampi consumers |
| I12 | Worker hosting: dedicated `raportointikantaContinuousWorkerTaskDefinition` Fargate task vs running inside main Koski Fargate. | "Existing pieces…" / CDK, decision #1 | 3 | CDK decision + task definition spec. | A few hours | Platform eng |
| I13 | §2 (logical replication) timing — once continuous mode is live, observe schema-additive backfill cadence + analyst-query needs for raw `opiskeluoikeus.data` on raportointikanta RDS. | §2, decision #11 | 7 (conditional) | Ongoing observation; threshold criteria for triggering Phase 7. | Continuous | Tech lead |
| I14 | Postgres-side transformation beyond GENERATED columns — only revisit if Koski adopts a strict, version-stamped JSON contract on `opiskeluoikeus.data` (invalidates rejection reason #1 of §4). | §4, decision #13 | none | No work unless trigger condition fires. | n/a | n/a |

### Development phases

#### Phase 0 — Pre-work (start immediately, no production behavior change)

**Investigations to kick off**: I3, I4, I5, I6, I7, I10, I11, I12.

**Tasks**:
- 🅳 PG version upgrade (PG 13 → 15 or 16) on raportointikanta RDS, scheduled before any partitioning work (§1.10). Not strictly required for Phase 1 but worth doing first because it eases later partitioning + autovacuum tuning and is independently good hygiene.

**Exit criteria**: I4 + I5 produce answers (those gate Phase 1); PG upgrade scheduled or already done.

#### Phase 1 — Foundations

**Prerequisites**: I4 (V84 trigger coverage), I5 (Lampi-during-backfill behavior).

**Tasks**:
- 🅳 Config flag `raportointikanta.continuousMode` (default `false`). [`reference.conf`, `KoskiApplication.scala`]
- 🅳 Flyway migration on primary Koski RDS: extend `paivitetty_opiskeluoikeus` with `yritykset int default 0` and `viimeisin_virhe text`.
- 🅳 Flyway migration on primary Koski RDS: add the missing `ytr_paivitetty_opiskeluoikeus` queue-population trigger on `ytr_opiskeluoikeus` (mirror of V84; today only V90's `aikaleima` trigger exists).
- 🅳 Flyway migration on raportointikanta RDS: create `r_backfill_status` table per §1.4 Flavor A.
- 🅳 If I4 surfaced gaps: add explicit `jono.lisää(oid)` calls in offending services (e.g. `SuostumuksenPeruutusService.etsiPoistetut`).
- 🅳 Skeleton `RaportointikantaLoadLimiter` class + config keys under `raportointikanta.backpressureLimits` (no wiring into `MassaluovutusScheduler` yet).
- 🅳 Extend `PäivitetytOpiskeluoikeudetJonoService` with `yritykset` / `viimeisin_virhe` read/write helpers.

**Exit criteria**: All migrations green in CI + staging; no production behavior change.

#### Phase 2 — Migrate Lampi/Vipunen exports to the snapshot-clone pattern

**Prerequisites**: I11 (consumer acceptance). Can run in parallel with Phase 1.

**Tasks**:
- 🅳 New `LampiSnapshotScheduler` orchestrator: Step Functions state machine (preferred for visibility) or long-running Fargate task implementing snapshot → restore → exports → cleanup per §1.8.
- 🅳 CDK in `koski-aws-infra-vm`: EventBridge schedule rule, IAM for `rds:CreateDBSnapshot` / `rds:RestoreDBInstanceFromDBSnapshot` / `rds:DeleteDBInstance`, parameter group reused from production raportointikanta RDS (so `aws_s3` extension stays enabled on the clone).
- 🅳 End-to-end validation: real `r_osasuoritus` export to a dev/staging Lampi bucket against a real snapshot-restored temp instance.
- 🅳 Cut over the production Lampi/Vipunen export to the new flow; retire the nightly direct export from production raportointikanta RDS.

**Exit criteria**: Production Lampi exports run entirely on the temp clone for 2 consecutive weeks with no consumer complaints. The "long-running export on production raportointikanta RDS" workload class disappears — the prerequisite that makes continuous mode safe.

#### Phase 3 — Continuous worker (the meat of the project)

**Prerequisites**: Phase 2 production-deployed. I2, I6, I7, I8, I9, I12.

**Tasks** (Scala):
- 🅳 `RaportointikantaContinuousWorker.scala`: tick (60 s) = dedupe queue rows → SELECT OOs from primary (preserve `PäivitettyOpiskeluoikeusLoader.scala:33–45` replica-lag-aware pattern) → `buildKoskiRow` → per-OO Slick transaction across all r_* tables → mark queue rows processed. Per-OO try/catch with exponential backoff against `yritykset` / `viimeisin_virhe`; ~10-retry DLQ.
- 🅳 Replace `OpiskeluoikeusLoaderRowBuilder.suoritusIds` (shared in-process `AtomicLong`) with Postgres sequences owned by raportointikanta RDS (`r_paatason_suoritus_id_seq`, `r_osasuoritus_id_seq`, plus YTR analogs). One Flyway migration to create the sequences + seed from current max IDs.
- 🅳 Per-OO YTR upsert path: add `RaportointiDatabase.updateYtrOpiskeluoikeus(oid, …)` plus `updateYtrTutkintokokonaisuudenSuoritukset / updateYtrTutkintokerranSuoritukset / updateYtrKokeenSuoritukset / updateYtrTutkintokokonaisuudenKokeenSuoritukset` mirroring the Koski variants in `RaportointiDatabase.scala` (lines 294–378).
- 🅳 `PrecomputedTable` trait + per-table implementations, all invoked once per oppija inside the per-oppija transaction (§1.1c):
  - `OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset`: delete-by-oo + insert per affected OO, inside the oppija transaction.
  - `PaallekkaisetOpiskeluoikeudet`: cross-OO over one oppija — recompute from the oppija's r_* rows post-update, upsert, all inside the oppija transaction.
  - Eight Lukio-kertymä tables: delete-by-oo + insert per affected OO, inside the oppija transaction.
  - `Oppivelvollisuustiedot`: recompute for the oppija inside the oppija transaction; **plus** a nightly full rebuild scheduler for config-driven changes (rajapäivät etc.). No separate `dirty_oppija` mini-queue.
- 🅳 `HenkiloRefreshScheduler` on raportointikanta RDS, cadence per I6. Walks `r_henkilo` in chunks, re-fetches `LaajatOppijaHenkilöTiedot` + kuntahistoria, handles turvakielto state transitions by atomically moving rows between `public` and `confidential` schemas. First-touch path stays inline in the per-OO transaction.
- 🅳 Move `OrganisaatioLoader` / `KoodistoLoader` / `OppivelvollisuudenVapautusLoader` out of `loadRestAndSwap` into independent periodic schedulers running against raportointikanta RDS.
- 🅳 `RaportointikantaConsistencyChecker.scala`: nightly sample-OO diff between continuous `r_*` and a freshly-rebuilt staging schema; alerts on any drift.
- 🅳 If I2 chose Option A or C: implement the diff-aware update path. Otherwise: keep today's DELETE-all + INSERT-all (Option D).
- 🅳 Wire `RaportointikantaLoadLimiter` into `MassaluovutusScheduler.runNext` for async-only backpressure. Source `workerQueueLagSeconds` + the existing `DatabaseLoadLimiter.checkOverloading` pattern.
- 🅳 If I7 reclassified any synchronous reports as async-queued: implement the routing change in `RaportitServlet` (queue via massaluovutus instead of running inline). **Never apply backpressure to interactive Valpas.**
- 🅳 Enrich `RaportointikantaStatusServlet` + `/paivitysaika` per §1.7 (`loadType`, `workerQueueLagSeconds`, `workerQueueDepth`, `precomputedTablesAsOf`, `lastFullReloadCompletedAt`). Keep the old field for UI backwards compatibility.

**Tasks** (CDK / infra):
- 🅳 New `raportointikantaContinuousWorkerTaskDefinition` Fargate task per I12, mirroring `raportointikantaLoaderTaskDefinition` in `KoskiApplicationStack.ts`. Connects to both primary Koski RDS (queue + OO reads) and raportointikanta RDS (writes). Apply I8's SG / IAM / secrets diff for ONR reachability.
- 🅳 `WorkerLeaseElector` lease table on raportointikanta RDS for single-slot guarantee.

**QA / rollout**:
- 🅳 Enable `raportointikanta.continuousMode = true` in QA / preprod. Run nightly full batch alongside as safety net.
- 🅸 ~2 weeks of drift-monitoring with `RaportointikantaConsistencyChecker`.

**Exit criteria**: Consistency checker green for 2 weeks in preprod; queue lag p95 < 5 min on realistic load; ready to flip in production.

#### Phase 4 — Cut over in production

**Prerequisites**: Phase 3 done.

**Tasks**:
- 🅳 Set `raportointikanta.continuousMode = true` in production.
- 🅳 Reduce nightly full reload to weekly, then monthly, as confidence builds (initially run continuous + nightly as a redundant pair).
- 🅸 Operational watch: consistency-checker, autovacuum metrics, p95 query latency, queue lag, ONR retry-envelope trips, autovacuum dead-tuple ratio on `r_osasuoritus`.

**Exit criteria**: Queue lag p95 < 15 min for 30 days; zero consistency drift; ready to start I1's profiling work against real production traffic.

#### Phase 5 — Partition the heavy r_* tables

**Prerequisites**: Phase 4 in production (so I1 can profile against real workloads); I1 produces a partition key.

**Tasks**:
- 🅳 Denormalize partition key onto every partitioned r_* table. For birth-year: populate `oppija_syntymävuosi` from `r_henkilo` at write time (Scala worker change + Flyway migration). For kt/oppilaitos: column already on `r_opiskeluoikeus`; mirror onto child tables.
- 🅳 Re-create `r_osasuoritus` / `r_paatason_suoritus` / `r_opiskeluoikeus` (plus precomputed tables that share the key) as partitioned tables under the chosen scheme. Easiest path: full reload into a partitioned `etl` schema (Flavor B), atomic swap, continuous worker catches up.
- 🅳 Update worker per-OO DELETE to include partition-key constraint so Postgres can prune (per §1.6 implementation notes). Pass the partition-key value alongside the oid in upsert calls.
- 🅳 Handle rare cross-partition moves under kt/oppilaitos partitioning: explicit DELETE-old + INSERT-new in the per-OO transaction (don't rely on UPDATE).
- 🅳 Per-table autovacuum tuning per §1.9: `autovacuum_vacuum_scale_factor = 0.01`, `autovacuum_vacuum_cost_limit ≈ 2000`, `autovacuum_max_workers ≥ 5`.
- 🅳 Retire the blue/green machinery for routine paths once partitioning is live; retain it only for destructive schema changes (Flavor C of §1.4).

**Exit criteria**: Per-partition autovacuum p95 < 10 min; partition pruning visible in EXPLAIN for representative Valpas + kt-scoped report queries; latency improvement measured.

#### Phase 6 (conditional) — Live streaming replica

**Trigger**: post-Phase-5 measurements show CPU/IOPS sustained > 70 % during peak Valpas hours, OR p95 Valpas latency degrading under concurrent long-query load, OR autovacuum running behind dead-tuple threshold.

**Tasks**: Provision a physical streaming replica with `hot_standby_feedback=off` and `max_standby_streaming_delay = 1000 s`. Route Valpas / synchronous virkailija reads to the replica. See §1.5a.

#### Phase 7 (conditional) — Upgrade to §2 (logical replication)

**Trigger**: I13 observes schema-additive backfills happening more than a few times a year, OR migration to 15 M+ OOs makes backfill cost on primary OLTP a recurring problem, OR analyst-query / audit need for raw `opiskeluoikeus.data` on raportointikanta RDS.

**Tasks**: See §2 migration steps. Migration is purely additive — same continuous worker, same precomputed-table logic, only the SELECT path and queue location move.

#### Phase 8 (conditional) — Lampi-dump consumer for periodic refresh

**Trigger**: ONR retry-envelope tripping recurring in production logs, OR OPH platform team flags Koski's refresh scan as a co-tenant problem. **Prerequisite**: I3 outcome confirms dump SLA / format / turvakielto handling are workable.

**Tasks**: New `HenkilöLampiSource` / `OrganisaatioLampiSource` S3 readers behind a config-selected interface (`raportointikanta.henkilo.source = "onr" | "lampi"`); cross-account S3 IAM in `koski-aws-infra-vm`; first-touch path stays on live ONR (hybrid pattern). See §1.1b "Future option".

### Dependency sketch

```
Phase 0 ───────────┐
                   ├──> Phase 1 ──> Phase 2 ──> Phase 3 ──> Phase 4 ──> Phase 5
I3, I4, I5 ────────┘                              ▲                       ▲
                                                  │                       │
                                       I2, I6, I7, I8, I9,                I1
                                       I11, I12                           │
                                                                          │
I10 (Aurora)      — independent track, no gate on this project            │
I13 (§2 timing)   — ongoing observation post-Phase 4 → may trigger Phase 7│
I14 (Postgres-side transformation) — only revisited if JSON contract changes
                                                                          ▼
                                          (Phase 6 / 7 / 8 all conditional on
                                           measured production signals)
```

Phases that can run in parallel:
- Phase 1 + Phase 2 are independent migrations; Phase 2 doesn't strictly require Phase 1 to land first.
- All I-track investigations can start day 1.
- I1's instrumentation work can begin during Phase 3 even though it gates Phase 5.

### Roles: human vs AI work split

- **Stakeholder conversations** (I3, I5, I6, I7 product portion, I11): human-led; schedule meetings, capture decisions, record them back into this doc.
- **Code audits** (I4, I7 slow-query side): mixed — AI agent can enumerate code paths and trigger sources; human reviews and decides edge cases.
- **DB profiling and measurement** (I1, I2, I9): AI agent can produce the instrumentation scripts, queries, and a draft analysis; human-driven on production access and final decision.
- **Infrastructure review** (I8, I10, I12): platform engineer review of CDK; AI agent can draft the diff.
- **Scala / Flyway / CDK implementation tasks** (most 🅳 items): AI-assistable end-to-end with human review on PR.
- **Production deploys, secret management, traffic flips** (Phase 4 cutover, Phase 2 cutover, Phase 5 schema swap, conditional Phase 6 replica provisioning): human-driven.

### Success / exit criteria recap

| Milestone | Measurement |
|---|---|
| Phase 2 complete | Lampi exports run on temp clone for 2 weeks, no consumer complaints. |
| Phase 3 complete (preprod) | `RaportointikantaConsistencyChecker` zero drift over 2 weeks; queue lag p95 < 5 min. |
| Phase 4 complete (production) | Queue lag p95 < 15 min for 30 days; zero consistency drift. |
| Phase 5 complete | Per-partition autovacuum p95 < 10 min; partition pruning visible in EXPLAIN; report latency improvement measured. |
| Phase 8 complete (if pursued) | Zero ONR retry-envelope trips per week for r_henkilo refresh. |

---

## §1. Recommended approach: continuous worker reading from primary over the network

### Topology

- **Primary Koski RDS** (existing, unchanged): owns `opiskeluoikeus`, `ytr_opiskeluoikeus`, `henkilo`, `opiskeluoikeushistoria`, and the `paivitetty_opiskeluoikeus` queue maintained by the V84 trigger. Purely OLTP, plus the queue table.
- **Valpas RDS** (existing, unchanged): owns `oppivelvollisuudesta_vapautetut`.
- **Raportointikanta RDS** (existing, gains the continuous worker as a writer): already owns `r_*` tables and the precomputed tables. No new tables besides eventual partitioning DDL and worker state.
- **Continuous worker**: runs as a new ECS Fargate service `raportointikantaContinuousWorkerTaskDefinition` in CDK, alongside the existing `raportointikantaLoaderTaskDefinition`. Connects to **both** primary Koski RDS (read OOs + drain queue) and raportointikanta RDS (write `r_*`). Single-slot `WorkerLeaseElector` (lease stored on raportointikanta RDS) ensures only one task processes at a time. Also reaches out to ONR for first-touch henkilö/kotikuntahistoria fetches — see §1.1b.
- **Periodic refresh schedulers** on raportointikanta RDS: `HenkiloRefreshScheduler` (ONR), plus reuse of `OrganisaatioLoader` / `KoodistoLoader` / `OppivelvollisuudenVapautusLoader` running on a slow cadence — see §1.1b.
- **Reports**: synchronous reports (Valpas, RaportitServlet) already read from raportointikanta RDS via `application.raportointiDatabase`. No change to the datasource; reports become continuous-fresh transparently. Massaluovutus-style async reports also already point there.
- **Backpressure**: long async reports (massaluovutus, scheduled jobs) are gated on worker-queue lag (§1.5). Synchronous Valpas/virkailija queries are **not** backpressured (§1.5).

Result: minimal new infrastructure. Worker is one new Fargate task; everything else is existing components running in continuous mode rather than nightly batch.

### Why this is much smaller scope than I initially framed it

- **No new RDS instance.** Already there.
- **No initial sync of `r_*` data.** Already populated by the nightly batch; continuous worker just keeps it fresh from that point on.
- **No new datasource wiring.** `application.raportointiDatabase` already exists.
- **No "decommission primary-side r_*"** — there isn't any. (The blue/green `etl → public` rename happens **inside** raportointikanta RDS, not on the primary.)
- **No changes to report code.** Reports keep reading from `application.raportointiDatabase`.
- **No logical replication, publications, or subscriptions** (see §2 for that variant).

The real work is: build the continuous worker; close the YTR-trigger gap; handle precomputed tables incrementally; tune autovacuum and add partitioning on raportointikanta RDS; add async-only backpressure; replace nightly Lampi export with a snapshot-clone flow.

### §1.1 Continuous worker

The worker drains the existing `paivitetty_opiskeluoikeus` queue on primary Koski RDS:

The tick:
1. Read up to `batchSize = 500` unprocessed rows from `paivitetty_opiskeluoikeus` on primary Koski RDS. Dedupe by `opiskeluoikeus_oid` keeping the latest (collapses bursts) and **group the resulting rows by `oppija_oid`** so the rest of the tick processes one oppija at a time (see §1.1c for why; the queue carries `oppija_oid` as a denormalized column added by the same Flyway migration that adds `yritykset` / `viimeisin_virhe`).
2. SELECT the affected OO rows from `opiskeluoikeus` on primary (same pattern as today's loader's `PäivitettyOpiskeluoikeusLoader`).
3. For each **oppija batch** (all changed OOs for that oppija together), run `buildKoskiRow` for each OO, then in **one Slick transaction per oppija** on raportointikanta RDS: upsert all R-tables for every changed OO; then re-read the oppija's OOs from r_* (post-update inside the same transaction); then recompute and upsert that oppija's derived aggregates (PaallekkaisetOpiskeluoikeudet, Oppivelvollisuustiedot, the Lukio-kertymä family, OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset). The whole transaction commits as one — reports never observe a half-updated state across the oppija's r_* rows or between r_* and derived aggregates (§1.1c).
4. Mark every queue row processed for that oppija on primary (idempotent since upserts are idempotent — if step 3 succeeded but the queue update failed, retry re-does the same upserts harmlessly).

Cadence: tick every **60 s**. With 200 000 updates/day average and batch=500 the worker is mostly idle, but bursts of 5 000+ updates from a virkailija mass-import drain within a few ticks. The 15-min target leaves plenty of headroom.

YTR: same worker, same tick, separate queue (`ytr_paivitetty_opiskeluoikeus` on primary, populated by a new YTR-side trigger — see Existing pieces). The YTR R-tables have no precomputed dependencies, so YTR processing is just the normalized-table path.

Failure handling: per-OO try/catch. Add `yritykset` and `viimeisin_virhe` columns to the queue tables on primary. Exponential backoff via the existing `aikaleima` filter; hard cap (~10 retries) → DLQ table + alert. Today's loader fails the whole batch — unacceptable for a 24/7 worker.

The existing replica-lag-aware pattern from `PäivitettyOpiskeluoikeusLoader.scala:33–45` (the loader cross-checks the master vs the replica to avoid processing rows the replica hasn't seen yet) should be preserved verbatim — it handles the case where the worker reads `opiskeluoikeus` from the primary replica but the queue lives on primary master, and replica lag could otherwise cause "OO not yet visible" errors.

### §1.1b ONR-derived tables (`r_henkilo`, `r_kotikuntahistoria`) and other external data

`r_henkilo` and `r_kotikuntahistoria` (plus the confidential-schema variant of the latter) are **not** derived from a Postgres table on the primary — they come from external HTTP calls to **Oppijanumerorekisteri (ONR)**. Today `HenkilöLoader.loadHenkilöt` (HenkiloLoader.scala) calls:

- `opintopolkuHenkilöFacade.findMasterOppijat(batchOids)` — fetches `LaajatOppijaHenkilöTiedot` per oppija oid (name, hetu, syntymäaika, kuolinpäivä, äidinkieli, kansalaisuus, turvakielto, kotikunta, yksilöity, masterOid). Populates `r_henkilo`.
- `opintopolkuHenkilöFacade.findKuntahistoriat(oids = batchRows.map(_.masterOid), turvakiellolliset = …)` — fetches kotikuntahistoria per master_oid. Run twice (with and without turvakielto) and populates `r_kotikuntahistoria` in the `public` schema (turvakielto excluded) and in the `confidential` schema (turvakielto included).

Similarly, `r_organisaatio` (`OrganisaatioLoader`) and `r_koodisto_koodi` (`KoodistoLoader`) come from external services (Organisaatiopalvelu, Koodistopalvelu), not Postgres tables. `r_oppivelvollisuudesta_vapautus` is sourced from the `oppivelvollisuudesta_vapautetut` table on Valpas RDS via `OppivelvollisuudenVapautusLoader`.

#### Design

Decouple the OO update flow from the ONR refresh flow — they have different change cadences and different external rate limits:

1. **First-touch henkilö fetch** (inside the per-OO transaction). When a queue tick processes an OO whose `oppijaOid` is not yet in raportointikanta RDS's `r_henkilo`, the worker calls `findMasterOppijat([oid])` + `findKuntahistoriat([master_oid])` for that oppija, builds the rows, and upserts them in the same transaction as the `r_*` writes. Guarantees no OO row references a missing `r_henkilo`. Batched per tick (collect all unknown oppija_oids in the batch, do one ONR call) to keep ONR-API call volume bounded.
2. **Periodic henkilö refresh** (separate scheduler on raportointikanta RDS, `HenkiloRefreshScheduler`). Runs e.g. nightly or every N hours. Walks `r_henkilo` in chunks, re-fetches `LaajatOppijaHenkilöTiedot` + kuntahistoria, upserts. Catches ONR-side changes that aren't tied to any OO change (name changes, kuolinpäivä, kotikuntahistoria fixes, turvakielto status changes, yksilöinti, etc.). Pace controlled by `OppijanumeroRekisteriClientRetryStrategy`.
3. **Targeted invalidation hook** (optional). If we ever know that an oppija's ONR data has changed, enqueue that oppija into a `dirty_henkilo` mini-queue that the refresh scheduler drains ahead of its regular sweep. Not required for v1 — the periodic refresh is sufficient.
4. **Turvakielto state transitions**: when ONR's `turvakielto` flag changes for an oppija, the kotikuntahistoria rows must move between the `public` and `confidential` schemas. The refresh scheduler handles this: re-fetch both turvakielto variants, replace both schemas' rows for that oppija in one transaction. Same as today's loader.

#### ONR-API operational concerns

- **Rate limits / latency**: ONR is a shared OPH service. Today's loader uses `OppijanumeroRekisteriClientRetryStrategy` with retries up to 10 attempts, 5-min max wait between retries, 3-min retry timeout (RaportointikantaService.scala:138–143). The continuous worker should use the same retry strategy. ONR outages must not block the OO-side queue — if ONR is unreachable, the worker either (a) defers henkilö-dependent upserts and processes the rest, or (b) blocks the affected oppija's OO updates until ONR recovers. Recommend (a): allow `r_opiskeluoikeus.oppija_oid` to point at an `r_henkilo` row that's marked stale (`r_henkilo_aikaleima < threshold`), with a status indicator so reports can surface "henkilö data is N hours old".
- **Burst protection**: avoid letting a backlog drain create a thundering herd of ONR calls. The worker tick already batches; cap batch size at the existing `BatchSize=1000` from `HenkilöLoader`.
- **Initial state**: today's `HenkilöLoader.loadHenkilöt` already populates these tables. After continuous mode is enabled, the continuous worker handles new oppijas (first-touch) and the refresh scheduler handles updates. No re-seeding needed.

#### Other external data

- **`r_organisaatio`** (from Organisaatiopalvelu) — reuse `OrganisaatioLoader` on raportointikanta RDS, run periodically (org changes are rare).
- **`r_koodisto_koodi`** (from Koodistopalvelu) — reuse `KoodistoLoader` on raportointikanta RDS, run periodically (koodisto changes are rare).
- **`r_oppivelvollisuudesta_vapautus`** — reuse `OppivelvollisuudenVapautusLoader` on raportointikanta RDS, reading from Valpas RDS over the network on a slow cadence. The source table is small and changes rarely.

For all three, today's `loadRestAndSwap` (RaportointikantaService.scala:252) runs them in sequence after the OO load. Under continuous mode, schedule them as independent intervals so they can be tuned separately.

#### Future option: consume ONR / Organisaatio dumps from Lampi instead of live HTTP for periodic refresh

Lampi is treated as an *export* destination in this plan (§1.8), but the upstream OPH services (ONR, Organisaatiopalvelu, Koodistopalvelu) also *publish* their own data dumps into Lampi with roughly 1 h latency. For the **periodic-refresh** path of `r_henkilo` / `r_kotikuntahistoria` / `r_organisaatio` — the millions-of-OIDs scan that catches ONR-side changes — a Lampi consumer would replace the heaviest external-API load the loader generates today.

Why it's attractive on the refresh path:
- Today's full refresh fan-outs through `findMasterOppijat(batch=1000)` + `findKuntahistoriat`, wrapped in a 10-retry / 5-min-max-wait envelope (`OppijanumeroRekisteriClientRetryStrategy` — the existence of that envelope is the explicit acknowledgement that ONR availability is the loader's biggest operational risk). Replacing this with one S3 read + bulk upsert removes the co-tenant load on ONR entirely.
- Lampi's ~1 h staleness is fine here. Today's nightly batch is up to 24 h stale; even hourly Lampi consumption is a strict improvement.
- Failure mode is clean: if Lampi dump is unavailable, skip this refresh cycle and try again next interval. No load on ONR. (Today's failure mode is partial fan-out → half-stale state.)
- Same story for `r_organisaatio` — replaces `findAllHierarkiat` with an S3 read.
- `r_koodisto_koodi` is too small to bother (thousands of rows, 8 koodistos); leave on live HTTP.

Why this **does not replace the first-touch path** (the mechanism-1 inline fetch above):
- A newly-created oppija in Koski appears in ONR immediately (Koski creates them there). Lampi sees them ~1 h later. First-touch via Lampi would miss recent oppijas and have to fall back to live ONR anyway.
- First-touch is a single-OID lookup that ONR handles in ms; pulling a multi-GB Lampi dump to satisfy one OID is the wrong shape.
- Turvakielto state transitions: the confidential-schema kotikuntahistoria still likely needs live ONR — TBD by Lampi-team confirmation whether the turvakielto variant is published at all.

Architecture if pursued: hybrid. Keep `OpintopolkuHenkilöFacade` HTTP for first-touch + debug; replace the periodic refresh scheduler's data source with a new `HenkilöLampiSource` / `OrganisaatioLampiSource` S3 reader behind a config-selected interface (`raportointikanta.henkilo.source = "onr" | "lampi"`). Downstream upsert code in `RaportointiDatabase.*` stays unchanged because the row shapes don't change. Cross-account S3 read IAM needs adding in `/home/ahtiainen/koski-aws-infra-vm` (mirror of the upload side); Lampi-side bucket policy needs Koski's raportointikanta task role on the read path.

**Operational unknowns to confirm with the Lampi / OPH platform team before committing:**
- Dump publication SLA. "About 1 h late" — guarantee, observed average, or vendor target? Retroactive correction behavior?
- Dump format and schema stability. Parquet, CSV, JSON-lines? Documented and version-stable?
- Turvakielto handling. Is the confidential-side data exposed at all in the Lampi dumps?
- Per-source granularity (ONR vs Organisaatio vs Koodisto) — same cadence? same bucket scheme?
- Authoritative-source semantics for audit / compliance — raportointikanta is by design a derived store, so 1 h staleness is fine, but worth a one-line confirmation.

**Recommendation: defer to a later phase, not Phase 1.** Continuous mode ships fine on live HTTP. Re-evaluate when (a) the ONR retry envelope starts tripping in production logs on a recurring basis, or (b) the OPH platform team flags Koski's refresh scan as a co-tenant problem. Slot as Phase 8 in the rollout list above (Execution roadmap). Cheap option-value step worth doing now: open a conversation with the Lampi team about inbound consumption permissions and dump-schema documentation so the answers are in hand if/when we pursue this.

### §1.1c Per-oppija atomic consistency invariant (end-user requirement)

**Invariant**: an end-user feature reading raportointikanta for a single oppija (Valpas, virkailija interactive views) must never observe a state where the visible `r_opiskeluoikeus` rows reflect one generation of underlying opiskeluoikeus data but the derived aggregates for that oppija (`PaallekkaisetOpiskeluoikeudet`, `Oppivelvollisuustiedot`, the Lukio-kertymä tables, `OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset`) reflect an earlier generation. Within one oppija the data must be a coherent snapshot — informally, characterizable as "as of KOSKI data-transfer time T for this oppija".

**Scope**: applies to all r_* and derived tables read by end-user-facing features. Does **not** apply to Lampi/Vipunen exports or batch analytics, which are explicitly allowed to be eventually-consistent at the per-row level. Also does not apply to globally-config-driven changes (rajapäivät updates affecting `Oppivelvollisuustiedot` for many oppijas at once); those still need either a blue/green rebuild or a tolerated inconsistency window.

**How the worker preserves the invariant**: the continuous worker batches by **oppija**, not by individual OO. Each tick:

1. Read up to `batchSize` rows from `paivitetty_opiskeluoikeus`, group by `oppija_oid` (queue rows already carry `opiskeluoikeus_oid`; an `oppija_oid` denormalization column is added by the same Flyway migration that extends the queue with `yritykset` / `viimeisin_virhe`, so the group-by doesn't require a join to `opiskeluoikeus` on every tick).
2. For each oppija batch, in **one Slick transaction** on raportointikanta RDS:
   - Update all r_* rows for every changed OO of that oppija (the existing per-OO upsert logic, applied repeatedly within the transaction).
   - Re-read all of that oppija's OOs from r_* (now post-update inside the same transaction).
   - Recompute and upsert all the oppija's derived aggregates: `PaallekkaisetOpiskeluoikeudet`, `Oppivelvollisuustiedot`, the Lukio-kertymä family, `OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset`.
   - Commit.
3. Mark every processed queue row for that oppija on primary (idempotent — replay re-derives the same rows safely).

**Why this is strictly stronger than a per-OO design**: per-OO atomicity already prevented half-updated state within one OO. Per-oppija atomicity additionally prevents the post-OO-A / pre-derived-recompute window that an earlier iteration of §1.3 left open (a `dirty_oppija` mini-queue with a second-pass tick). That mini-queue and the second-pass logic are no longer needed and are removed from §1.3.

**Cost**: each per-oppija transaction does one extra indexed read of the oppija's OOs from r_* (typical fan-out 1–3 OOs, max ~10), plus one recompute pass per derived table. Negligible vs. the existing per-OO upsert cost; eliminates an entire second-pass code path.

**What end-user features can rely on**: any single-oppija SELECT joining `r_opiskeluoikeus` + any derived aggregate table for that oppija returns a coherent snapshot. Multi-oppija queries (e.g. kuntailmoitusvalmistelu listing all 17-year-olds in a kunta) return rows that are each internally consistent but may be from slightly different per-oppija as-of moments. That weaker cross-oppija property is acceptable — the user-facing UX of multi-oppija lists tolerates per-row freshness drift, and Postgres MVCC ensures every row is committed-consistent regardless.

### §1.2 No more blue/green schema swap (for steady-state continuous mode)

Continuous writes go directly into raportointikanta RDS's `public` schema. The 5-min `ALTER SCHEMA RENAME` retry logic in long reports (`RaportointiDatabase.retryDbSync`) is **not** exercised in steady-state — but **keep it** as a safety net for the full-reload swap in §1.4 Flavor B. The drain step (§1.4 step 7) is supposed to ensure no long queries are in flight at swap time, but the retry path provides defence in depth if a query slipped through.

Blue/green machinery — the `etl` schema, `FullReloadOpiskeluoikeusLoader`, schema rename — is preserved for the full-reload escape hatch (§1.4).

### §1.3 Precomputed / aggregate tables

Built today in `RaportointiDatabase.createPrecomputedTables` (lines 156–179). Under continuous mode the worker maintains them inside the **per-oppija transaction** described in §1.1c — every derived aggregate that depends on an oppija's r_* state is recomputed in the same transaction that updates the base rows, so a single-oppija read of base + derived always returns a coherent snapshot.

| Table | How it stays fresh |
|---|---|
| `OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset` | Pure function of one OO. Delete-by-oo + insert, inside the per-oppija transaction (§1.1c). |
| `PaallekkaisetOpiskeluoikeudet` | Cross-OO over one oppija. **Recomputed inside the same per-oppija transaction** that updates the affected OO's r_* rows (§1.1c). All of the oppija's OOs are re-read post-update, the aggregate is recomputed, the rows are upserted, all in one commit. |
| `LukioOppimaaranKussikertymat`, `Lukio2019OppimaaranOpintopistekertymat`, `LukioOppiaineenOppimaaranKurssikertymat`, `Lukio2019AineopintojenOpintopistekertymat`, `LukioOppiaineRahoitusmuodonMukaan`, `Lukio2019OppiaineRahoitusmuodonMukaan`, `LukioOppiaineEriVuonnaKorotetutKurssit`, `Lukio2019OppiaineEriVuonnaKorotetutOpintopisteet` | Deterministic per-OO aggregations of `r_osasuoritus` / `r_paatason_suoritus` data. Delete-by-oo + insert, inside the per-oppija transaction. |
| `Oppivelvollisuustiedot` | Per-oppija. **Recomputed inside the same per-oppija transaction** (§1.1c). **Plus a scheduled nightly full rebuild** for globally-config-driven changes (rajapäivät, oppivelvollisuuden-vapautukset). The nightly rebuild is the one acknowledged exception to the per-oppija invariant — see §1.1c "Scope". |

Introduce a `PrecomputedTable` trait with `recomputeOppija(masterOid)` — invoked once per oppija inside the per-oppija transaction (§1.1c). The worker stays a thin orchestrator; no separate dirty-oppija queue and no second-pass tick logic.

### §1.4 Full regeneration / schema change

Three flavors of "regeneration", each with its own procedure under continuous mode:

#### Flavor A — Online column backfill (additive, nullable column)

For adding a new `r_*` column whose value is derived from existing `opiskeluoikeus.data` (e.g. a new field that's now exposed in reports).

Naive approach: Flyway migration adds the column as NULL → start backfilling 5 M OOs in chunks → done. **Problem**: between "migration adds the column" and "backfill completes" the column exists in the live schema with NULL values for unbackfilled rows. Consumers that read the column (Valpas, Lampi exports, raportit) see NULLs that aren't really NULL — they're "not yet computed". Lampi would export these NULLs to S3 and downstream consumers would treat them as authoritative — a real correctness problem.

Recommended approach: **`r_backfill_status` metadata table** + consumer-aware exports.

1. **Add a `r_backfill_status` table** (new Flyway migration, lands once):
   ```sql
   CREATE TABLE r_backfill_status (
     table_name   text NOT NULL,
     column_name  text NOT NULL,
     status       text NOT NULL CHECK (status IN ('in_progress','complete','failed')),
     started_at   timestamptz NOT NULL,
     completed_at timestamptz,
     progress_pct int,
     notes        text,
     PRIMARY KEY (table_name, column_name)
   );
   ```
2. **Backfill procedure**:
   - Flyway migration adds the column as NULL.
   - Same migration inserts a row into `r_backfill_status` with `status = 'in_progress'`.
   - **The continuous worker starts populating the new column immediately** for every OO it processes (so freshly-touched OOs already have it correct — only the long tail of cold OOs needs the explicit backfill).
   - A `BackfillJob` walks `r_opiskeluoikeus` in chunks (cursor-based by `opiskeluoikeus_oid`), for each oid pulls the current `opiskeluoikeus` row from primary, re-runs `buildKoskiRow`, upserts using the **same per-OO transaction** as continuous. Guard backfill writes with `WHERE r_opiskeluoikeus.aikaleima < backfill_start_time` so a queue update from the continuous worker (which already wrote the new column correctly) never gets overwritten by stale data.
   - `BackfillJob` updates `r_backfill_status.progress_pct` periodically.
   - When the cursor walks past the last row, set `status = 'complete'`, `completed_at = now()`.
3. **Consumer integration**:
   - **Lampi snapshot-clone orchestrator** (§1.8): before exporting, `SELECT * FROM r_backfill_status WHERE status != 'complete'`. If any rows match, either:
     - Block the export until they're all complete (safest, recommended default), or
     - Export the unaffected tables/columns and skip the in-progress ones, with a clear log line, or
     - Export everything but write a sidecar file listing in-progress columns so downstream consumers can decide.
   - **Valpas / virkailija reports**: most reports don't query new columns unless code explicitly references them; usually no behavior change needed. For columns that ARE referenced, the report code can `LEFT JOIN r_backfill_status` and either fail-soft ("data still loading") or fall back to today's computation.
   - **Status endpoint**: surface `r_backfill_status` rows in `RaportointikantaStatusServlet` (§1.7) so ops can see at a glance which backfills are in flight.
4. **Communication is still needed** but it's now backed by a queryable artifact: announce the backfill to consumers, they can poll the status table themselves rather than waiting for an email "backfill done" signal.

##### Shortcut: Postgres GENERATED columns for trivial-extract additive columns

For the subset of additive columns whose value is a **trivial expression over existing columns on the same r_* row** (typically `data->'kentta'->>'koodiarvo'` style scalar extracts from the JSONB column, or `EXTRACT(YEAR FROM alkamispäivä)` style derivations), the entire backfill machinery above can be skipped by defining the column as Postgres `GENERATED ALWAYS AS (...) STORED` in the Flyway migration.

Properties:
- **Zero backfill code.** The DDL itself populates every existing row at migration time, atomically.
- **Always correct on insert/update.** No drift window where Valpas / Lampi see NULLs for not-yet-backfilled rows. No need to register the column in `r_backfill_status`.
- **Worker doesn't have to know about it.** It writes the existing base columns; Postgres computes the derived one.

Does **not** fit:
- Derivations that need the Scala type system (any pattern match on `Opiskeluoikeus` subtype — the type-discriminated `lisätiedot` fields, etc.).
- Derivations that depend on other tables (joins to `r_henkilo`, `r_organisaatio`, koodisto).
- Aikajakso-style derived rows (state collapses across the `tila` history) — these are multi-row outputs, not single-column expressions.
- Anything where validation logic or schema-version drift fix-ups apply (those live inside the Scala deserializer `toOpiskeluoikeusUnsafe`, which isn't reachable from SQL).

Expected use: maybe 1 in 4 future additive columns qualifies. This is a small, surgical refinement to Flavor A — the `r_backfill_status` machinery and the cursor-based backfill above stay in place for the other 3 in 4.

Cost shape of Flavor A under §1: 5 M reads against primary `opiskeluoikeus` over the network during the backfill window. Realistic backfill rate is ~10 OO/s (indexes stay in place, runs alongside continuous worker and live reads), so a one-column backfill takes **~5–6 days of wall-clock time** for the full 5 M. Acceptable for occasional additive columns; if backfills become routine, §2 (logical replication) eliminates the primary-network cost and reduces wall-clock (local reads + same per-OO work).

#### Flavor B — Full regeneration via blue/green into ETL (keep today's optimization)

**Online "rebuild in place" is impractical for full regeneration.** Math:

- Today's blue/green full generation takes ~20 h. That benefits from the "build without indices, add at end" optimization — roughly **5× faster** than maintaining indexes inline.
- An online rebuild on `public` has to keep indices in place (Valpas needs them throughout). At ~10 OO/s realistic write rate with indexes, **5 M OOs = ~140 h = ~6 days**.
- That's too long for routine schema-version bumps or worker-bug recoveries.

The read-availability constraint ("never completely stop serving reads to raportointikanta") rules out the naive "lock the table, rebuild, unlock". But blue/green into a **separate ETL schema** doesn't require stopping reads — readers stay on `public` throughout the rebuild. The only contention point is the final swap.

Procedure under continuous mode + 15-min reader ceiling:

1. **Continuous worker keeps running normally** against `public`. Valpas, reports, and Lampi snapshot-clones use `public` throughout.
2. **Trigger full regeneration** (`GENERATE_RAPORTOINTIKANTA=full` ECS task). Existing `RaportointikantaService` flow creates the `etl` schema with the new `RaportointiDatabaseSchema` DDL (including any partitioning / new columns / index changes).
3. **Record the queue watermark** at load start: highest `paivitetty_opiskeluoikeus.id` (or equivalent cursor) on primary at that instant.
4. **`FullReloadOpiskeluoikeusLoader` builds `etl` with the existing optimization**: bulk-load data first, **then** build indices in batch. Reads `opiskeluoikeus` on primary at a fixed snapshot/LSN. Takes ~20 h. **No impact on raportointikanta RDS readers** — etl is invisible to them. Pins primary's autovacuum on `opiskeluoikeus` for the duration, which is acceptable for a planned operation (and goes away under §2).
5. **Continuous worker continues to update `public` during the load.** Live data stays current; etl catches a snapshot from step 3.
6. **When etl load + index build completes**:
   a. Acquire the continuous worker's `WorkerLeaseElector` lease — worker pauses gracefully after finishing its current tick.
   b. **Catch-up replay**: re-run the continuous worker logic against the `etl` schema, processing every `paivitetty_opiskeluoikeus` row whose `id > watermark` (cursor from step 3). Over 20 h of build time at ~2.3 OO/s avg, the catch-up queue is ~165 k OOs; replayed at worker full speed (~100 OO/s without long-query backpressure) this takes ~30 min.
   c. Continue replay until queue lag is zero — `etl` and `public` are now equivalent in content.
7. **Pre-swap window** (~15 min): tell the async/queueable report layer to stop scheduling new long jobs. Existing in-flight long queries continue to completion (they're bounded by the 900 s ceiling, so the window is at most 15 min). Interactive Valpas queries are unaffected — they finish fast.
8. **Atomic schema swap** (`ALTER SCHEMA public RENAME TO old_public; ALTER SCHEMA etl RENAME TO public`). Takes ACCESS EXCLUSIVE locks at the schema level. With step 7 having drained long queries, only short interactive transactions are in flight; the rename completes in milliseconds and interactive reads see at most a few-ms latency hiccup. **Without step 7**, a 15-min report query holding ACCESS SHARE on a table in the schema would block the rename indefinitely (the existing 5-min retry in `RaportointiDatabase.retryDbSync` would then time out and fail) — so step 7 is essential under continuous mode.
9. **Resume the continuous worker** against the new `public`.
10. **Resume async report scheduling.** Queued long jobs run against the new `public`.
11. **Drop `old_public`** after a hold period (e.g. 24 h, so we can roll back if needed).
12. **Validate**: run `RaportointikantaConsistencyChecker` against the new public for a few hours to confirm no drift vs. the worker's continuing updates.

Reader impact:
- **Interactive Valpas / virkailija reads**: a few ms of latency at the rename moment. Effectively zero downtime.
- **Async/queueable long reports**: scheduling paused for ~15–30 min (step 7 drain + swap). Users see "your report is queued" instead of "your report is running" briefly.
- **Lampi snapshot-clones**: don't run during the swap window — coordinate schedules.

Total full-reload cost: ~20 h of primary OLTP contention from the loader scan (same as today). Under §2 this cost goes away because the loader reads from the locally-replicated table.

#### Why keep blue/green at all?

Three reasons make blue/green the right tool for full regeneration even with continuous mode in place:

1. **Index-deferred build is ~5× faster.** Online rebuild on `public` must maintain indexes inline → ~140 h instead of ~20 h. Routine operations (schema-version bumps a few times a year) at 6 days each is not acceptable; at 20 h it's a manageable maintenance window.
2. **Isolated resource consumption.** Building into `etl` doesn't compete with Valpas reads or the continuous worker for IOPS on `public`'s indexes. An online rebuild would contend for everything.
3. **Atomic swap as a correctness boundary.** If the rebuild produces inconsistent data, we discover it during the consistency check before swapping. With online rewrite, partial-state corruption is visible to readers.

The blue/green machinery should be **kept for full regenerations and destructive schema changes** (Flavor B and C). It's already in the codebase; continuous mode adds the catch-up-replay step and the pre-swap drain step but doesn't replace it.

#### Flavor C — Destructive schema change (column drop, type change, table restructure)

Same procedure as Flavor B (build into etl, catch up, drain, swap). The new `etl` schema is defined with the destructive change applied; old `public` retains the old shape; swap is atomic. Same reader-impact story.

#### What about autovacuum during long online operations?

(Relevant to Flavor A column backfills, which **do** run online.) The question: can autovacuum keep up at backfill rates while 15-min queries run?

Combined production rate during a backfill: continuous worker (~2.3 OO/s) + backfill (~10 OO/s) ≈ ~12 OO/s, generating ~600 row mutations/s on `r_osasuoritus` under DELETE-all+INSERT-all. With the 15-min query ceiling, worst-case unreclaimable backlog = 15 × 60 × 600 = ~540 k dead tuples, or **~0.5 % of a 100 M-row table at any moment**. Autovacuum can sustain thousands of dead tuples/s on properly-sized RDS — 600/s is well within reach. So **autovacuum keeps up** during online backfills. Bloat doesn't run away; the autovacuum-runs-during-backfill scenario is fine.

The constraint that pushes us toward blue/green for full regeneration isn't autovacuum — it's **wall-clock duration**. At ~10 OO/s online, 5 M OOs takes 6 days. The continuous worker would have to coexist with the backfill for that duration; reader load would compete; the consistency window stretches. Blue/green's 20 h is just more operationally sane.

#### Lampi exports during a full regeneration

The snapshot-clone path (§1.8) is unaffected by what's happening on `public` — RDS storage snapshots capture whatever was committed at the snapshot moment, including in-progress `etl` schema. If a Lampi export starts during the catch-up replay or the schema swap, it gets a consistent point-in-time view; consumers see whichever schema (`public` or `etl` + `old_public`) existed at the snapshot moment. To avoid confusion, **don't run Lampi snapshot-clones during the swap window** — coordinate the schedules.

### §1.5pre Important constraint: interactive queries are already bounded to ~900 s

Production data: the practical upper bound for synchronous Valpas / virkailija report queries against raportointikanta RDS is **~900 seconds (15 minutes)**, enforced by client-side statement timeouts (per-repository `runDbSync(... timeout = …)`, ranging from 5 min to 10 min) and `kyselyt.timeout` for massaluovutus. Anything longer is a batch / maintenance operation (Lampi exports at ~1h47m, VACUUM at ~51m, full reloads) and runs via separate orchestration, not as a user-blocking request.

This 900 s ceiling makes the bloat math much friendlier than I'd been assuming:
- A worst-case interactive query holds its snapshot for at most ~900 s.
- During those 900 s the continuous worker can produce at most ~900 × 2.3 ≈ ~2070 OO updates → on `r_osasuoritus` roughly ~100k dead tuples (assuming avg 50 osasuoritus per OO).
- On a 100M-row table that's ~0.1 % bloat per worst-case query, fully recoverable by autovacuum once the query ends. Easy.

The rest of this section, and §1.5a, should be read with the 900 s ceiling in mind. It's the single most reassuring fact about the design.

### §1.5 Backpressure — applied selectively, NOT to interactive Valpas

Important constraint: **interactive Valpas queries must not be backpressured**. Valpas users navigate between views (oppijahaku, koulutustoimija-näkymät, kuntailmoitusvalmistelu, oppivelvollisuustiedot) and a "raportointikanta on ruuhkainen, yritä N s päästä" 503 mid-session is unacceptable UX. So backpressure has to be scoped to workloads where waiting is OK.

#### Where backpressure IS appropriate

- **Async / queueable reports** (massaluovutus, scheduled long reports): route through a massaluovutus-style queue and gate on `systemIsOverloaded`. Users already accept "results will be ready in N minutes — you'll get a notification" for this class of query. Drop in `MassaluovutusScheduler.runNext` gated by a new `RaportointikantaLoadLimiter` using the same `stopAt` / `continueAt` hysteresis as `DatabaseLoadLimiter` plus a worker-queue-lag config key under `raportointikanta.backpressureLimits`.
- **Scheduled batch jobs** (Vipunen/Lampi uploads, internal report generation): same pattern — defer when overloaded.

#### Where backpressure IS NOT appropriate

- **Valpas interactive queries**: never 503 these. UX requires sub-second / few-second responsiveness with consistent availability.
- **Synchronous virkailija report previews** (small filters): same — these need to feel interactive.

#### How interactive queries stay fast without backpressure

Backpressure is a *symptomatic* mitigation. The interactive-UX path needs the underlying system to **not be overloaded in the first place**. The 900 s query-timeout ceiling (§1.5pre) is the key fact that makes this achievable on vanilla RDS:

- **Make the queries fast**: partition `r_osasuoritus` by oppija birth year (§1.6) so Valpas's 17–25-age-band queries hit only a handful of partitions. Audit indexes against actual Valpas query patterns.
- **Bound the worker's write impact**: the continuous worker runs at ~2.3 OO/s average, batches per tick, transactions are per-OO and short.
- **Bounded autovacuum-pinning window**: with the 900 s ceiling, no synchronous query can pin autovacuum on `r_osasuoritus` for longer than ~15 min. Worst-case bloat per query ≈ 0.1 % of table size (§1.9). Autovacuum easily keeps up.
- **Isolate batch workloads** (Lampi exports, full reloads): these exceed 900 s and run via the snapshot-clone path (§1.8), not on the production raportointikanta RDS at all.
- **Live replica is optional, not required**: with the 900 s ceiling and the snapshot-clone for Lampi, a streaming replica becomes a "Phase N if needed" item — see §1.5a.

#### Lag plumbing (still needed)

Even though we don't apply it to Valpas, expose worker-queue-lag so we can:
1. Gate the async/queueable reports on it.
2. Surface it in `/paivitysaika` (§1.7) so users can see when reports are running stale.
3. Alarm on it for ops.

Add a `RaportointikantaLoadLimiter` for the async-only gating, modeled on `MassaluovutusService.systemIsOverloaded`.

### §1.5a Multiple read replicas of raportointikanta RDS — help or hurt?

**Honest reframing given the 900 s ceiling (§1.5pre):** the original case for replicas — "isolate long queries so they don't pin autovacuum on r_osasuoritus" — largely dissolves once we know synchronous queries can't run longer than 15 min. The MVCC/bloat envelope is small and self-recovering even on the writer. What's left is a **resource-scaling** question, not an architectural-correctness one.

What replicas still buy (real but bounded):
- **CPU/IOPS isolation**: a 15-min Valpas report doing partition scans doesn't compete with the continuous worker's writes for IOPS on the writer.
- **Cache isolation**: report scans don't evict hot index pages used by interactive queries.
- **Failure isolation**: a runaway query that OOMs the Postgres process takes the instance it's on, not the writer.
- **Horizontal read scaling**: multiple replicas serve more concurrent reports.

What replicas don't buy under the 900 s ceiling:
- Better correctness or bloat behavior (autovacuum + partitioning + the ceiling already solve it).
- Better freshness — replicas are *worse* (up to ~17 min stale with `max_standby_streaming_delay = 1000 s`).

**Recommended default: don't provision a live replica in Phase 1.** Run continuous mode on the existing raportointikanta RDS with the snapshot-clone for batch exports (§1.8) and measure. Add a replica later **only if** observed metrics show:
- CPU/IOPS sustained above ~70 % during peak Valpas hours;
- p95 Valpas query latency degrading under concurrent long-query load;
- autovacuum running behind the dead-tuple threshold on r_osasuoritus.

If those signals appear, the right choice is a **physical streaming replica with `hot_standby_feedback=off` and `max_standby_streaming_delay = 1000 s`**. The 1000 s setting is wide enough to never cancel a legitimate query and narrow enough to bound replica staleness at ~17 min in the worst case. **Avoid `hot_standby_feedback=on`** — it pushes bloat back onto the writer and makes more replicas strictly worse.

**Aurora** would remove the `max_standby_streaming_delay` decision entirely (shared cluster storage, no WAL replay on readers) and is the cleanest answer if read-scaling pressure grows. **Evaluate Aurora independently** of this project; the migration is a separate piece of work, and the 900 s ceiling makes it non-urgent.

### §1.6 Partitioning the heavy tables on raportointikanta RDS

Because raportointikanta RDS is a separate Postgres, its schema can diverge from primary's. This is the moment to partition the heavy tables. Two distinct payoffs:

1. **Partition pruning for the latency-critical workloads**: queries that filter on the partition key scan only a fraction of the table.
2. **Autovacuum scaling under continuous writes**: today's full-table `VACUUM ANALYZE` on `r_osasuoritus` takes ~51 min in production (§1.9). Under continuous mode an unpartitioned autovacuum pass would run that long every time the dead-tuple threshold is hit. With any partitioning, autovacuum runs per-partition in parallel — typical per-partition duration drops to single-digit minutes and several partitions vacuum concurrently. **Any reasonable partitioning scheme gives the autovacuum benefit**; the choice of partition key is mostly about which queries get explicit pruning.

#### Open investigation: partition key choice is not obvious

*(The partition-key conflict described in this subsection is a symptom of serving two fundamentally different workloads from one store; §6 — greenfield reference design — describes the architectural pattern that eliminates it.)*

The two latency-critical workload families pull in different directions:

- **Valpas (oppija-centric, age-bounded)**: queries scan an oppija's OOs across **every** oppilaitos/koulutustoimija the oppija has studied at, with the working age band 17–25 (oppivelvollisuus). Partition by **oppija birth year** prunes away all the partitions for oppijas outside the active age band. Partition by koulutustoimija_oid scans every partition.
- **Normal raportit / virkailija reports (kt- or oppilaitos-scoped)**: a large fraction of report queries are scoped to a single koulutustoimija or single oppilaitos (sektoriraportit, "koulutuksen järjestäjän raportit", vipunen-tyyppiset, oppilaitos-tason yhteenvedot). Partition by **`koulutustoimija_oid`** or **`oppilaitos_oid`** prunes these dramatically. Partition by birth year scans every partition.

Important data invariant: **`koulutustoimija_oid` / `oppilaitos_oid` change very rarely on an OO** (organisaatiohistoria notwithstanding). This means kt/oppilaitos partitioning doesn't suffer from frequent cross-partition row moves — those are rare events to handle, not a constant cost.

So the real choice is:

| Option | Valpas | Kt/oppilaitos-scoped reports | Operational complexity |
|---|---|---|---|
| **RANGE on birth year** | Great (pruned to active age band) | No pruning (scan all partitions) | Lowest. ~50 partitions. Birth year is immutable. |
| **LIST on `koulutustoimija_oid`** | No pruning (scan all kts) | Great (pruned to one kt) | Medium. ~hundreds of partitions. Kt rarely changes → rare cross-partition moves. |
| **LIST on `oppilaitos_oid`** | No pruning | Even better for oppilaitos-scoped reports | Higher. ~thousands of partitions. Postgres planning slows with that many partitions; needs `enable_partition_pruning` and careful constraint exclusion. |
| **Two-level: RANGE(birth year) → LIST(koulutustoimija)** | Great (top-level prune) | Great (sub-partition prune) | High. ~50 × ~hundreds = thousands of partitions. Needs `pg_partman` or rolling migration tooling. |
| **Two-level: LIST(koulutustoimija) → RANGE(birth year)** | Great | Great | Same complexity as above; planning cost is similar. |

**TODO / investigation needed before committing**:
1. Profile representative queries from **both** workload families against production data: which family's queries are most often slow today? How much of the slowness is full-table scans on `r_osasuoritus` vs index-driven?
2. Estimate per-partition row counts under each scheme. A LIST(koulutustoimija) scheme with HUS as one partition could leave HUS with tens of millions of rows in a single partition while small kts have thousands — uneven bloat envelopes. May need to bucket small kts into a default partition.
3. Decide whether two-level partitioning is worth the operational complexity. If kt-scoped reports are slow today AND Valpas hits the age band, two-level wins on query performance but is non-trivial to operate. If one workload family dominates, single-level wins.
4. Decide bucket size for RANGE(birth year): 1-year (~90 partitions over 1940–2030), 2-year (~45), 5-year (~18). Affects per-partition size and planner cost.
5. Consider whether `oppilaitos_oid` partitioning ever makes sense in Phase 2 sub-partitioning — most kt-scoped reports drill into single oppilaitos anyway.

This is an open architectural decision that should be **driven by measurement of real query patterns**, not by a-priori preference. Birth-year favours Valpas; kt/oppilaitos favours the bulk of report queries; two-level satisfies both at higher operational cost. The investigation steps above are what's needed before committing.

#### Common implementation notes (apply regardless of which key wins)

- **Denormalize the partition key** onto every partitioned r_* table. The continuous worker reads it (from r_henkilo for birth year; from the OO row itself for kt/oppilaitos) when constructing the r_* rows. Add `oppija_syntymävuosi` and/or already-present `koulutustoimija_oid`/`oppilaitos_oid` columns as needed.
- **Partition pruning on the per-OO DELETE**: today's `DELETE WHERE opiskeluoikeus_oid IN (…)` continues to work, but Postgres scans every partition unless we also constrain on the partition key. Pass the partition-key value alongside the oid in the upsert call so the planner can prune.
- **Rare cross-partition moves** under kt/oppilaitos partitioning: when an OO's kt/oppilaitos changes (rare per organisaatiohistoria), the per-OO transaction must DELETE from the old partition and INSERT into the new — use explicit DELETE+INSERT rather than UPDATE so Postgres routes correctly. Under birth-year partitioning this case essentially doesn't exist (birth year is immutable modulo data fixes).
- **Precomputed tables**: partition `PaallekkaisetOpiskeluoikeudet`, `Oppivelvollisuustiedot`, and the Lukio kertymä tables by the same key as the base tables, so they prune alongside.
- **Skip HASH partitioning** — gives no query pruning under any workload pattern here, and the autovacuum benefit is captured by any partitioning scheme.

#### Edge cases worth handling explicitly

- **Oppijas with unknown birth date** (under birth-year partitioning): rare henkilö-data-quality issue; route to a "default" / "unknown" partition.
- **Default partition for the long tail** (under kt/oppilaitos partitioning): tiny koulutustoimijat with a handful of OOs shouldn't each get their own partition — bucket them into a shared default.
- **Pre-school-age oppijas in esiopetus**: their data lives in the same r_* tables. Birth-year handles them naturally; kt/oppilaitos partitioning also fine.
- **Bucket sizing under RANGE(birth year)**: 1-year (~90 partitions over 1940–2030), 2-year (~45), 5-year (~18). Affects per-partition size and planner cost; pick after estimating row counts per cohort.

### §1.6a Open investigation: diff-aware per-OO updates

Today's per-OO upsert in `RaportointiDatabase.update*` does **DELETE-all-rows-for-this-oid + INSERT-all-rows-for-this-oid** for each child table (`r_osasuoritus`, `r_paatason_suoritus`, `r_opiskeluoikeus_aikajakso`, `r_aikajakso`, `r_organisaatiohistoria`, `r_koulutuksen_jarjestamismuoto_ammatillinen`, `r_osaamisen_hankkimistapa_ammatillinen`, `esiopetus_opiskeluoik_aikajakso`, `muu_ammatillinen_raportointi`, `topks_ammatillinen_raportointi`). Even if a virkailija touched only a single field on the OO (e.g. updated a comment on one suoritus), the upsert deletes and re-inserts **every** osasuoritus row for that OO.

This is correct but wasteful under continuous mode:

- **Dead-tuple production**: each unchanged osasuoritus row that gets re-emitted produces one dead tuple per upsert. Across 200 000 OO updates/day with average ~50 osasuoritus/OO, that's ~10 M dead tuples/day on `r_osasuoritus` for content that didn't change.
- **Index update cost**: every DELETE + INSERT touches every index on the affected tables — even though the new row is identical to the old one.
- **WAL volume**: every unchanged row that gets rewritten generates WAL records.

**Investigation: can the worker skip unchanged rows?**

Option A — **Compare-then-upsert**: before writing, read the existing r_osasuoritus rows for this OO, hash-compare against the new rows, and only DELETE+INSERT the differences. Pros: minimizes dead tuples. Cons: per-OO transaction now does an extra SELECT; comparison cost grows with osasuoritus count; correctness depends on a stable hash function. Worth measuring.

Option B — **`INSERT … ON CONFLICT DO UPDATE` (UPSERT) with content equality short-circuit**: use UPSERT per row instead of DELETE+INSERT. If the new values match the existing ones, the row is updated to the same content (still generates a dead tuple under Postgres MVCC, but at least HOT updates might apply if no indexed column changes). Pros: simpler than diff. Cons: rows that disappear from the new set aren't auto-deleted — still need a separate DELETE pass. Maybe not better than today.

Option C — **Stable composite primary keys + true diff**: today's r_osasuoritus uses a synthetic `osasuoritus_id` (from a sequence) — the same osasuoritus across two upserts gets different IDs. If we changed the primary key to a stable hash of (opiskeluoikeus_oid, päätason_suoritus_index, osasuoritus_path), unchanged rows would keep their ID and could be detected as "already present, skip". Pros: clean diff semantics. Cons: schema migration; downstream consumers of `osasuoritus_id` would need to handle the new format; full reload would have to assign consistent IDs.

Option D — **Don't bother; let autovacuum + partitioning absorb the waste**. The 900 s ceiling caps the bloat envelope at ~0.1 % per worst-case query; partitioning shrinks per-partition vacuum to single-digit minutes. The waste might be tolerable.

**TODO**: measure how often OO updates actually change the osasuoritus rows vs leave them unchanged. The cost/benefit of Options A–C depends on this ratio. If most OO updates only touch the parent `opiskeluoikeus` fields (tila, päättymispäivä, etc.) and leave the osasuoritus tree intact, Option A or C is a big win. If most OO updates also touch osasuoritus, the savings are modest and Option D is fine.

Same investigation applies to `r_aikajakso`-family tables: aikajaksot are computed from the OO's tila history. If the tila didn't change, the aikajakso rows didn't change either — re-emitting them generates dead tuples for nothing.

Worth doing this measurement before committing to a worker implementation strategy. Initial implementation can use today's DELETE-all + INSERT-all approach (simplest), and Option A/C can be added as a performance optimization once measurement justifies it.

### §1.7 Freshness / `/paivitysaika`

Replace the single "load completed" timestamp surfaced by `RaportitServlet.scala` (`/paivitysaika`) and `RaportointikantaStatusServlet` with a richer object:

```
{
  loadType: "continuous" | "full",
  workerQueueLagSeconds: <now - oldest unprocessed paivitetty_opiskeluoikeus row>,
  workerQueueDepth: <count where prosessoitu=false>,
  precomputedTablesAsOf: { <table>: <ts>, ... },
  lastFullReloadCompletedAt: <ts>
}
```

`workerQueueLagSeconds` is the user-meaningful number ("raportointikanta on N s jäljessä"). Keep the old field for UI backwards compatibility until the virkailija UI is updated.

### §1.8 Lampi / Vipunen uploads — run them on an RDS snapshot-restore clone

The Lampi/Vipunen exports are the part of the workload most clearly incompatible with continuous writes. Today's exports look like:

```
SELECT files_uploaded FROM aws_s3.query_export_to_s3(
  'SELECT * FROM public.r_osasuoritus',
  aws_commons.create_s3_uri('koski-raportointikanta-export-prod', '...', 'eu-west-1'),
  options := 'format csv, header true')
```

A real production sample for `r_osasuoritus` took **6 446 268 ms ≈ 1h 47m**. Under continuous mode the same query running on the production raportointikanta RDS would hold an MVCC snapshot for that 1h 47m, pinning autovacuum on `r_osasuoritus` and bloating the heap+indexes for everyone else (Valpas included). Postgres-level snapshot tools (`pg_export_snapshot`, SERIALIZABLE transactions) don't fix this — the snapshot stays active and pins the same way.

#### Recommended approach: ephemeral RDS clone via snapshot-restore

For *scheduled, periodic* exports (Lampi, Vipunen, anything that runs on a nightly-ish cadence), the cleanest solution is **not a long-lived replica** but an **ephemeral RDS instance restored from a fresh snapshot**:

1. **`aws rds create-db-snapshot`** of the production raportointikanta RDS. Storage-layer / block-level snapshot. Transactionally consistent. Typically completes in minutes; incremental against the previous automated backup, so cost is bounded.
2. **`aws rds restore-db-instance-from-db-snapshot`** into a temporary instance — e.g. `raportointikanta-lampi-export-${date}`. Restore time for hundreds of GB is roughly 15–60 min.
3. **Run all the Lampi/Vipunen exports** against the temporary instance. They can take as long as they like — they're not competing with anything. The temporary instance can even be a smaller class (fewer vCPUs, less memory) since it's not serving live traffic.
4. **Delete the temporary instance** when done. Optionally delete the snapshot too (or keep N days for audit).

Why this is better than a streaming replica for **this specific use case**:

- **Zero impact on production raportointikanta RDS.** No `hot_standby_feedback` decision; no MVCC pinning across instances; no replication-slot WAL retention worry. The snapshot is a storage-layer operation that doesn't hold a Postgres-level snapshot at all.
- **No ongoing replica cost.** Pay for the temporary instance only during the export window (a few hours/night).
- **No replica-tuning operational debt.** No `max_standby_streaming_delay`, no replication monitoring, no reader auto-scaling decisions.
- **Atomic point-in-time semantics.** The snapshot is a precise moment-in-time copy. Lampi consumers see "the world as of timestamp T" — clean and explainable.
- **Works on vanilla RDS Postgres 13 as-is.** No engine migration required.
- **Failure handling is simple.** If an export fails, retry against the same snapshot or take a new one.

Tradeoffs / caveats:

- **Cold start**: 30–60 min from snapshot start to "exports can begin". Fine for nightly cadence; not fine for sub-hour SLAs. Currently nothing requires sub-hour Lampi freshness.
- **Cost during export window**: one extra RDS instance for a few hours/night. Use a smaller instance class than production if the exports are I/O-bound (almost always — `query_export_to_s3` is straight sequential scans).
- **Snapshot retention storage**: charged per GB-month. Keep retention modest unless audit requires more.
- **Orchestration code**: a new Fargate task / Step Function / Lambda that runs the snapshot → restore → exports → cleanup sequence and handles partial failures. The new `LampiSnapshotScheduler` becomes this orchestrator instead of an in-process Slick scheduler.

#### Updated `LampiSnapshotScheduler`

- Trigger: EventBridge schedule rule (e.g. daily 02:00).
- Step 1: `rds:CreateDBSnapshot` of raportointikanta RDS. Wait for `available`.
- Step 2: `rds:RestoreDBInstanceFromDBSnapshot` into temp instance with a deterministic name and reduced sizing. Wait for `available`. Reuse the parameter group from production (so `aws_s3` extension is enabled).
- Step 3: connect to the temp instance, run the existing `aws_s3.query_export_to_s3` queries for each table in turn. Record the snapshot identifier alongside each S3 file written so consumers know the "as-of" point.
- Step 4: fire `putUploadEvents()` to EventBridge (same as today) once all exports succeed.
- Step 5: `rds:DeleteDBInstance` (no final snapshot — the source snapshot is the authoritative one). Optionally delete the snapshot after N retention days via lifecycle policy.

Implementable as a Step Functions state machine for visibility, or as a long-running Fargate task using the AWS SDK. The CDK precedent for orchestrating RDS-aware Fargate tasks already exists in `KoskiApplicationStack.ts`.

#### What about non-periodic long queries?

The snapshot-clone approach is right for scheduled, batch exports. It is **not** right for:

- **Ad-hoc analyst queries** (someone running a one-off SELECT for hours) — need either a live replica (§1.5a) or migration to Aurora.
- **Massaluovutus long jobs** — depends. If they're scheduled and tolerate point-in-time semantics, they could share the nightly clone. If they need fresher data, queue them with backpressure (§1.5).
- **Valpas long-running queries** — should not exist. Audit Valpas for any query that takes more than a few seconds; treat those as bugs to fix via indexes / partitioning.

### §1.9 Operational risks & mitigations

- **YTR queue-trigger gap**: today's queue trigger isn't wired for `ytr_opiskeluoikeus`. Adding it is a small Flyway migration but is on the critical path for continuous YTR processing.
- **Autovacuum on `r_osasuoritus` is itself a long-running operation**. A production log sample shows an explicit `VACUUM ANALYZE` (the one at the end of `loadRestAndSwap`, `RaportointikantaService.scala:261`) taking **3 079 856 ms ≈ 51 min**.
  - **Under continuous mode the *explicit* VACUUM ANALYZE goes away** (no more full rebuild → no `loadRestAndSwap` → no `vacuumAnalyze()` call). It's replaced by autovacuum running continuously on r_osasuoritus.
  - **Autovacuum on r_osasuoritus will still be ~tens of minutes per pass** as long as the table is unpartitioned. VACUUM does not take exclusive locks so it doesn't block Valpas, but it does consume IOPS for the full window and holds its own snapshot.
  - **Mitigations**: (a) partition r_osasuoritus per §1.6 — each partition vacuums independently and in parallel, dropping per-pass duration from ~50 min to ~5 min. This is the single biggest lever. (b) tune per-table autovacuum on r_osasuoritus: `autovacuum_vacuum_scale_factor = 0.01`, bump `autovacuum_vacuum_cost_limit` (e.g. 2000), keep `autovacuum_max_workers ≥ 5`. (c) consider a diff-aware worker that only re-writes osasuoritus rows whose content actually changed — see §1.6a for the investigation.
- **Bloat amplification absent partitioning**: even with autovacuum tuned aggressively, an unpartitioned 100M+ row r_osasuoritus under continuous churn will see bloat ratio drift up. **Plan to land partitioning *before* fully cutting over from the nightly batch.**
- **The 900 s query-timeout ceiling caps bloat exposure** (§1.5pre): worst-case ~0.1 % of table per query, fully recoverable. Without this ceiling the bloat envelope would be unbounded; with it the math is comfortable.
- **Worker connection to primary**: at 200 k OO/day this is a low-traffic SELECT pattern, but the worker does hold a persistent pool. Use a small connection limit (e.g. 5) and reuse the existing replica-lag-aware fetch pattern. Bursts (mass imports) hit primary the same way today's nightly loader does — no new risk.
- **Index maintenance**: each per-OO upsert pays full index cost on `r_osasuoritus`. Audit indexes in `RaportointiDatabaseSchema`; drop redundant ones before going live.
- **Worker lease / single-instance**: ensure exactly one Fargate task processes the queue at a time. Reuse `WorkerLeaseElector` against raportointikanta RDS (leases live in a small table on raportointikanta RDS).
- **Loss of nightly full-rebuild safety net**: today, a corrupt r_* row gets fixed by the next nightly rebuild. In continuous mode, a corrupt row stays corrupt until that OO is touched again. Mitigation: keep a weekly or monthly full-reload running for the first 6 months; add a per-OO consistency checker (§ Verification).
- **Backfill cost under §1**: every additive schema change that introduces a new `r_*` column requires a 5 M-row scan of primary `opiskeluoikeus`. Slow and primary-impacting. If this becomes routine, upgrade to §2 (logical replication) so backfills happen locally on raportointikanta RDS.

### §1.10 Scaling to 15 M OOs / billions of osasuoritus

Roughly 3× today's volume. Most of the design scales linearly, but a few items shift in priority:

#### Stays the same (linear scaling)

- **Per-OO transaction cost**: a single OO's upsert touches the same number of rows regardless of total table size; only index-update cost grows logarithmically. Per-OO worker latency stays in the tens-of-ms range.
- **Continuous-write throughput**: at proportional usage growth, ~600 k OO updates/day = ~7/s avg, peaks maybe 50–100/s. One Fargate task at modest sizing handles this easily.
- **Backpressure math under 900 s ceiling**: bloat envelope per worst-case query stays ~0.1 % of table size — absolute dead-tuple count grows 3×, but so does the table; autovacuum keeps up if partitioning is in place.

#### Becomes mandatory (was optional)

- **Partitioning** (§1.6): a 3× larger unpartitioned `r_osasuoritus` would push full-table autovacuum from ~50 min to ~150+ min. **Partitioning is mandatory at this scale** (key still per §1.6 investigation). Whichever key is chosen, per-partition vacuum stays manageable since each partition shrinks proportionally to partition count. If a single active partition becomes too large at 3× scale, sub-partitioning (two-level) becomes worth the operational complexity.
- **Index audit + drop redundant indexes**: at 3× scale, every extra index is 3× more expensive on every per-OO upsert. Audit ruthlessly.
- **§2 (logical replication) becomes much more attractive**: backfill cost under §1 grows linearly with table size (now ~15 M-row primary scans per schema-additive change). Under §2 backfills happen locally on raportointikanta RDS, no primary impact.

#### Becomes worth investing in

- **Postgres version upgrade**: today's PG 13 lacks some performance and operational improvements available in PG 14+ (parallel logical-replication apply matters if we ever move to §2; better autovacuum heuristics; general feature/security currency). Plan a PG 13 → 15/16 upgrade before scale arrives.
- **Aurora**: reader scaling becomes more valuable. Auto-scaling reader groups absorb growing report concurrency; Aurora snapshots scale with storage (still effectively instant for the snapshot itself); fast reader provisioning helps Lampi snapshot-clones.
- **Storage tier / instance class**: 3× data means RDS storage costs and IOPS budgets need re-sizing. Consider gp3 with higher provisioned IOPS or moving to io2.

#### Becomes potentially problematic

- **Today's borderline-900 s queries** (full-table SELECTs hidden in some report that today completes in ~700 s) will exceed the ceiling at 3× scale. **Audit slow-query logs before scale arrives**; rewrite or push to async/snapshot-clone path. Partition pruning saves most reports; full-table scanners need explicit work.
- **Lampi exports** (today 1h 47m for `r_osasuoritus`) become ~5 h+ as a single SELECT. Move to **chunked exports per partition** so each chunk completes in <1 h; parallelize across the snapshot-clone instance.
- **Memory pressure**: indexes on `r_osasuoritus` at 3× scale may exceed `shared_buffers` on smaller instance classes. Re-size the raportointikanta RDS instance proactively.

#### What this means for the project

If the 15 M / billions scenario is **likely within 2–3 years**, fold in:
- PG version upgrade now (or as Phase 0 of this project) so it's done before partitioning lands.
- Make partitioning a Phase 1 mandatory item (it's already recommended).
- Re-evaluate §2 (logical replication) as backfill cost grows.
- Aurora re-evaluation timed to coincide with the growth curve.

If the scenario is **unlikely or beyond 5 years**, treat current design as is; revisit when growth signals appear.

---

## §2. Optional upgrade: logical-replicate source tables into raportointikanta RDS

§1 has the worker pull OOs over the network from primary Koski RDS on every tick and on every backfill. §2 instead logical-replicates `opiskeluoikeus`, `ytr_opiskeluoikeus`, `henkilo` from primary Koski RDS and `oppivelvollisuudesta_vapautetut` from Valpas RDS **into** raportointikanta RDS, so the worker reads them locally. The §1 → §2 migration is purely additive — same continuous worker, same precomputed-table logic, same partitioning, same backpressure — only the source of the SELECT moves from "remote primary" to "local replicated table".

### When §2 becomes worth doing

§2 trades operational complexity (replication slot management, DDL coordination across two RDS instances) for one concrete recurring benefit: **schema-additive backfills become primary-free**.

Every time we add a new column to an `r_*` table derived from `opiskeluoikeus.data`, we have to re-run `buildKoskiRow` over all 5 M OOs to populate it. Under §1 the backfill scan reads 5 M rows over the network from primary Koski RDS, holds a long snapshot on primary's `opiskeluoikeus` (pinning primary's own autovacuum), and competes with OLTP for IOPS. Under §2 the same backfill scan reads from the locally-replicated table on raportointikanta RDS — no primary involvement, no network round-trips, no OLTP contention. Same applies to full reloads, fixing worker bugs across all OOs, schema-version bumps, and disaster recovery.

**§2 is worth doing when**:
- Schema-additive `r_*` backfills happen more than a few times a year and the operational cost on primary becomes noticeable.
- We expect a major data-model refactor (full reload) within the next year.
- We want a continuously-replicated live copy of `opiskeluoikeus` for ad-hoc analyst queries or operational/audit reasons.
- We grow to 15 M+ OOs and backfill scans against primary become hours of OLTP contention each time.

### §2 costs

- **Replication slot management**: monitor `pg_replication_slots.confirmed_flush_lsn` and `pg_stat_subscription` lag on raportointikanta RDS; set `max_slot_wal_keep_size` on primary to bound WAL retention if raportointikanta RDS apply falls behind; handle slot recreation after primary failover.
- **DDL coordination**: every Flyway migration that touches `opiskeluoikeus` / `ytr_opiskeluoikeus` / `henkilo` / `oppivelvollisuudesta_vapautetut` must be applied to **both** the source RDS and raportointikanta RDS (logical replication does not replicate DDL). New Flyway runner logic needed.
- **RDS configuration**: `rds.logical_replication=1` in the parameter group (requires restart); IAM/grants for the replication role; subscription credentials in Secrets Manager.
- **Storage on raportointikanta RDS**: full copy of `opiskeluoikeus` (5 M × hundreds of KB of JSON in TOAST ≈ a few hundred GB). Not free.
- **Initial sync**: one-time `COPY` of 5 M JSON rows (hours-not-days). Pre-seed via the existing full-reload flow and start the subscription with `copy_data = false` from a known LSN.
- **Large-transaction apply lag**: any large transaction on primary (e.g. a bulk-migration Flyway script) becomes a single logical-replication transaction that has to apply atomically on raportointikanta RDS, potentially causing lag spikes. Under §1 the worker batches naturally.

### What stays the same under §2

The continuous worker design, precomputed-table logic, ONR fetches, partitioning, backpressure, Lampi snapshot-clone, freshness reporting, autovacuum tuning — all identical to §1. Only the SELECT path against `opiskeluoikeus` and the queue location change:

| Aspect | §1 | §2 |
|---|---|---|
| Where the queue lives | Primary Koski RDS (existing `paivitetty_opiskeluoikeus`) | Raportointikanta RDS (new local queue, populated by a Postgres trigger on the *replicated* `opiskeluoikeus`) |
| Where the worker reads OOs from | Primary Koski RDS (network) | Raportointikanta RDS (local) |
| Where backfill reads OOs from | Primary Koski RDS (5 M-row remote scan) | Raportointikanta RDS (local scan, no primary impact) |
| Where r_* writes go | Raportointikanta RDS | Raportointikanta RDS |
| YTR-queue trigger | Mandatory new migration on primary | Mandatory new migration on raportointikanta RDS subscriber |

### Migration §1 → §2 (purely additive)

If we ship §1 first and decide §2 is worth doing later:
1. Enable `rds.logical_replication=1` on primary Koski RDS and Valpas RDS (parameter-group change requiring a restart).
2. `CREATE PUBLICATION` on primary Koski RDS for `opiskeluoikeus`, `ytr_opiskeluoikeus`, `henkilo`; on Valpas RDS for `oppivelvollisuudesta_vapautetut`.
3. Pre-seed replicated tables on raportointikanta RDS (one-time `COPY` from full-reload flow).
4. `CREATE SUBSCRIPTION` on raportointikanta RDS pointing at both publications, with `copy_data = false` from the pre-seed LSN.
5. Add Postgres trigger on the replicated `opiskeluoikeus` / `ytr_opiskeluoikeus` tables that populates a local queue table on raportointikanta RDS (mirror of V84). Switch the continuous worker to drain this local queue instead of primary's queue.
6. Retire the worker's connection pool to primary Koski RDS (still needed for ONR-unrelated full-reload flow until that's also migrated).

No rework of the worker's core logic, no datasource changes for reports, no precomputed-table migration. Safe to defer the §2 decision indefinitely if §1 is meeting our needs.

---

## §3. Alternative considered & rejected: drop `r_*`, query JSON directly

Logical-replicate `opiskeluoikeus` into raportointikanta RDS as in §2, but drop the normalized `r_*` schema entirely. Reports query the JSON `data` column directly via JSONB operators.

Rejected because:
- Every existing report against `r_*` would need to be rewritten — years of complex SQL with multi-table joins becoming JSONB path expressions.
- Aggregations over `osasuoritus` mean unnesting the suoritus array on every query (O(N×avg_osasuoritus) instead of O(matching rows)).
- Aikajakso semantics (`r_opiskeluoikeus_aikajakso`, `r_aikajakso`, etc.) encode pre-computed time-range collapses that are expensive to derive from the `tila` array on every query. We'd end up rebuilding these tables anyway.
- Precomputed tables (PaallekkaisetOpiskeluoikeudet, Lukio kertymät, Oppivelvollisuustiedot) cannot be replaced by on-demand JSON queries at reporting latency.
- Schema-version drift inside JSON: every OO doc reflects the version of `Opiskeluoikeus.scala` current when it was last saved. `buildKoskiRow` today absorbs that drift; querying JSON directly does not.
- TOAST traffic: 5 M × hundreds of KB JSON ≈ heavy TOAST detoast for any full-scan. Reports that today scan a thin column (`r_osasuoritus.tyyppi`) would drag the full document.
- Lampi/Vipunen consumers and BI tools expect the relational shape.

The promised simplification evaporates as soon as you need precomputed tables, schema-version normalisation, or fast aggregations.

---

## §4. Alternative considered & partially rejected: move JSON → r_* transformation into Postgres

Could `buildKoskiRow` / `buildYtrRow` be replaced (in whole or in part) by Postgres-side PL/pgSQL / triggers / generated columns, so a JSONB mutation directly produces the r_* rows without round-tripping through a Scala worker?

What `buildKoskiRow` actually does today (`OpiskeluoikeusLoaderRowBuilder.scala`):
- ~693 lines of transformation producing **19 distinct r_* tables**.
- **The first thing it does on every OO** (line 44 for Koski, line 89 for YTR) is `inputRow.toOpiskeluoikeusUnsafe(KoskiSpecificSession.systemUser)`. This deserializes the JSONB document into a typed `KoskeenTallennettavaOpiskeluoikeus` Scala value via json4s codecs. **This step absorbs all schema-version drift.** Older docs written under earlier versions of `Opiskeluoikeus.scala` are up-converted to the current shape transparently. Everything downstream operates on the typed Scala value, not the raw JSONB.
- Logic breakdown: ~25 % trivial JSONB extracts (scalar fields, localized-string picks, koodisto code conversions); **~45 % Scala-type pattern matching** over the 18+ `Opiskeluoikeus` ADT variants and their N-way `lisätiedot` subtypes plus stateful aikajakso collapses (`AikajaksoRowBuilder.scala`) and YTR cross-references; ~20 % external dependencies (koodisto registry, organisaatio names, date math).
- Some report SQL already does narrow JSONB extraction (`PaallekkaisetOpiskeluoikeudet.scala` filters on `data->'järjestämismuoto'->>'koodiarvo'`). This is a useful local pattern, not a foundation for a wholesale rewrite.

**Rejected as a wholesale strategy** for three independent reasons, any one of which suffices:

1. **Schema-version drift is solved by the Scala deserializer.** The single line `toOpiskeluoikeusUnsafe` invokes hundreds of generated json4s codecs that read OO docs written years ago and produce a current-shape typed value. Replicating that in Postgres means hand-writing per-version JSONB rewriting rules and keeping them in sync with every Scala schema change forever. Flavors A (additive column) and C (destructive schema change) of §1.4 both rely on Scala's automatic up-conversion.
2. **Type pattern-matching reproduces poorly in SQL.** The 18-variant `Opiskeluoikeus` hierarchy with N-way `lisätiedot` subtypes becomes hundreds of `CASE WHEN data->>'tyyppi'='ammatillinen' THEN ...` branches with no compile-time coverage check. Every new schema variant becomes two changes (Scala + SQL) instead of one.
3. **The expected wins are small and the parent plan already captures them another way.** The §1 worker is already a thin orchestrator — most of the cost is JSON parse + insert I/O, not Scala compute. Per-OO latency is in the tens of ms; queue-lag target is 15 min. Scala compute is nowhere near the ceiling. The actually-painful operations are full reloads and column backfills, neither of which gain meaningfully from Postgres-side transformation (backfills still touch every row; full reloads are already index-deferred under blue/green).

**Partially adopted**: the narrow slice that *does* belong in this plan is the GENERATED-column shortcut for trivial-extract additive columns — see §1.4 Flavor A "Shortcut: Postgres GENERATED columns for trivial-extract additive columns". That converts a 5–6-day backfill into a Flyway migration whenever the new column shape allows. It does not replicate any of the type-discriminated or stateful logic above; it just lets Postgres handle the cases where it's strictly easier than Scala.

---

## §5. Alternative considered & largely rejected: introduce DBT for the transformation layer

Could DBT (data build tool) replace some or all of the Scala-driven raportointikanta build? The sibling Ovara project (`/home/ahtiainen/ovara-vm`) uses DBT extensively for a similar reporting-database pattern: ~141 models layered `raw → stg → int → dw → pub`, materialized into Postgres on a scheduled Fargate task, with incremental `merge` strategy on the dw layer keyed by `(resourceid, muokattu)` metadata timestamps. Lineage DAG, auto-generated docs, declarative incremental semantics — well-suited to Ovara's style of ELT.

### Why DBT is structurally a poor fit for the **core** raportointikanta build

Every reason §4 gave for keeping the JSON → r_* transformation in Scala applies to DBT too — DBT models are SQL, so they inherit the same SQL-can't-do-this constraints. Plus extra blockers specific to the continuous-mode design:

1. **Schema-version drift is solved by the Scala deserializer.** Ovara's stg-layer JSON parsing works because its source systems land *current-shape* JSON into `raw.*` via Lampi-siirtaja. Koski's `opiskeluoikeus.data` is heterogeneous historical JSON spanning years of `Opiskeluoikeus.scala` versions; `toOpiskeluoikeusUnsafe` in `OpiskeluoikeusLoaderRowBuilder` invokes hundreds of generated json4s codecs to up-convert old docs. No equivalent exists in DBT-side SQL.
2. **Type pattern-matching over the 18-variant `Opiskeluoikeus` ADT reproduces poorly in SQL.** Same blocker as §4 reason #2. DBT macros could template some of it, but every new schema variant becomes two changes (Scala + DBT macros) instead of one, with no compile-time coverage check on the DBT side.
3. **Per-OO atomicity isn't expressible in DBT.** §1.1's worker design rests on "one OO update = one Slick transaction across all `r_*` tables, so readers never observe partial state." DBT runs each model as its own transaction; readers could see `r_opiskeluoikeus` updated while `r_osasuoritus` is still stale. Wrapping multiple DBT model executions in one transaction is not something DBT supports.
4. **DBT is batch-oriented; continuous mode is event-driven.** Ovara's DBT is cron-scheduled (every hour in Ovara's case). Koski wants ~15-min freshness from continuous mode (§1.1). Running DBT every 15 min is possible but each run is a full incremental sweep with model-compilation overhead, not a per-OO event drain — defeats §1.1's purpose and per-OO transactional property.
5. **The shape of Ovara's incremental merge is wrong for `r_*`.** Ovara merges by `(resourceid, muokattu)` and accumulates history — its `dw` layer is a temporal snapshot store. Koski's `r_*` tables are not temporal; they should always reflect the **current** shape of each OO with historic rows replaced (today's per-OO DELETE-all + INSERT-all). Mimicking this in DBT requires a `delete+insert` per-OO model, which is exactly what the Scala worker already does — but with one more round trip and external orchestration.
6. **Ovara's overall consistency contract is weaker than Koski needs.** Under closer inspection Ovara accepts eventual-consistency-within-a-window: ingest is *not* paused during `dbt build` (separate DynamoDB locks for `lampi-scheduled-task` and `dbt-scheduled-task`); the watermark in `completed_dbt_runs.start_time` is written per-model at end-of-model with `current_timestamp`, so different downstream models can effectively read source data from different points-in-time of the same ingest stream within one run; the S3 export opens a fresh Postgres connection per table (`DatabaseToS3.exportTableToS3`) with no transaction held across tables; no orchestration enforces a serialized "ingest stops → dbt → export" cycle. For Ovara's BI consumers that's invisible. For Koski's interactive consumers (Valpas) it would be a regression — a half-deleted OO whose `r_opiskeluoikeus` row is gone but whose `r_osasuoritus` rows linger breaks the UI. The continuous worker's per-OO Slick transaction (§1.1) gives **strictly stronger** cross-table consistency than Ovara's design accepts; adopting Ovara's DBT pattern on production raportointikanta RDS would weaken that.

These six reasons rule out DBT as the **primary** transformation layer. The Scala `OpiskeluoikeusLoaderRowBuilder` + continuous worker stays as designed in §1.1.

### Where DBT could play a narrow optional role

Two sub-areas exist where DBT's strengths (declarative SQL, lineage DAG, auto-generated docs, incremental materialization with metadata-timestamp filtering) match the problem shape:

1. **Precomputed / aggregate tables (§1.3).** `PaallekkaisetOpiskeluoikeudet`, the 8 Lukio kertymä tables, `Oppivelvollisuustiedot`, `OpiskeluoikeudenUlkopuolellaArvioidutOsasuoritukset` are all deterministic SQL aggregations over already-normalized `r_*` tables. Today they're inline SQL inside `RaportointiDatabase.createPrecomputedTables`. Shape-wise they're identical to Ovara's `pub_dim_*` / `int_*` layers. DBT could express them with `ref()` lineage and incremental materialization (`WHERE master_oid IN (recent dirty oppijas)` etc.).

   **Tradeoff against §1.3's design.** §1.3 maintains these inside the same per-OO Slick transaction that writes the base `r_*` rows — so a `PaallekkaisetOpiskeluoikeudet` row is always consistent with the `r_opiskeluoikeus` rows that fed it. A DBT batch run on a 5–15 min cron would let these go briefly stale between runs (precomputed.as_of < base_r_*.as_of by up to one cron interval). Invisible for most users (today's nightly batch is 24 h stale), but violates a property §1.3 explicitly preserves.

   **Cost of introducing DBT just for ~11 aggregate tables.** New tooling (dbt-core, dbt-postgres image), new Fargate task / scheduler / DynamoDB lock, new profile config in CDK, new "dbt build" step in CI, mixed Scala-and-DBT orchestration in `RaportointikantaService`, team training. Today's alternative is small inline SQL with explicit `recomputeOppija(masterOid)` from the worker — already maintainable, no new tools.

   **Verdict for §1.3 specifically: not adopted.** Defer.

2. **Future denormalized / analyst views over `r_*` — on the §1.8 snapshot-clone.** If a Lampi or Vipunen consumer ever asks for a flattened wide view (one row per oppija with denormalized suoritus data, or a join across `r_opiskeluoikeus` + `r_henkilo` + `r_organisaatio` for a specific dashboard), the natural place for that work is a **DBT layer running on the §1.8 snapshot-clone**, not on production raportointikanta RDS.

   **Why the snapshot-clone is the right host.** An RDS storage-layer snapshot is transactionally consistent by construction; the temporary instance restored from it presents a **frozen point-in-time world**. Every issue that ruled DBT out for the core build dissolves here: there's no concurrent per-OO mutation to race against, so per-OO atomicity is moot; the world doesn't change while DBT runs, so the batch orientation is fine; the watermark-drift / per-table-export-window consistency holes that Ovara accepts on its live DB simply can't occur on a frozen clone, because there's no ingest happening underneath; the resulting wide-view tables exist on a throw-away instance, so the "is this consistent with the latest writer state" question doesn't even apply. Lineage / DAG / auto-docs / incremental-merge all earn their cost because the consumer audience explicitly wants documented, versioned derived shapes. **DBT-on-snapshot-clone is actually a stricter consistency model than Ovara's DBT-on-live-DB**, even though it uses the same tool.

   **Architectural parallel to Ovara.** Ovara's point-in-time soundness comes from (a) `pub.*` tables `materialized: table` — rebuilt fresh from scratch each dbt run, so the temporally-accumulating dw layer never leaks to consumers — and (b) `lampi-siirtaja` exporting `pub` / `int1` / `gen` to S3 as CSV plus a `manifest.json`. Vipunen / Lampi consumers read those versioned immutable S3 artifacts, not the live DB. The "sound view" boundary is the S3 export step, fed by a freshly-rebuilt pub layer. Koski's §1.8 snapshot-clone-to-S3 path is the same idea via a different mechanism (RDS snapshot of a continuously-fresh DB instead of from-scratch DBT rebuild). DBT on the snapshot-clone slots into the same architectural position as Ovara's `pub.*` layer.

   **Verdict: keep in mind as a future-Phase option.** Not on the Phase 1–5 critical path. Re-evaluate if/when a wide-view consumer requirement appears. The clone instance is short-lived (hours) so DBT can be invoked from the snapshot-clone orchestrator (Step Functions / Fargate) without persistent infrastructure — no separate cron, no separate lock table, no co-existence with the production loader.

### Why not "use DBT for the full-reload aggregate-table step only"

A narrow variant: keep Scala for the base `r_*` build, but invoke DBT for `createPrecomputedTables` instead of inline SQL. Tempting because it gets lineage / docs / DAG visualization for the aggregate layer without touching the loader.

- The aggregate layer is ~11 SQL statements — small enough that today's inline-SQL approach in `RaportointiDatabase.createPrecomputedTables` is already maintainable.
- Introducing DBT adds: new Docker image, new Fargate task with EventBridge/lock orchestration, env-var-driven profile config in CDK, mixed Scala-and-DBT orchestration in `RaportointikantaService.loadRestAndSwap`, a new "what was the last DBT run state" question alongside the existing worker state. Operational surface grows for small payoff.
- The "we'd get docs and lineage" win is real but small: ~11 models, lineage essentially `r_*` → precomputed with no deeper graph to discover.

**Verdict: not adopted.**

### When to revisit

- A Lampi/Vipunen (or analyst) consumer asks for **denormalized wide views** with documented lineage. DBT on the §1.8 snapshot-clone is the right answer at that point.
- The set of precomputed/aggregate tables grows by ~3× and the inline-SQL approach becomes hard to navigate. The lineage DAG starts earning operational cost.
- Koski grows a *non-OO* data source that lands current-shape JSON via a Lampi-siirtaja-style pull (no schema-version drift, no 18-variant ADT). DBT's stg→dw pattern fits naturally for that subset, independently of `r_*`.
- Variant A of §6 (greenfield reference design) adopts DBT for the BI store half — see §6 for the broader context in which DBT becomes the natural tool.

None of these triggers exist today. **Do not introduce DBT in Phases 1–5 of this rollout.**

---

## §6. Greenfield reference design (what we'd build today)

This section is forward-looking, not part of the Phase 1–5 rollout. It exists so that the chosen continuous-update path (§1) can be evaluated against a clean-sheet design rather than only against today's nightly batch.

**Scope assumption (user-confirmed)**: OLTP stays as Koski has today — primary RDS, JSONB `opiskeluoikeus.data`, the Scala domain model with `toOpiskeluoikeusUnsafe` absorbing schema-version drift. Only the downstream Valpas + reporting / BI layer is open for redesign.

### Why a clean-sheet design would not look like today's r_*

Today's `r_*` tables serve two workloads with fundamentally different access patterns:

- **Valpas**: oppija-centric, age-bounded (oppivelvollisuus 17–25), sub-second-interactive. One oppija's whole life lit up across all their OOs.
- **Reporting / BI / Vipunen / Lampi**: koulutustoimija-centric, batch, aggregations over millions of rows. Analysts.

Most of the operational complexity in §1 is a symptom of trying to serve both from one store:

- The §1.6 partition-key conflict (Valpas wants birth-year, BI wants koulutustoimija) — exists only because the same physical table serves both.
- Autovacuum tuning + bloat-envelope reasoning under the 900 s ceiling — exists only because writers and Valpas-interactive readers contend for the same heap.
- §1.5's asymmetric "backpressure for async reports, never for Valpas" — exists only because both classes of consumer share a database that can become loaded.
- §1.8's snapshot-clone for Lampi exports — exists only because long export queries on a Valpas-serving database are unacceptable.
- §5's rejection of DBT — exists only because the writer must preserve per-oppija transactional consistency (§1.1c) for Valpas.

A clean-sheet design **splits these via CQRS** (Command Query Responsibility Segregation) — separate read models, each tuned to its actual workload. Two variants follow.

### Variant A — Pragmatic OPH-norm design (PostgreSQL / OpenSearch / S3 / Fargate / DBT)

Three downstream components, all on platforms OPH already operates:

1. **Valpas read model — denormalized oppija-document store.**
   - Tech: OpenSearch (OPH already uses it for Koski's own search backend), or a denormalized JSONB document table on a dedicated PostgreSQL instance.
   - Shape: one document per oppija, fully denormalized — all OOs, all päätason suoritus, all osasuoritus, kotikuntahistoria, oppivelvollisuustiedot, organisaatio refs, ONR henkilö data, all pre-joined and current-state-only.
   - Update mechanism: a continuous worker (the §1.1 + §1.1c pattern, repurposed) consumes the OLTP change stream and re-emits the affected oppija's document atomically per change.
   - Reads: Valpas does `GET /oppija/{oid}` returning one document. Sub-millisecond, no joins. Kuntailmoitusvalmistelu and oppivelvollisuusscanner become single-document filters (`WHERE oppivelvollisuus.aktivinen = true AND syntymäpäivä BETWEEN ...`), trivially indexable.
   - **The per-oppija atomic consistency invariant (§1.1c) is a structural property of this store, not an implementation discipline**: there is literally one document per oppija, it is replaced atomically, and a reader either sees the pre-update or post-update version. The §1.3 "recompute derived aggregates in the same transaction" gymnastics disappear because the derived aggregates are already part of the document.

2. **Reporting / BI store — classical raportointikanta built declaratively.**
   - Tech: PostgreSQL RDS (same as today's raportointikanta), but built with DBT in the Ovara-style pattern (`raw → stg → int → pub`).
   - Shape: star schema tuned for koulutustoimija / oppilaitos analyst queries — no longer compromised by Valpas's age-band access pattern, free to choose `koulutustoimija_oid` as the natural partition key.
   - Update mechanism: scheduled DBT runs (hourly or as needed), not real-time. Per-oppija atomicity is no longer required because Valpas isn't reading this store; the §5 rejection of DBT lifts.
   - Lampi exports: from a freshly-rebuilt `pub.*` layer (Ovara pattern), or from a snapshot-clone — both work because no interactive consumer is reading this store.

3. **Common change-stream — connecting OLTP to both downstream stores.**
   - Option: the existing `paivitetty_opiskeluoikeus` queue (V84 trigger) plus the missing YTR trigger, reused as a multi-consumer log.
   - Option: PostgreSQL logical replication from primary OLTP into a "raw" schema on a shared downstream RDS, with both the Valpas continuous worker and the DBT batch consuming from there.
   - Either way, the queue/log itself is shared; only the read-model consumers diverge.

**What's eliminated vs. today's plan.** The §1.6 partitioning conflict dissolves (Valpas store is naturally oppija-keyed, BI store is naturally koulutustoimija-keyed). The §1.1c per-oppija-atomic discipline becomes a structural property rather than an explicit invariant the worker must preserve. Per-OO transactional consistency requirement disappears at the BI store (Valpas reads one denormalized document atomically; BI is allowed to be eventually-consistent). §5's case against DBT dissolves for the BI store (per-oppija atomicity is no longer a constraint there). §1.8's snapshot-clone becomes optional, not load-bearing. §1.5's asymmetric backpressure becomes unnecessary — neither store has competing interactive vs. batch tenants.

**What stays the same.** `toOpiskeluoikeusUnsafe` still runs in Scala somewhere (probably in the change-stream consumer that feeds both the Valpas store and a `stg_raw_opiskeluoikeus` typed-row intermediate that DBT then reads). Schema-version drift is still solved by the same json4s codecs.

**What's harder.** Cross-store consistency: a single oppija's data lives in two places, and "Valpas shows fresh, BI shows N-minutes-stale" (or vice versa) is observable. This is the classic CQRS price. A consistency checker comparing the two stores' view of sampled oppijas becomes operational hygiene. Notably, the user's consistency requirement explicitly allows this — it is scoped to within one store for end-user features, not across stores.

### Variant B — No-constraints ideal (Kafka / Aurora or DynamoDB / Snowflake or BigQuery)

Same CQRS shape, best-of-breed tech per layer:

1. **Change-data-capture.** Debezium on primary OLTP → Kafka (MSK on AWS). Replayable, ordered, multi-consumer, schema-registry-backed. Removes the bespoke `paivitetty_opiskeluoikeus` trigger and queue table entirely. The OLTP database stops carrying the queue responsibility.
2. **Valpas read model.** DynamoDB single-table design (one item per oppija, sub-10ms point reads, auto-scales independently) or a managed Elasticsearch / OpenSearch cluster. Either eliminates per-table autovacuum / partitioning concerns entirely. Real-time updates via a Kafka consumer in Scala that runs `toOpiskeluoikeusUnsafe` and emits the document. Same structural per-oppija atomicity property as Variant A — one item per oppija.
3. **BI / reporting store.** Snowflake or BigQuery. Columnar storage, separates compute from storage so analyst query concurrency scales independently from the continuous-write path. DBT runs natively on both platforms. Vipunen / Lampi consumers either query directly via warehouse credentials or receive scheduled exports — no `aws_s3.query_export_to_s3` workarounds needed.
4. **Common infrastructure.** Kafka cluster (managed: MSK), schema registry, monitoring around CDC connector + replication-slot health, IAM federation into the warehouse.

**What's eliminated vs. Variant A.** PostgreSQL autovacuum tuning, partitioning operations on multi-hundred-million-row tables, the snapshot-clone-for-Lampi orchestration, the 900 s-ceiling-based reasoning about bloat envelopes — all of those are workarounds for using a single OLTP-class relational engine to serve workloads that have outgrown it. Variant B retires the workaround surface entirely.

**What's harder.** Three new platforms (Kafka / Debezium, DynamoDB or managed search, Snowflake or BigQuery) to operate and budget for. None are in OPH's current toolbox. Migration to a managed DWH is a multi-year organisational commitment with its own access-model, billing, and procurement politics. Variant B is the right end-state if Koski grows into a 15 M+ OO scale with high analyst-query concurrency; it is overkill at today's volume.

### Why we're not building either variant now

The continuous-update path in §1 is the **pragmatic incremental step**: it gets ~95 % of Variant A's Valpas-freshness benefit at ~5 % of the cost, while keeping `r_*` as the single store the team already operates. The partition-key conflict and autovacuum cost are real but bounded — by the 900 s ceiling (§1.5pre) on the read side, by partitioning (§1.6) on the autovacuum side, and by the snapshot-clone (§1.8) on the export side. The plan's §1 design **doesn't strand work toward Variant A**: the continuous worker, the per-oppija transaction discipline (§1.1c), the per-OO change stream, the queue + replay machinery, and the snapshot-clone pattern are all directly reusable if the CQRS split ever happens.

### Sign-posts that would push us toward Variant A

- Valpas p99 latency requirements tighten further than sub-second under concurrent BI load, even after §1.6 partitioning lands.
- Analyst-query concurrency on raportointikanta saturates a reasonably vertically-scaled RDS instance.
- Schema-additive backfills on `r_*` become a continuous operational drag (§2 logical-replication trigger, but Variant A solves it differently — at the BI-store boundary).
- A wide-view denormalized Lampi/Vipunen consumer requirement appears (the same trigger §5's narrow-role-for-DBT identifies — Variant A makes it the obvious place to apply DBT).

### Sign-posts that would push us toward Variant B

- All of Variant A's triggers, plus growth past ~20 M OOs where columnar storage genuinely outperforms PostgreSQL for the BI workload.
- A multi-team analyst audience needs federated query access (a managed DWH's auth and access-control model becomes load-bearing).
- OPH platform-wide adoption of Kafka or a managed DWH for other services makes the operational cost shared rather than Koski-specific.

None of these signposts are visible today. The §1 path is the right next step. This appendix exists so we know what comes after if and when the signposts appear, and so we can validate that today's design is shaped to migrate cleanly if it does.

---

## Critical files (for future implementation)

Files most likely to be touched under §1:

- `src/main/scala/fi/oph/koski/raportointikanta/RaportointikantaService.scala`
- `src/main/scala/fi/oph/koski/raportointikanta/IncrementalUpdateOpiskeluoikeusLoader.scala` (logic moves to the new worker)
- `src/main/scala/fi/oph/koski/raportointikanta/FullReloadOpiskeluoikeusLoader.scala` (still used for full reloads; add YTR per-OO upsert helpers)
- `src/main/scala/fi/oph/koski/raportointikanta/RaportointiDatabase.scala` (add `updateYtrOpiskeluoikeus` per-OO methods)
- `src/main/scala/fi/oph/koski/raportointikanta/RaportointiDatabaseSchema.scala` (partitioning DDL under §1.6)
- `src/main/scala/fi/oph/koski/raportointikanta/OpiskeluoikeusLoaderRowBuilder.scala` (replace shared `AtomicLong` with sequences)
- `src/main/scala/fi/oph/koski/raportointikanta/HenkiloLoader.scala` (split: `loadHenkilöt` for initial-sync; new per-oppija helpers for first-touch + refresh; confidential-schema kotikuntahistoria handling for turvakielto state changes)
- `src/main/scala/fi/oph/koski/raportointikanta/OrganisaatioLoader.scala`, `KoodistoLoader.scala`, `OppivelvollisuudenVapautusLoader.scala` (run on raportointikanta RDS as independent periodic schedulers)
- `src/main/scala/fi/oph/koski/henkilo/OpintopolkuHenkilöFacade.scala` (ONR client wiring needs to be available in the new continuous-worker Fargate task; mind auth/credentials)
- `src/main/scala/fi/oph/koski/opiskeluoikeus/PäivitetytOpiskeluoikeudetJonoService.scala` (add `yritykset` / `viimeisin_virhe`; YTR-queue counterpart)
- `src/main/scala/fi/oph/koski/ytr/download/YtrDownloadService.scala` (verify writes go through a path that fires the new YTR trigger; if it bulk-COPYs around triggers, add explicit enqueueing)
- `src/main/scala/fi/oph/koski/perustiedot/PerustiedotSyncScheduler.scala` (template for the new continuous worker)
- `src/main/scala/fi/oph/koski/massaluovutus/MassaluovutusService.scala` (template for backpressure)
- `src/main/scala/fi/oph/koski/raportit/` & `src/main/scala/fi/oph/koski/raportointikanta/RaportointikantaStatusServlet.scala`, `RaportitServlet.scala` (new freshness shape; 503 + Retry-After on async-only backpressure)
- `src/main/scala/fi/oph/koski/config/KoskiApplication.scala` (already has `raportointiDatabase` + `raportointiGenerointiDatabase` pointing at raportointikanta RDS; new continuous-worker Fargate task uses these same datasources, plus a connection to primary for queue/OO reads)
- AWS CDK: `/home/ahtiainen/koski-aws-infra-vm/src/KoskiApplicationStack.ts` (new Fargate task definition for the continuous worker, mirroring `raportointikantaLoaderTaskDefinition`). `KoskiRaportointiDatabaseStack.ts` may need updates for autovacuum tuning and parameter group changes. **No new RDS instance is needed.**
- New on the Koski side: `RaportointikantaContinuousWorker.scala`, `PrecomputedTable.scala` trait, `RaportointikantaConsistencyChecker.scala`, `LampiSnapshotScheduler.scala`, Flyway migrations for queue extension (`yritykset` / `viimeisin_virhe`), YTR queue trigger, partitioning DDL, autovacuum tuning.

Files additionally touched if/when upgrading to §2: see §2's migration section.

## Verification (when implementation lands)

This is a feasibility doc, so verification is described, not executed:

- **Unit/integration tests** parallel to existing `RaportointikantaService` specs: build a small fixture set, mutate an OO, run one worker tick, assert all R-tables and affected precomputed tables match what `FullReloadOpiskeluoikeusLoader` produces for the same data.
- **Consistency checker** in preprod (Phase 3 onward): nightly diff between continuous `r_*` and a freshly-rebuilt staging schema for ~1 000 sampled OOs; alert on any non-zero diff. Keep running for at least the first 6 months in production.
- **Load test** in preprod: replay 200 000 synthetic OO updates over 24 h; observe worker queue lag, raportointikanta autovacuum metrics, `r_osasuoritus` bloat ratio, and report query latency. Acceptance: p95 queue lag < 15 min, no report regression > 20 %, bloat ratio stable over 72 h.
- **Schema-change rehearsal**: add a nullable column to `r_opiskeluoikeus` via the online backfill path; verify continuous worker continues uninterrupted and backfill completes without overwriting newer rows.
- **Failover rehearsal**: simulate raportointikanta RDS loss (stop the RDS instance). Verify reports cleanly degrade to 503/queued; primary keeps serving OLTP. Recovery cleanly catches up via the queue.

## Open decisions / risks called out for the user

1. **Worker hosting**: dedicated ECS Fargate service for the continuous worker (recommended, mirrors `raportointikantaLoaderTaskDefinition`) vs. running it inside the main Koski Fargate service. The dedicated task is cleaner operationally but costs an additional task definition.
2. **Synchronous vs. queued reports**: many reports are synchronous today. Routing the long ones through a massaluovutus-style async queue is a UX change (users get a notification when results are ready) worth confirming separately from the data-pipeline question.
3. **Partition key for r_osasuoritus / r_paatason_suoritus / r_opiskeluoikeus** — **open investigation, not settled** (see §1.6). Valpas wants birth-year; normal raportit wants koulutustoimija/oppilaitos. Both kt and oppilaitos rarely change on an OO, so kt partitioning is more viable than I previously thought. Profile real query patterns from both workload families before committing. Two-level partitioning (RANGE(birth-year) → LIST(kt)) gives both benefits at higher operational cost.
4. **Diff-aware updates** (§1.6a): measure how often OO updates actually change the osasuoritus / aikajakso rows vs leave them unchanged. If most OO updates only touch parent `opiskeluoikeus` fields, a diff-aware worker (skip unchanged rows) cuts dead-tuple production substantially. If most OO updates touch the suoritus tree anyway, today's DELETE-all + INSERT-all is fine. Decide implementation strategy after measuring.
5. **Lampi/Vipunen behavior during column backfills** (§1.4 Flavor A): the recommended `r_backfill_status` table tells the Lampi orchestrator which columns are not yet fully populated. Default behavior should be one of: (a) block the export until backfill completes (safest); (b) skip in-progress columns and proceed; (c) export everything plus a sidecar file listing in-progress columns. Decide with Lampi/Vipunen consumers which they prefer.
6. **`suostumus peruutus` flow**: confirm the V84 trigger captures all paths into `opiskeluoikeus` updates, including `SuostumuksenPeruutusService`. If not, add explicit `jono.lisää(oid)` calls there.
7. **Lampi consumers' snapshot expectations**: confirm with Lampi/Vipunen team that a scheduled-snapshot model is acceptable replacement for "uploaded after every full load".
8. **YTR download cadence**: today `YtrDownloadService` runs periodically and lands large bursts into `ytr_opiskeluoikeus`. Confirm the bursts can be absorbed by the continuous worker within the 15-min target; add the missing YTR queue trigger so the worker can drain them.
9. **ONR refresh cadence**: how stale is acceptable for `r_henkilo` / `r_kotikuntahistoria`? The continuous worker covers first-touch for new oppijas; the periodic `HenkiloRefreshScheduler` (nightly? every 4 h?) catches ONR-side changes (kuolinpäivä, name changes, kotikuntahistoria fixes, turvakielto transitions). Today the answer is "≤ 24 h" by virtue of nightly full reload — confirm same target is acceptable.
10. **ONR connectivity from the new Fargate task**: verify the CDK security groups / IAM / secrets give the new continuous-worker task ONR network reachability + OAuth2 credentials, mirroring the existing main Koski Fargate task.
11. **When to upgrade to §2 (logical replication)**: decide based on observed schema-additive backfill cadence and any analyst-query needs for raw `opiskeluoikeus.data` on raportointikanta RDS. Migration is purely additive, so deferring is safe.
12. **Lampi as upstream source for periodic refresh** (§1.1b future option, §4 Phase 8): when (if ever) to migrate `r_henkilo` / `r_kotikuntahistoria` / `r_organisaatio` periodic-refresh from live HTTP to Lampi dump consumption. Triggering signals: ONR retry-envelope tripping in production, or OPH platform team flagging Koski's refresh scan as a co-tenant problem. Open prerequisite: confirm with the Lampi / OPH platform team the dump SLA, format, schema stability, and whether turvakielto-side data is published at all. Cheap to start the conversation now even if implementation is deferred.
13. **Postgres-side JSON → r_* transformation, beyond GENERATED columns** (§4): the wholesale rewrite is rejected for the reasons in §4. The GENERATED-column shortcut is adopted in §1.4 Flavor A. No further investigation recommended unless the schema-version-drift problem changes shape (e.g. we adopt a strict, version-stamped JSON contract on `opiskeluoikeus.data`, which would invalidate reason #1 of the rejection).
14. **Aurora migration**: independent of §1/§2. Aurora's structural advantages (no `max_standby_streaming_delay` decision, sub-100ms reader freshness, faster failover, fast snapshot/restore for the Lampi clone pattern) are real but bounded once the 900 s query ceiling is in place. Evaluate on its own merits (cost, ops appetite, read-scaling needs), not as part of this project.
15. **DBT for the transformation/aggregate layer**: rejected as both primary loader replacement and aggregate-table maintenance tool (§5). Sibling Ovara (`/home/ahtiainen/ovara-vm`) uses DBT for a similar ELT pattern, but Koski's Scala-side `toOpiskeluoikeusUnsafe` (schema-version drift), 18-variant `Opiskeluoikeus` ADT type matching, and the per-OO atomicity property of §1.1 make DBT structurally wrong as a core tool — and Ovara's eventual-consistency-within-a-window contract would be a regression for interactive Valpas. Reconsider only if a denormalized wide-view Lampi/Vipunen consumer requirement appears (then on the §1.8 snapshot-clone, where the frozen storage-layer copy eliminates the consistency holes Ovara accepts on its live DB), or if the aggregate-table set grows >3×.
16. **Per-oppija atomic consistency invariant** (§1.1c): hard requirement for end-user features (Valpas, virkailija interactive views) — derived data shown for an oppija must always be computed from the same opiskeluoikeus generation the user sees. Relaxed for analysis paths (Lampi / Vipunen / batch BI). Drives the per-oppija (not per-OO) transaction batching in §1.1 and the elimination of the dirty-oppija mini-queue / second-pass logic in §1.3.
17. **Greenfield reference design** (§6): two reference variants documented (pragmatic OPH-norm Variant A; no-constraints ideal Variant B). Forward-looking only; not on the Phase 1–5 rollout. Sign-posts that would trigger Variant A: Valpas latency requirements diverge further from BI batch needs; analyst concurrency outgrows PostgreSQL; wide-view denormalized Lampi/Vipunen consumer requirement appears. Sign-posts for Variant B: all of Variant A's plus growth past ~20 M OOs and OPH platform-wide adoption of Kafka or managed DWH for other services.
