## Context

See `proposal.md` - Why / What Changes for motivation and scope. This design
covers only the merge and conflict-resolution approach for moving the
Simprints fork's implementation baseline from `develop-eyeseetea` 3.4.2
(merge-base `f87bec8c3`) to 3.5.0 (`89b2f2e3b`).

Current state:
- Branch `feature-simprints/upgrade_3.5.0` is at `develop-simprints@09cad0d3e`
  (PR #341, 3.4.2 upgrade). The merge has not started. An abandoned direct
  merge of `upstream/3.5.0` was found and discarded on 2026-10-22, because
  Oslo must never be merged directly into a fork.
- The 3.4.2 upgrade is closed and archived
  (`openspec/changes/archive/2026-09-17-upgrade-to-3-4-2/`). All 14 active
  customizations were validated against 3.4.2.
- `develop-eyeseetea` advanced by:
  - 164 Oslo commits;
  - PR #343, the Oslo 3.5.0 merge into baseline;
  - two docs commits: `3cd19be2e` (TDD / commit-granularity rules in the
    OpenSpec config template, i.e. B7 promoted) and `8e0200bcc` (AGP
    composite-build fix for a local SDK).
- Baseline-level findings from PR #343 that also apply here:
  - Oslo rewrote `UserManagerImpl` in Kotlin, without `logIn()`.
  - ANDROAPP-7666 is now in Oslo.
  - Oslo changed its `dhis2`/`dhis2PlayServices` `GranularSyncModule.kt`
    without any conflict on the flavor copies.
  - AGP moved from 9.0.1 to 9.3.1, which needs Android Studio Quail 2+ /
    Rabbit 1.
  - Oslo's new `ci.yml` is disabled in the EyeSeeTea repo.
- `eyeseetea-docs/upgrade/conflict-rules.md` is the canonical merge guide.
  This design defers to it for classification mechanics.

## Goals / Non-Goals

**Goals:**
- Merge `develop-eyeseetea` 3.5.0 with all 14 active Simprints capabilities
  intact and behaviorally unchanged.
- Classify every conflicted file, plus every file in
  `customization-files.md`, before editing it.
- Catch the two kinds of silent loss that git does not report:
  - automerge drops in shared files;
  - Oslo flavor-file changes that never reach the Simprints flavor copies.
- Leave `customization-files.md`, the validation checklist,
  `AGENTS-simprints.md` and `upgrade-3.5.0-notes.md` in a state a future
  agent can resume from cold.

**Non-Goals:**
- Changing what any capability does (`skip_specs: true`).
- Adopting new Oslo 3.5.0 features in Simprints-specific code, such as
  custom terminology labels on biometric UI strings, unless a capability
  breaks without it.
- Picking up pending baseline-promotion items (B2, B3, B6) or
  `refactors-pending.md` entries.
- Reconciling shared-drift build files beyond what is required to compile.

## Decisions

**Classification order.** Same order as 3.4.1 / 3.4.2, per `conflict-rules.md`
"Mandatory post-merge preclassification":
1. **Direct flavor files** (`app/src/simprints/**`, `app/src/simprintsDebug/**`):
   `accept_ours`. One new sub-step: diff
   `origin/upstream/3.4.2..origin/upstream/3.5.0 -- app/src/dhis2 app/src/dhis2PlayServices`
   and port any change to a file that has a Simprints copy. In this range
   that is `GranularSyncModule.kt`. Alternative considered: rely on compile
   errors. Rejected, because the 3.5.0 `GranularSyncModule` change compiled
   either way and only surfaced by diffing.
2. **`customization-files.md` §2 (14 active customizations)**: mostly
   `manual_reapply_on_theirs`, with a much larger delta than 3.4.2. Named
   priority checks, done before the bulk of the manual batch:
   - (a) the custom-terminology series across search, TEI cards and the
     dashboard, against §2.1, §2.8, §2.9 and §2.10;
   - (b) `cf72c54d5`, search error messages, against biometric search result
     handling (§2.1);
   - (c) the OAuth / offline-login / session-renewal series in
     `LoginActivity` / `LoginModule`, which must keep the
     `SyncBiometricsConfig` login path (§2.2);
   - (d) the `TeiDashboardMenu.kt` deletion, which leaves the
     `DashboardViewModel` transfer cache (§5 note) without its known caller;
   - (e) form changes (`977c1c439`, `8d2c133f6`) against the biometric form
     field (§2.10).
3. **§3 (out of scope for preservation)**: `accept_theirs`. Oslo touched
   `SyncPresenterImpl.kt` and `OpenIdSession.kt`. Confirm no Simprints
   variant survives, and grep for `biometric`/`simprints` before accepting.
4. **§4 (shared drift)**: `app/build.gradle.kts`, `settings.gradle.kts` and
   `gradle/libs.versions.toml` are `defer_after_build_verification`.
   Identity strings must end as `3.5.0-simprints-fork-1`, keep the
   `simprints` flavor, and use SDK `1.15.0-eyeseetea-fork-1` through the
   existing substitution.
5. **`.claude/skills/openspec-*`, `.claude/commands/opsx/*`**: expect `AA`
   conflicts; `accept_ours` (fork on CLI 1.8.0, baseline on 1.2.0). New
   baseline `.claude/` skills and agents are `accept_theirs`.
6. **Anything else conflicted**: classify fresh by `conflict-rules.md`
   "Default rules by path". If a real customization surfaces, flag an
   inventory gap.

**SDK fork dependency.** No new SDK patch surface is expected. The 14
capabilities depend only on public SDK APIs plus the EyeSeeTea fork's
existing patches. However, Oslo adapted to SDK breaking changes in this
range (`a919bf84a` domain model, `0d8268015` suspending functions,
`ebd6fdad7`), and Simprints flavor/shared code that calls those APIs was not
adapted. Treat the resulting compile errors as mechanical API migrations
(`accept_theirs` style), not as behavior changes. If an SDK API the
biometrics flow needs disappeared, stop and raise it with the user.

**Pause points.**
- After preclassification.
- After the easy batch, before `manual_reapply_on_theirs`.
- After the priority checks, if any of them shows a delta larger than
  expected.

**Expected-delta rule.** Kept from 3.4.1. Before editing each manual-reapply
file, write its expected Simprints delta in `upgrade-3.5.0-notes.md`. If
the resulting diff against `develop-eyeseetea` is significantly larger, stop
and redo it.

## Risks / Trade-offs

- [Automerge silently drops customization wiring outside conflicted hunks]
  → Mitigation: run the automerge verification rule over every file in
  `customization-files.md`, then do a semantic diff-scan. The surface is
  larger than in 3.4.2, so do it per inventory section.
- [Flavor copies silently go stale] → Mitigation: the flavor-diff sub-step
  in classification step 1.
- [Custom-terminology refactors reshape the methods the biometric UI hooks
  into, e.g. card mappers and dashboard models] → Mitigation: priority
  check (a). Reapply on the new shape, and don't resurrect old signatures.
- [Login rewrite drops the `SyncBiometricsConfig` call path] → Mitigation:
  priority check (c), plus a manual login check of biometrics config sync
  with metadata sync.
- [AGP 9.3.1 / kapt cache noise is mistaken for a regression] → Mitigation:
  clear the `app/build/kspCaches` and kapt dirs before the first test run,
  as in 3.4.1 and 3.4.2.
- [`connectedSimprintsDebugAndroidTest` stays blocked by the Room
  identity-hash mismatch] → Mitigation: carry it forward as a known issue
  and do not let it block. Retry it once on 3.5.0 / SDK 1.15.0 in case it
  changed.

## Migration Plan

1. Create `upgrade-3.5.0-notes.md` from the template and carry forward the
   inherited B-items.
2. Merge `develop-eyeseetea@89b2f2e3b` into `feature-simprints/upgrade_3.5.0`.
3. Preclassify and record the result in the notes. Pause.
4. Resolve `accept_ours` / `accept_theirs` and port the flavor changes. Pause.
5. Do the priority checks (a)–(e), then the remaining
   `manual_reapply_on_theirs`. Pause if a delta exceeds expectations.
6. Resolve `defer_after_build_verification` until it builds.
7. Run the automerge verification over the whole inventory.
8. Validate: unit tests and the manual checklist.
9. Close out the inventory, `AGENTS-simprints.md` and the notes; archive.

Rollback: all work is on `feature-simprints/upgrade_3.5.0`.
- Before the commit: `git merge --abort`.
- After the commit: reset to `09cad0d3e`, only with explicit user
  confirmation.

## Open Questions

- Does Oslo 3.5.0's custom terminology change any label Simprints testers
  rely on in biometric flows (e.g. "TEI" vs a configured term)? This is
  deferrable: it is observed during manual validation and does not change
  the plan.
