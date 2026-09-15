# Ducit

> AI reasons. Ducit governs. The user decides.

Ducit is an Android-first, agency-centered personal operating layer. This
repository is the **native Kotlin/Jetpack Compose implementation** —
per the Intelligence-to-Implementation Dossier v1.0, the prior React
prototype is treated as interaction/reference material only, not the
production core.

## Status: Sprint 1 (Foundation and truth reset), in progress

This commit implements days 1-9 of the dossier's ten-day Sprint 1.
**Nothing here claims to be more finished than it is** — see
[What is *not* built yet](#what-is-not-built-yet) before assuming any
capability beyond what's listed below.

### What's built and verified

| Piece | Where | Verified how |
|---|---|---|
| Module skeleton, Gradle wrapper, CI | root, `.github/workflows/ci.yml` | `domain-tests` CI job is green |
| Governed domain schemas (Proposition, PersonalContextRecord, RiskFacts, AuthorityEnvelope, GovernanceDecision, TransactionState, TrustReceipt, ...) | `core/domain-model` | Compiles + 5 unit tests pass locally |
| Deterministic R0-R5 risk classifier | `core/domain-policy/RiskClassifier.kt` | 9 unit tests pass locally |
| Deterministic governance policy engine (ALLOW/DENY/APPROVAL_REQUIRED) | `core/domain-policy/GovernancePolicyEngine.kt` | 6 unit tests pass locally |
| Transaction state machine (DRAFT → ... → RECEIPTED) | `core/domain-policy/TransactionStateMachine.kt` | 9 unit tests pass locally, including the property test "no R4 transaction ever reaches READY without a granted approval" |
| PersonalContextRecord lifecycle (capture/confirm/correct/dispute/restrict-purpose/delete) | `core/domain-policy/PersonalContextRecordLifecycle.kt` | 8 unit tests pass locally |
| Room persistence (installed-app cache, PersonalContextRecord, transaction state) | `core/data-local` | Compiles + `:core:data-local:test` passes in CI |
| Offline app-grid launcher (PackageManager discovery, search, launch) | `app` | `:app:assembleDebug` passes in CI |
| Memory Inspector (capture form, record list, Because/Last verified/Used for detail, confirm/correct/dispute/restrict-purpose/delete) | `app/.../ui/memory` | `:app:assembleDebug` + `:app:lintDebug` pass in CI |
| Capture via Android share (`ACTION_SEND text/plain` pre-fills the capture dialog; never saves silently) | `MainActivity`, `MemoryViewModel.openCaptureWithPrefill` | `:app:assembleDebug` passes in CI |

**37 domain-layer unit tests, all passing**, run with:

```
./gradlew :core:domain-model:test :core:domain-policy:test
```

These two modules are pure Kotlin/JVM with zero Android dependency, by
design (Verbal Reference Implementation v1.0, section 5: "Pure Kotlin
domain layer; JVM-testable without Android"). That's what makes the
governance invariants below independently checkable without a device,
emulator, or even the Android SDK.

### Build-environment note

`core:data-local` and `:app` depend on the Android Gradle Plugin and
AndroidX/Compose/Room, which live on Google's Maven repository — the
sandbox that wrote most of this code had no route to `dl.google.com`, so
those two modules were authored and reviewed without a local compile.
CI's `android-build` job caught four real bugs from that blind spot on
its first successful run (missing dependency, a smart-cast issue, a
missing icon, a version-sensitive API) before landing green. **As of
[run 35005218207](https://github.com/captainlandon/DUCITv1.0/actions/runs/35005218207),
`:app:assembleDebug`, `:core:data-local:test`, and `:app:lintDebug` all
pass in CI.** Full account, including the diagnosis of a second,
unrelated CI infrastructure bug, in
[`docs/adr/ADR-00-build-environment.md`](docs/adr/ADR-00-build-environment.md).

### What is *not* built yet

Everything past Sprint 1 day 9, honestly:

- Day 10's truth audit against the React demo doesn't apply — there is no
  React demo in *this* repo to audit against.
- Any AI/model integration whatsoever (by design — Sprint 1 requires none)
- Now Cards, Command surface, Plan preview, Approval, Receipt UI
- Intent/capability resolution, connector registry, orchestration engine
- Real execution adapters (Android intents beyond `getLaunchIntentForPackage`)
- Trust Receipt hashing/chaining, deletion + meta-receipt
- Process-death recovery tests for the transaction state machine (the
  state machine itself is tested; persistence-survives-a-kill is not yet)
- **Any on-device verification.** CI proves the code compiles, passes
  its unit tests, and passes lint — it does not prove the launcher is
  usable, that cold-start/app-launch timing is acceptable, or that a
  process kill mid-transaction actually reconciles cleanly on a real
  phone. The Dossier's Sprint 1 acceptance criteria (Days 1-2: "cold
  start target measured... app launching works offline"; Days 4-6:
  "process-kill test does not lose or misreport canonical state") are
  device tests, not CI jobs, and haven't been run on the founder's S25.
- Everything gated behind ADR-01 through ADR-08 (all open — see
  [`docs/adr/`](docs/adr/))

See the Dossier's section 8 (Ordered engineering backlog) for the full
picture; this repo currently addresses B-002, B-004, and B-005, plus a
slice of B-003.

## Governing invariants this code enforces today

From Verbal Reference Implementation v1.0, section 1.2, the ones the
`core:domain-policy` test suite actually exercises:

- **INV-01** — inference never silently becomes fact (`EpistemicStatus`,
  `Proposition.isActionable`).
- **INV-02** — a model cannot grant, expand, or persist its own authority
  (`GovernancePolicyEngine` and `TransactionStateMachine` are pure,
  deterministic, model-free).
- **INV-03 / INV-04** — every consequential action resolves risk and
  authority before execution; R4 requires explicit contextual approval,
  never a silent standing bypass; R5 is never autonomously executable.
  (`TransactionStateMachineTest`'s property test.)
- **INV-05** — execution success is not outcome success
  (`VerificationStatus` is a distinct axis from `TransactionState`).
- **INV-09** — every durable proposition/record has provenance or is
  explicitly marked unknown (constructor `require()`s in `Proposition`
  and `PersonalContextRecord`).
- **INV-11** — deletion is real, not cosmetic: `PersonalContextRecordLifecycle.delete`
  flips `deletionState` and clears the purpose allowlist; the DAO's
  `observeRetrievable()` query excludes deleted/superseded rows at the SQL
  level, not just in application code.
- Correction propagation (Verbal Reference Implementation v1.0, section
  17.2) — `PersonalContextRecordLifecycle.correct` never overwrites a
  record in place; it supersedes the old one and creates a new one, so
  the original evidence is preserved rather than falsified.

## Module map

```
core/domain-model   pure Kotlin — typed schemas, no Android dependency
core/domain-policy  pure Kotlin — risk classifier, governance engine,
                    transaction state machine (depends only on domain-model)
core/data-local     Android library — Room entities/DAOs backing the above
app                 Android application — the offline launcher shell
```

This mirrors the dossier's recommended module boundary (section 5),
scoped down to what Sprint 1 needs. `ui-designsystem`, `ui-now`,
`ui-memory`, `ui-receipts`, `domain-orchestration`, `data-connectors`,
`intelligence-gateway`, `execution-android`, `verification`, `receipts`,
`security`, `observability`, and `test-fixtures` do not exist yet.

## Building

```bash
# Domain/governance layer — works anywhere with a JDK, no Android SDK needed.
./gradlew :core:domain-model:test :core:domain-policy:test

# Full app — needs the Android SDK and a route to dl.google.com.
./gradlew :app:assembleDebug
```

## Architecture references

This implementation follows three planning documents (not checked into
this repo — they're external planning artifacts, referenced by name):

- **Ducit Intelligence-to-Implementation Dossier v1.0** — the execution
  program, sprint plan, and backlog this README's status section tracks.
- **Ducit Verbal Reference Implementation v1.0** — the engineering
  baseline: invariants, schemas (Appendix A), risk tiers, transaction
  protocol, phased build order.
- **Ducit Inner Workings & Data Flow Architecture v1.1** — the
  least-privilege data-flow model, three-registry system, and 18-
  capability ontology.

Decisions this repo has *not* made are tracked as open ADRs in
[`docs/adr/`](docs/adr/) rather than being silently assumed.
