# Raportointikanta continuous updates — management overview

## What's the problem we're solving

Raportointikanta (Koski's reporting database) is rebuilt as a **nightly batch job**. Reports against it can therefore be up to **24 hours stale**. As Valpas, Vipunen, and virkailija report consumers grow more interactive, this lag is increasingly noticeable.

## What we want

Updates to an opiskeluoikeus visible in reports within **~15 minutes**, against today's 24 hours. Targets: ≤ 200 000 OO updates / 24 h, production scale ~5 M OOs and hundreds of millions of osasuoritus rows, reports continue to run against the same instance the loader writes to.

## What we propose

A **continuous-update worker** that drains the existing change queue (already populated by a Postgres trigger today) and applies per-OO updates to raportointikanta as they happen — replacing the nightly rebuild for steady-state operation.

### Architecture in one paragraph

A new always-on Fargate service watches the change queue, fetches each updated opiskeluoikeus, and writes the corresponding rows into raportointikanta inside a single short transaction per OO. Reports keep reading from the same raportointikanta they read today — they just become continuously fresh instead of once-a-day. Heavy long-running export jobs (Lampi/Vipunen) move to an ephemeral throw-away clone of raportointikanta so they don't compete with live readers.

## Scope: what's in, what's out

### In scope
- New continuous-update worker (one Fargate task).
- Bridge a small gap in the existing change-queue plumbing (YTR-side trigger missing today).
- Make Lampi/Vipunen exports run on an ephemeral RDS snapshot-clone instead of on production raportointikanta.
- Partition the heaviest reporting tables so autovacuum stays manageable under continuous writes.
- Per-OO failure handling, freshness/lag reporting endpoints, per-table-autovacuum tuning.
- Keep the existing full-rebuild path as an escape hatch for schema changes and disaster recovery.

### Explicitly **not** in scope
- **No new database instance** — raportointikanta already runs on its own RDS.
- **No schema rewrite** — the existing `r_*` table shape stays.
- **No Aurora migration** — evaluated separately on its own merits.
- **No live read replica in Phase 1** — added later only if measurements show resource pressure.
- **No DBT or other new transformation tool** — sibling Ovara uses DBT successfully for its reporting DB, but Koski's design constraints (schema-version drift in the source JSON, atomic cross-table consistency for Valpas, event-driven per-OO updates) make DBT structurally a poor fit on the live raportointikanta. Possible future role on the export-clone if a wide-view consumer requirement appears.

## Why this is smaller than it might sound

The change-capture trigger on the OLTP database already exists. The per-OO row-builder in Scala already exists. The queue service already exists. The reports already read from raportointikanta. What's new is essentially: a worker that wires these pieces into a 24/7 loop, plus operational hardening (partitioning, autovacuum tuning, snapshot-clone for exports). No new platforms, no greenfield rewrite.

## Rollout phases

The detailed plan has 8 phases; most are small or conditional. Headline view:

| Phase | What happens | Notes |
|---|---|---|
| 0 | Postgres version upgrade; start investigations | Hygiene + parallel info-gathering |
| 1 | Foundations: config flag, queue extensions, the missing YTR trigger | No production behavior change |
| 2 | Move Lampi/Vipunen exports to snapshot-clone | Prerequisite for continuous mode being safe |
| 3 | Build & test the continuous worker | Drift-monitored in preprod for ~2 weeks |
| 4 | Production cutover | Nightly full reload retained initially as safety net |
| 5 | Partition the heaviest tables | After production data informs the partition key |
| 6 | *(conditional)* Live read replica | Only if metrics show CPU/IOPS saturation |
| 7 | *(conditional)* Logical replication upgrade | Only if schema-additive backfills become routine |
| 8 | *(conditional)* Read external master data from Lampi dumps | Only if ONR refresh load becomes operationally painful |

Phases 1 and 2 run in parallel. Phases 6–8 are explicitly "only if measured need" — not on the critical path.

## Trade-offs the design accepts

- **Loss of the implicit nightly clean slate.** Today's nightly rebuild fixes any accumulated drift for free. Under continuous mode, a corrupt row stays corrupt until that OO is touched again. **Mitigated** with a per-OO consistency checker and a weekly/monthly full reload retained initially.
- **Long-running exports must move off the live database.** Lampi exports today take nearly two hours as a single SELECT. Under continuous writes that would bloat the live tables for everyone else. **Mitigated** by running exports on a transactionally-consistent snapshot-clone of raportointikanta.
- **Backfilling a new derived column is slower under this approach.** A schema-additive column requires reading every opiskeluoikeus from the OLTP database (days, not hours). **Mitigated** by (a) a backfill-status metadata table so consumers know which columns are still loading, and (b) the option to upgrade to logical replication later if backfills become routine.
- **Synchronous Valpas queries are never throttled.** Backpressure is applied to async/queued reports only — interactive UX requires consistent availability. This works because synchronous queries are already bounded to 15 minutes by client timeouts, which keeps the bloat math comfortable on the live database.

## Risk on the critical path: per-OO throughput

A production measurement of today's incremental loader shows it processes ~500 opiskeluoikeus updates in ~460 seconds, i.e. **~0.92 seconds per OO**, and that's without computing the derived aggregates or running per-oppija transactions.

The steady-state continuous-mode budget is **~0.43 seconds per OO** (200 000 OO updates/day evenly distributed = 2.3/s). Once the per-oppija atomic consistency invariant above is layered on, the cost goes up, not down. So **today's loader is roughly 2× slower than the continuous-mode budget**, and optimization is on the critical path of Phase 3 — not optional polish to add after cutover.

The plan reflects this directly: investigations I15–I22 in Phase 0 measure where the seconds actually go (DB writes? JSON parse? long-tail OOs? autovacuum contention? subtree-change distribution?), and Phase 3's Scala work includes a dedicated throughput-optimization workstream. The highest-impact levers identified upfront:
- **Subtree-aware change detection from the database trigger**: extend the trigger that already records "this opiskeluoikeus changed" to also record *which part* of it changed (suoritukset / tila / lisätiedot / organisaatiohistoria / juuritason kentät). The worker then skips whole reporting tables whose source subtree didn't change. Most virkailija edits touch only one subtree; today every queue entry causes recomputation of all ~11 reporting tables regardless.
- **Diff-aware writes** for `r_osasuoritus` / `r_aikajakso` so only rows whose content actually changed are deleted+inserted (instead of today's "delete everything for this OO, re-insert everything"). Independent of and multiplicative with subtree-aware branching: the trigger flags skip whole tables, diff-aware writes skip rows within touched tables.
- **Batched upsert** for `r_opiskeluoikeus` (today's per-row pattern is the most visible chattiness).
- **Index audit** on the heaviest write-target tables — each redundant index makes every write proportionally slower.
- **Per-oppija worker parallelism** — the per-oppija atomicity invariant means different oppijas can be processed in parallel without contention.

Combined effect of the first two levers, on conservative assumptions (typical edit touches 1 of 5 subtrees and within the touched group only 1 of 5 rows actually changed), is on the order of **100× fewer database mutations per OO update** than today. Even at much weaker assumptions, the combination comfortably closes the 2× gap between today's ~0.9 s/OO and the ~0.4 s/OO continuous-mode budget.

Until measurements come back from I15–I22 we cannot pick the order, only the set. If the measurements show that none of the levers above closes the gap, that's a Phase 3 exit-criterion failure and forces re-evaluation before production cutover.

## Hard consistency invariant

End-user features (Valpas, virkailija interactive views) require that the derived data shown for an oppija (oppivelvollisuustiedot, päällekkäiset opiskeluoikeudet, Lukio-kertymät, …) is always computed from the same underlying opiskeluoikeus state that the user currently sees. Roughly: the data shown for one oppija must be characterizable as "as of KOSKI data-transfer time T" — a coherent snapshot, never a mix of fresh visible OOs with stale derived aggregates.

**The continuous-update worker satisfies this by batching per oppija**, not per individual OO: each tick groups queue rows by oppija, then runs one atomic transaction per oppija that updates the oppija's base rows AND recomputes their derived aggregates AND commits everything together. The dirty-oppija mini-queue / second-pass design that an earlier iteration of the plan considered has been removed for this reason.

**Relaxed for analysis.** Lampi / Vipunen / batch BI consumers are explicitly allowed to be eventually-consistent at the per-row level — the manifest-based S3 export model already accepts this. The invariant binds only the end-user-interactive read path.

**Multi-oppija views** (e.g. kuntailmoitusvalmistelu listing all 17-year-olds in a kunta) return rows where each oppija is internally consistent but rows can be from slightly different per-oppija as-of moments. That weaker cross-oppija property is acceptable for the UX.

## Open questions that need stakeholder input

1. **Partitioning key** for the heaviest tables: profile real query patterns from Valpas (oppija-centric) vs. report consumers (organisaatio-centric) before committing.
2. **Lampi/Vipunen consumer expectations**: confirm that scheduled snapshot-clone exports are an acceptable replacement for "uploaded after every full load".
3. **Behavior during a column backfill**: should Lampi exports block until the backfill completes, skip in-progress columns, or proceed with a sidecar list of incomplete columns? Decide with downstream consumers.
4. **ONR refresh cadence**: today's nightly batch gives ≤24h staleness of person data implicitly. Confirm acceptable target under continuous mode (every 4h? 12h? 24h?).
5. **Long synchronous reports**: which currently-synchronous reports should route through an async queue instead? UX change worth confirming with product.

## What would we build today if Koski didn't exist?

A clean-sheet design would split the two workloads that today's `r_*` tables serve into separate, purpose-built read models — a denormalized **oppija-document store for Valpas** (one document per oppija with all data pre-joined for sub-millisecond lookups) and a **classical reporting store** for analysts / Vipunen / Lampi (declarative DBT-built, batch-refreshed, snapshot-exported). Both fed from the same change-stream off OLTP. This is the pattern known as CQRS.

Most of the operational complexity in the current plan — the partition-key conflict, autovacuum tuning on shared tables, the careful asymmetry of backpressuring async reports but not Valpas, the snapshot-clone for Lampi exports, the per-oppija atomic discipline the continuous worker must explicitly maintain — is a symptom of serving two fundamentally different workloads from one store. A clean-sheet design **eliminates the conflict and makes the consistency invariant a structural property** (one document per oppija means atomic-replacement is automatic), at the cost of operating an additional store and accepting that the same oppija can be at slightly different freshness in the two stores.

**Why we're not building this now.** The continuous-update path in this plan gets roughly 95 % of the Valpas-freshness benefit at roughly 5 % of the cost, and keeps a single reporting store that the team already operates. The current plan's machinery (continuous worker, change-queue, snapshot-clone) is reusable as the Valpas-side half of the clean-sheet design — meaning today's work doesn't strand if pressure ever validates the split.

**Two reference variants** are documented in §6 of the full plan:
- **A pragmatic version** on OPH-norm tech (OpenSearch or PostgreSQL document table for Valpas; PostgreSQL + DBT for the reporting store).
- **A no-constraints ideal** (Kafka + Debezium / DynamoDB or managed Elasticsearch / Snowflake or BigQuery) for the long-run end-state if Koski scales past ~20 M OOs and analyst concurrency outgrows PostgreSQL.

Both variants are forward-looking. Neither is part of the rollout this plan describes.

## Success metrics

- **Queue lag p95 under 15 minutes** for 30 days post-cutover.
- **Per-OO worker latency ≤ 0.4 s** (production target) — from today's measured ~0.92 s/OO. Gates Phase 3 exit.
- **Zero cross-table consistency drift** measured by the nightly consistency checker.
- **No regression in synchronous report p95 latency** versus today's nightly-baseline.
- **No regression in Lampi/Vipunen export reliability** after the snapshot-clone migration.
- **Post-partitioning**: per-partition autovacuum p95 under 10 minutes.

## Cost shape

- **New infrastructure**: one always-on Fargate task (continuous worker) + an ephemeral RDS instance running a few hours per night for Lampi exports. Both bounded.
- **Phased growth costs are conditional**: a live read replica or logical-replication upgrade only happen if measured need appears.
- **Engineering effort**: phased over multiple iterations; investigations run in parallel with development; most steps are additive (preserve fallback paths) so a phase that doesn't pan out can be paused.

## Why now

Three pressures converge:
1. Valpas and other interactive consumers increasingly expect near-real-time data; 24h is becoming a UX problem.
2. We're approaching scale (5M → 15M OOs in the medium term) where the nightly rebuild itself becomes operationally tight.
3. The Postgres-side and codebase-side building blocks needed to do this are already in place — the integration work is meaningfully smaller than greenfield. Doing it before scale lands is materially cheaper than retrofitting it later.
