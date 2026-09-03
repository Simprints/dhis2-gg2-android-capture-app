# Simprints upgrade 3.4 notes

Use this file as the temporary working notes for the Simprints upgrade to 3.4.

## Purpose

This file is for:
- temporary upgrade progress
- conflict decisions taken during the current upgrade
- unresolved questions
- follow-up checks before closing the upgrade

This file is not for:
- stable merge rules
- final customization inventory
- long-term functional documentation

## Header

- Client: `simprints`
- Target version: `3.4.1`
- Base branch: `develop-eyeseetea`
- Base commit at onboarding time: `73a7eb8f0fcc127d37d8887144673e480c4d5b93`
- Base commit updated to: `938b819597` (`develop-eyeseetea` after upgrade 3.4.1, PR #323)
- Current merge-base with upgrade branch: `d87193d003a0acccc53914f88026719df6fe8fc3`
- Upgrade branch: `feature-simprints/upgrade_3.4.1` (renamed from `feature-simprints/upgrade_3.4`)
- Started on: `2026-05-12`
- Status: `in_progress`

Note: a first merge attempt against `develop-eyeseetea@73a7eb8f0f` (3.4) was aborted before commit, with no changes to `eyeseetea-docs/` — restarting the merge against `develop-eyeseetea@938b819597` (3.4.1) instead.

Note: onboarding Phases 4-5 completed 2026-08-07 (OpenSpec specs for the 13 active customizations, `openspec/config.yaml`, root `CLAUDE.md`, `.claude/` scaffolding). The upgrade proposal `openspec/changes/upgrade-to-3-4-1/` was created the same day (`proposal.md`, `design.md`, `tasks.md`) and is the authoritative plan for this merge — this file continues to track temporary, session-level progress and conflict decisions per its own Purpose. Merge itself is starting now.

## Progress

- baseline prepared: `yes`
- merge started: `yes` (2026-08-07)
- easy conflicts resolved: `yes`
- manual conflicts pending: `no` — **all 65 conflicts resolved**
- build verified: `yes` — `./gradlew assembleSimprintsDebug` succeeds; APK
  `dhis2-v3.4.1-simprints-fork-1-feature-simprints-upgrade_3.4.1.apk` produced
- tests run: `no` — next step
- validation started: `no`

Getting to a green build took ~86 compile errors across 15 files **after** the
last conflict was resolved. None of them had conflict markers: they were
Simprints code calling APIs baseline had migrated (SDK types → domain types,
`queryData` → `queryDataList`, moved packages, changed constructors). The
conflict list was the smaller half of the work.

Note on tooling: a `Storage for [...lookups.tab] is already registered` KSP
failure and a batch of phantom Java errors both disappeared after
`./gradlew --stop` plus deleting `app/build/kspCaches` — stale daemon state, not
real problems. Worth trying that before chasing errors that make no sense.

Merge is still open (not committed). Final checks run after the last conflict:
no leftover conflict markers anywhere in the staged tree, and no dropped imports
(the scan flags many symbols, but all inspected ones are false positives —
baseline moved them to new packages, e.g. `SyncStatusController` →
`org.dhis2.mobile.sync.domain`, `SearchParametersUiState` →
`org.dhis2.tracker.search.ui.state`; the `Enrollment` hits in `SearchTeiModel`
are `DomainEnrollment` plus the word inside customization comments).

## What has been done so far

- `eyeseetea-docs` was brought from `develop-eyeseetea` into `feature-simprints/upgrade_3.4`.
- Simprints onboarding files were created:
  - `eyeseetea-docs/customizations/simprints/customization-files.md`
  - `eyeseetea-docs/customizations/simprints/customization-specs.md`
  - `eyeseetea-docs/upgrade/simprints/upgrade-validation-checklist.md`
- The active Simprints scope was reduced to biometrics-related behavior only.
- The following areas were explicitly classified as not to preserve during merge:
  - login / OpenID / 2FA
  - notifications
  - change server URL
  - granular sync flavor wiring
- A first brownfield extraction of Simprints biometrics behavior was completed from code and `EyeSeeTea customization` comments.

## Decisions

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| `eyeseetea-docs/customizations/simprints/customization-files.md` | accept_ours | onboarding inventory for Simprints | `n/a` | resolved_keep_ours | New client-specific inventory file created on this branch. |
| `eyeseetea-docs/customizations/simprints/customization-specs.md` | accept_ours | temporary brownfield narrative draft | `n/a` | resolved_keep_ours | New client-specific draft created on this branch; later content may move to OpenSpec. |
| `eyeseetea-docs/upgrade/simprints/upgrade-validation-checklist.md` | accept_ours | client validation scaffold | `n/a` | resolved_keep_ours | New client-specific validation checklist created before merge. |
| `eyeseetea-docs/upgrade/simprints/upgrade-3.4-notes.md` | accept_ours | temporary upgrade notebook | `n/a` | resolved_keep_ours | This file tracks temporary progress and merge decisions for Simprints 3.4. |
| `app/src/simprints/**` | accept_ours_then_review_on_theirs | preserve flavor resources, branding, and client-specific source set contents | flavor identity surface | pending | Direct Simprints flavor surface documented in onboarding inventory and should not be dropped during merge. |
| `app/src/simprintsDebug/**` | accept_ours_then_review_on_theirs | preserve debug flavor resources and client-specific debug source set contents | flavor identity surface | pending | Keep unless a specific file is later proven to be obsolete drift. |
| `app/src/simprints/java/org/dhis2/data/user/UserComponentFlavor.kt` | accept_ours_then_review_on_theirs | preserve Simprints DI flavor entry point | flavor identity surface | pending | Explicit direct flavor code entry point documented during onboarding. |
| `login/**` | accept_theirs | remove Simprints drift in login/OpenID/2FA areas | `removed` | pending | Simprints does not preserve login/OpenID/2FA custom behavior. |
| `app/src/main/java/org/dhis2/data/notifications/**` | accept_theirs | remove notifications drift | `removed` | pending | Notifications are explicitly out of scope for preservation. |
| `app/src/main/java/org/dhis2/usescases/notifications/**` | accept_theirs | remove notifications drift | `removed` | pending | Notifications are explicitly out of scope for preservation. |
| `app/src/main/java/org/dhis2/utils/session/ChangeServerURL*` | accept_theirs | remove change-server-url drift | `removed` | pending | Change server URL must not survive the merge. |
| `app/src/simprints/java/org/dhis2/data/user/GranularSyncModule.kt` | accept_theirs | remove granular-sync flavor drift | `removed` | pending | Granular sync flavor wiring is not a Simprints customization to preserve. |
| `app/src/main/java/org/dhis2/data/biometrics/**` | manual_reapply_on_theirs | preserve Simprints biometrics integration over 3.4 baseline | active biometrics customization set | pending | Core customization area. Must be merged carefully against the 3.4 baseline. |
| `app/src/main/java/org/dhis2/usescases/biometrics/**` | manual_reapply_on_theirs | preserve Simprints-specific biometrics behavior | active biometrics customization set | pending | Includes age, module-id, sequential search, dashboard biometrics, duplicates, and UI helpers. |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/**` | manual_reapply_on_theirs | preserve Simprints search integration only where biometrics is involved | biometric search integration | pending | Search is customized for biometric identify, duplicate review, confirm identity, and TEI card behavior. |
| `app/src/main/java/org/dhis2/usescases/enrollment/**` | manual_reapply_on_theirs | preserve form-side biometrics registration flow | enrollment / TEI form biometrics | pending | Enrollment keeps registration, duplicates, and `registerLast`, but not verification. |
| `app/src/main/java/org/dhis2/usescases/teiDashboard/**` | manual_reapply_on_theirs | preserve dashboard biometrics registration and verification | TEI dashboard biometrics | pending | Dashboard has both register and verify behavior. |
| `app/src/main/java/org/dhis2/data/forms/**` | manual_reapply_on_theirs | preserve form integration points for biometrics | enrollment / TEI form biometrics | pending | Only the biometrics-related pieces should survive. |
| `app/build.gradle.kts` | defer_after_build_verification | classify flavor definition vs baseline drift | `n/a` | needs_validation | Likely contains valid flavor surface plus technical drift. |
| `settings.gradle.kts` | defer_after_build_verification | classify local build and dependency drift | `n/a` | needs_validation | Needs comparison against EyeSeeTea 3.4 baseline. |
| `gradle/libs.versions.toml` | defer_after_build_verification | classify release identity vs dependency drift | `n/a` | needs_validation | Needs comparison against EyeSeeTea 3.4 baseline. |

## Confirmed functional findings from onboarding

- Config sync and selection:
  - biometrics configs are downloaded and stored locally
  - one active config is selected per entered program
  - precedence is `program` -> `orgUnitGroup` -> `default`
  - `default` is mandatory
- Config-driven biometrics behavior reviewed:
  - `biometricsMode`
  - `ageThresholdMonths`
  - `dateOfBirthAttribute`
  - `confidenceScoreFilter`
  - `orgUnitLevelAsModuleId`
  - `lastVerificationDuration`
  - `lastDeclinedEnrolDuration`
  - `enableIdentificationForTET`
- `BiometricsClient` reviewed:
  - built from active preferences by `BiometricsClientFactory`
  - sends metadata including fork version, TEI id, org unit context, user org units, and subject age
  - forces JSON responses with `versionCode=20250102`
  - maps register / identify / verify / confirm identity / `registerLast` responses back to DHIS2 models
  - preserves credential-linked matches below threshold
  - can propagate scanned credential data back into DHIS2
- Search and duplicate handling reviewed:
  - Simprints GUIDs are reused as biometrics attribute values in DHIS2 search
  - duplicate review is backed by DHIS2 search, not a disconnected list
  - duplicate flow can confirm identity, open existing dashboard, or continue with `registerLast`
- UI behavior reviewed:
  - `TEICardMapper` keeps biometrics and NHIS rows visible even when other empty attributes are hidden
  - cards decorate biometrics/NHIS rows with specific markers
  - avatar initials come from name/surname attributes
- Dashboard vs form distinction confirmed:
  - `TEI dashboard`: register + verify
  - `Enrollment / TEI form`: register + duplicates + `registerLast`, no form-driven verify found

## Baseline reference note

- `73a7eb8f0fcc127d37d8887144673e480c4d5b93` is the `develop-eyeseetea` branch head that was current when onboarding started.
- `d87193d003a0acccc53914f88026719df6fe8fc3` is the current `merge-base` with `feature-simprints/upgrade_3.4`.
- Use the branch head when describing the intended 3.4 baseline reference.
- Use the merge-base when reviewing unexplained shared drift or reconstructing what diverged on the Simprints branch.

## WIDP leftovers inherited by this branch

Verified 2026-08-07, before starting the 3.4.1 merge.

This branch carries WIDP customizations it never used, inherited through the
historical merge `f81f1dc3f3` (`origin/feature-widp/bring_last_changes_3_3_0_1`
into `feature-simprints/bring_last_changes_3_3_0_1`). Files that exist here but
not in `develop-eyeseetea` include:

- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorRequiredException.kt`
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorState.kt`
- `login/src/androidInstrumentedTest/kotlin/screen/TwoFAToEnableScreenTest.kt`
- `commonskmm/.../auth/OpenIdController.kt`, `OpenIdControllerImpl.kt`
- plus 2FA-related blocks inside shared files (`CredentialsViewModel.kt`,
  `CredentialsScreen.kt`, `LoginRepositoryImpl.kt`, `LoginResult.kt`)

Context: `develop-eyeseetea` was created from `develop-widp` (the cleanest fork
at the time) and had the WIDP customizations deliberately removed. The removal
is recorded in baseline history as `87c5da0109 "Remove 2factor customization"`.

Decision: **do not delete these manually before the merge.** `87c5da0109` is an
ancestor of `develop-eyeseetea@938b819597` but not of this branch, so the 3.4.1
merge itself will bring the deletion through a normal three-way merge — the same
way baseline was cleaned. This is the correct mechanism, not an accident.

Caveat to verify after merging (tasks 3.3 and 7.1): the deletion may surface as a
real conflict rather than a silent auto-delete, because Simprints pulled upstream
and WIDP bring-forward merges that touched the same files after that point.
Confirm the files are actually gone post-merge; do not assume.

## 3.4.1 merge preclassification (2026-08-07)

Merge of `origin/develop-eyeseetea@938b819597` run with `--no-commit --no-ff`.
Result: **65 conflicted files** — 45 `UU` (both modified), 12 `AA` (add/add),
8 `UD` (modified here, deleted in baseline).

### A. `AA` — tooling and docs added on both sides

Caused by this branch adding `.claude/` scaffolding via `git checkout` from
baseline (no merge history) plus `openspec update` to CLI 1.8.0, while the
merge now brings baseline's own copies of the same paths.

| File | Classification | Expected delta | Status |
|------|----------------|----------------|--------|
| `.claude/commands/opsx/*.md` (4) | accept_ours | keep the CLI 1.8.0 regenerated versions (baseline is still 1.2.0) | pending |
| `.claude/skills/openspec-*/SKILL.md` (4) | accept_ours | same — ours is the newer generation | pending |
| `CLAUDE.md` | manual_reapply_on_theirs | accept the upstream (Oslo) file as-is and add one `@AGENTS-simprints.md` import line; move all Simprints content into the new `AGENTS-simprints.md`. See B1 below. | pending |
| `.github/workflows/eyeseetea-main.yml` | accept_theirs | CI is baseline-owned | pending |
| `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` | accept_theirs | baseline inventory, must not be edited from a client fork (conflict-rules.md) | pending |
| `eyeseetea-docs/upgrade/conflict-rules.md` | accept_theirs | shared merge rules are baseline-owned | pending |

### B. `UD` — modified here, deleted in baseline

These are the WIDP/notifications/granular-sync leftovers this branch never
used. Baseline deleted them deliberately; the deletion should stand.

| File | Classification | Expected delta | Status |
|------|----------------|----------------|--------|
| `app/src/main/java/org/dhis2/data/service/SyncDataWorker.java` | accept_theirs (accept deletion) | granular sync wiring, out of scope | pending |
| `app/src/main/java/org/dhis2/data/service/SyncDataWorkerModule.kt` | accept_theirs (accept deletion) | granular sync wiring, out of scope | pending |
| `app/src/main/java/org/dhis2/data/service/SyncInitWorkerModule.kt` | accept_theirs (accept deletion) | granular sync wiring, out of scope | pending |
| `app/src/main/java/org/dhis2/data/service/SyncMetadataWorkerModule.kt` | accept_theirs (accept deletion) | granular sync wiring, out of scope | pending |
| `app/src/main/java/org/dhis2/usescases/eventsWithoutRegistration/eventCapture/EventCaptureRepositoryImpl.java` | defer_after_build_verification | confirm no biometrics dependency before accepting deletion | pending |
| `app/src/main/java/org/dhis2/usescases/main/HomeRepositoryImpl.kt` | defer_after_build_verification | confirm no biometrics dependency before accepting deletion | pending |
| `app/src/test/java/org/dhis2/data/notifications/NotificationD2RepositoryTest.kt` | accept_theirs (accept deletion) | notifications, out of scope | pending |
| `commons/src/main/java/org/dhis2/commons/prefs/BasicPreferenceProviderImpl.kt` | manual_reapply_on_theirs | **restore** — see "BasicPreferenceProvider decision" below | resolved_manual_merge |

### C. `UU` — active customization areas (manual reapply)

Files in the biometrics/search/dashboard/program surface documented in
`customization-files.md` section 2. All `manual_reapply_on_theirs`.

| File | Linked capability | Status |
|------|-------------------|--------|
| `data/service/SyncPresenter.java`, `SyncPresenterImpl.kt` | Biometrics Configuration Selection | pending |
| `usescases/main/program/ProgramViewModel.kt`, `ProgramModule.kt`, `ProgramViewModelFactory.kt` | Biometrics Configuration Selection | pending |
| `usescases/searchTrackEntity/SearchTEIViewModel.kt`, `SearchTEActivity.kt`, `SearchTEPresenter.java`, `SearchRepository.java`, `SearchRepositoryImpl.java`, `SearchTEModule.java`, `SearchTeiModel.java`, `SearchTeiViewModelFactory.kt`, `listView/SearchTEList.kt` | Biometric Search Integration / Duplicate Review | pending |
| `usescases/searchTrackEntity/ui/mapper/TEICardMapper.kt` | Biometrics In TEI Cards | pending |
| `usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt`, `TEIDataFragment.kt`, `TEIDataModule.kt` | Biometrics In TEI Dashboard | pending |
| 7 matching `app/src/test/**` files | tests for the above | pending |

### D. `UU` — shared/base areas (not Simprints customizations)

| File | Classification | Status |
|------|----------------|--------|
| `login/**` (`CredentialsScreen.kt`, `CredentialsViewModel.kt`, `build.gradle.kts`) | accept_theirs | pending |
| `commonskmm/.../DomainErrorMapper.kt` | accept_theirs | pending |
| `form/.../FormView.kt`, `FieldProvider.kt` | manual_reapply_on_theirs (biometrics form fields) | pending |
| `app/src/main/res/values*/strings.xml`, `styles.xml`, `layout-land/activity_dashboard_mobile.xml` | manual_reapply_on_theirs | pending |
| `stock-usecase/src/main/res/values-ru/strings.xml` | accept_theirs | pending |
| `app/src/main/java/org/dhis2/AppComponent.java`, `MainActivity.kt`, `SyncGranularRxModule.kt`, `EventCaptureActivity.kt` | defer_after_build_verification | pending |
| `app/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml` | defer_after_build_verification | pending |
| `.gitignore`, `app/src/androidTest/assets/databases/dhis_test.db` | accept_theirs | pending |

### `UD` batch analysis (2026-08-07)

Method used for each "deleted in baseline, modified here" file — three checks,
because the first two alone gave a wrong answer once (see `HomeRepositoryImpl`):

1. Does Simprints have non-merge commits of its own on the file?
2. Is the file referenced from biometrics/simprints code?
3. **Does the file itself contain biometrics/Simprints code?** (added after
   check 1-2 nearly caused `HomeRepositoryImpl` to be deleted — customization
   living *inside* a baseline file is invisible to the first two checks)

Analysing `develop-widp` was considered and rejected: it has evolved
independently since the fork, and it cannot answer the question that actually
matters, which is whether *Simprints* needs the code today.

| File | Verdict | Evidence |
|------|---------|----------|
| `SyncDataWorker.java` | accept deletion | Worker removed in baseline. Its only non-Oslo line was `presenter.destroy()` (`af75d31095c`), which came from the **PSI** fork, not Simprints — discard. |
| `SyncDataWorkerModule.kt`, `SyncInitWorkerModule.kt`, `SyncMetadataWorkerModule.kt` | accept deletion | Contain `BiometricsConfigRepository` wiring, but only as Dagger plumbing for the workers baseline deleted. The real behavior lives in `SyncPresenterImpl.downloadBiometricsConfig()`, preserved separately. |
| `NotificationD2RepositoryTest.kt` | accept deletion | Notifications, out of scope. No biometrics content. |
| `EventCaptureRepositoryImpl.java` | accept deletion | Migrated to `.kt` in baseline. Its only delta was `programStageName()` returning `displayName()` without a fallback; baseline's version adds `?: programStage.uid()` and is better. Nothing to carry over. |
| `HomeRepositoryImpl.kt` | **manual_reapply_on_theirs** | **Do not delete.** Moved to `main/data/` *and* heavily rewritten in baseline (suspend/Result, new constructor). Contains live Simprints customization: an `init` block cleaning corrupted biometrics GUIDs (`BIOMETRICS_SEARCH_PATTERN` / `BIOMETRICS_FAILURE_PATTERN`), the `deleteBiometricsAttributeValue()` helper, and `BIOMETRICS_PERMISSION`. Must be reapplied on top of the new version — and reconsidered for better placement, since an `init` block doing DB deletions inside an Oslo file is placement level 4 (worst). |

Related follow-up for `SyncPresenterImpl.kt` when it is resolved:
- discard `destroy()` / `job.cancel()` — PSI customization, absent from
  baseline's interface, and its only caller (`SyncDataWorker`) is gone.
- discard `syncMetadata(SyncMetadataWorker.OnProgressUpdate)` — baseline has no
  `syncMetadata` at all, and the parameter type belongs to a worker deleted in
  this batch. It currently survives only because the file is still unresolved
  (it is on the "ours" side of the conflict); accepting baseline's structure
  removes it and clears the dangling reference.
- confirm `downloadBiometricsConfig()` still has a caller in baseline's sync
  flow now that the old workers are deleted.

### Other-fork conflicts resolved as `accept_theirs` (2026-08-07)

Isolated by extracting the "ours" side of every conflict block and scoring it
for biometrics/Simprints symbols vs other-fork symbols (2FA, notifications,
OpenID, change-server-URL). Files with other-fork content and **zero**
biometrics content, verified across the whole file, not just the hunk:

| File | Discarded content | Simprints non-merge commits |
|------|-------------------|------------------------------|
| `login/.../ui/screen/CredentialsScreen.kt` | `TwoFactorContainer` composable (WIDP 2FA) | 0 |
| `login/.../ui/viewmodel/CredentialsViewModel.kt` | `LoginResult.TwoFactorError` handling (WIDP 2FA) | 0 |
| `app/src/main/java/org/dhis2/AppComponent.java` | `NotificationsModule` import (WIDP notifications) | 2, none in the conflicted hunk |
| `app/src/main/java/org/dhis2/usescases/main/MainActivity.kt` | `R.id.change_url` branch, **already commented out** (WIDP change-server-URL) | 6, none in the conflicted hunk |

Confirmed after resolving: `TwoFactorState.kt`,
`TwoFactorRequiredException.kt` and `TwoFAToEnableScreenTest.kt` are gone from
the tree with no dangling references — the merge applied baseline's
`87c5da0109` deletion automatically, exactly as predicted in task 1.4. No
manual WIDP cleanup was needed.

### Easy conflicts resolved (2026-08-07)

| File | Resolution | Rationale |
|------|------------|-----------|
| `.github/workflows/eyeseetea-main.yml` | **accept_ours** | Identical to baseline except the Gradle task: ours runs `:app:testSimprintsDebugUnitTest`, baseline runs `:app:testEyeseeteaDebugUnitTest`. A fork's CI must run its own flavor's tests. |
| `commonskmm/.../error/DomainErrorMapper.kt` | accept_theirs | Ours only added WIDP 2FA error mappings (`INCORRECT_TWO_FACTOR_CODE_*`, `USER_ACCOUNT_DISABLED/LOCKED`). 0 Simprints commits, 0 biometrics content. |
| `stock-usecase/src/main/res/values-ru/strings.xml` | accept_theirs | Russian translations, no biometrics content. |
| `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` | accept_theirs | Baseline adds a new "Oslo bug fixes active in this baseline" section; we add nothing. `conflict-rules.md` explicitly forbids editing this file from a client fork. |
| `eyeseetea-docs/upgrade/conflict-rules.md` | accept_theirs | Baseline adds the `// EyeSeeTea fix` comment convention for Oslo regressions; shared merge rules are baseline-owned. |
| `app/src/androidTest/assets/databases/dhis_test.db` | accept_theirs | Binary, so inspected with sqlite3: baseline's schema is newer (`user_version` 180 vs 170, 143 tables vs 136) and **neither** version contains the Simprints biometrics attribute `KdZcTAZfIk4`. Our copy came from `9f137d2218` ("Fix compilation bug to execute androidTests"), a regenerated test DB with no Simprints data. Keeping ours would break androidTests on the 3.4.1 schema. |

Note: `eyeseetea-main.yml` was initially classified `accept_theirs` in the
preclassification table above — that was wrong and would have stopped CI from
running the Simprints flavor's tests. Corrected before resolving.

### Build and resource conflicts resolved manually by the developer (2026-08-07)

Resolved directly by the developer, not by the agent: `.gitignore`,
`app/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`,
`login/build.gradle.kts`, `app/src/main/res/values/strings.xml`,
`values-es/strings.xml`, `values/styles.xml`.

Post-merge fork identity check passed on the result:
- `vName = 3.4.1-simprints-fork-1`, `vCode = 156` (version bumped to the target)
- `create("simprints")` flavor and `applicationId = org.simprints.dhis2` intact
- `libs.eyeseetea.libsimprints` dependency still present
- `app/src/simprints/` and `app/src/simprintsDebug/` source sets present
- 27 biometrics strings preserved in `values/strings.xml`

This closes the `defer_after_build_verification` group from the
preclassification table, ahead of the build check in task 6.2.

### `CLAUDE.md` resolved as an Oslo-file customization (2026-08-07)

`CLAUDE.md` became an **upstream Oslo file** in 3.4 (`2deafc54c5`, PR #4778),
alongside `AGENTS.md`. The Phase 5 fork-specific `CLAUDE.md` written from
`CLAUDE.md.template` therefore overwrote an Oslo file — placement level 4, the
worst option, guaranteeing a whole-file conflict on every future upgrade.

Resolution applied (placement level 2 — new file instead of editing Oslo code):
- All fork content moved to a new root file `AGENTS-simprints.md`.
- `CLAUDE.md` takes Oslo's version verbatim plus one import line
  (`@AGENTS-simprints.md`) with a customization comment.
- `AGENTS.md` left byte-identical to baseline.

Verified against the Claude Code docs: `CLAUDE.md` supports multiple `@file`
imports, arbitrary filenames, relative paths and up to four hops of nesting
(https://code.claude.com/docs/en/memory.md#import-additional-files).

Content was also refreshed while moving: version `3.4.1-simprints-fork-1`,
12 modules (`ui-components` is gone after the merge), the
`// EyeSeeTea fix` convention baseline just added, and a new rule capturing the
`HomeRepositoryImpl` lesson (customization can live *inside* a baseline file, so
grep the file's own contents before accepting a deletion).

Future upgrades should now conflict on at most those 4 added lines. See B1 in
"Improvements to promote to `develop-eyeseetea`" for the template fix.

### Layer 1 — models and contracts (resolved 2026-08-07)

Conflicts are being resolved **by dependency layer**, not by capability:
contracts → implementations → DI wiring → UI → tests. Resolving a constructor
or interface before the code that consumes it means deciding a signature blind
and reworking it later.

| File | Resolution | Kept | Discarded |
|------|------------|------|-----------|
| `data/service/SyncPresenter.java` | accept_theirs | nothing | `logTimeToFinish`, `updateProyectAnalytics`, `initSyncControllerMap`, `finishSync`, `setNetworkUnavailable`, `destroy` — all with **0 callers** in the tree. The one live `.destroy()` call (`SplashActivity:91`) targets a different presenter. |
| `searchTrackEntity/SearchRepository.java` | manual_reapply_on_theirs | `updateAttributeValue()` (2 live callers: `BiometricsDuplicatesDialogPresenter:115`, `SearchTEPresenter:439/451`) and `getUserOrgUnits()` (`SearchTEPresenter:470`) | `getFetchedTeiUIDs`, `getSavedSearchParameters`, `getSavedFilters` — 0 callers |

`SearchRepository.java` delta against baseline is exactly the two methods
appended at the end of the interface with their canonical-title customization
comments — placement level 3, no inline edits to Oslo declarations.

`SearchTeiModel.java` was deliberately **deferred**: its consumers
(`TEICardMapper`, `SearchTEPresenter`) are still conflicted, so which getters
survive cannot be decided yet. `allAttributeValues` has 6 live uses in
`TEICardMapper` and is customization that must be preserved.

Known temporary breakage: `SyncPresenterImpl.kt` still declares `override fun`
for methods just removed from `SyncPresenter`. Expected — those overrides live
on the unresolved "ours" side of that file and disappear when it is resolved.

### Automerge casualty: `addToAllAttributes` (found 2026-08-07, pending fix)

**This is the Automerge verification rule firing for real.** Neither
`SearchRepositoryImplKt.kt` nor `TEICardMapper.kt` was ever reported as
conflicted — both merged clean (`M`) — yet a customization was silently lost.

The customization: Simprints stores **all** TEI attributes, including those
with `displayInList = false`, because biometrics and NHIS attributes must stay
visible on search cards even when other empty attributes are hidden. Baseline
only stores the `displayInList` ones.

Broken chain, three points:

| Point | File | Merge status | Problem |
|-------|------|--------------|---------|
| Definition | `SearchTeiModel.java` | conflicted | Baseline deleted the `allAttributeValues` field, its getter, and `addToAllAttributes()` |
| Population | `SearchRepositoryImplKt.kt` | **resolved (M)** | Baseline rewrote the mapper as `mapTrackedEntitySearchItemResultToSearchTeiModel()`; it calls `addAttributeValue`/`addTextAttribute` but **not** `addToAllAttributes` |
| Consumption | `TEICardMapper.kt` | **resolved (M)** | Still has 6 uses of `allAttributeValues` — **would not compile** |

Complication: baseline also changed the attribute type in the *search* flow
only, from the SDK's `TrackedEntityAttributeValue` to its own
`TrackedEntitySearchItemAttributeDomain`:

| Old (SDK) | New (domain) |
|-----------|--------------|
| `.trackedEntityAttribute()` | `.attribute` |
| `.value()` | `.value` |
| `.displayName()` | `.displayName` |
| `.valueType()` → `ValueType` | `.valueType` → `TrackerInputType` |

The TEI/dashboard and enrollment flows still use the SDK type — verified:
`DashboardProgramModel.trackedEntityAttributeValues` is
`List<TrackedEntityAttributeValue>` in **both** this branch and baseline, and
is not conflicted. So the two types coexist by design, not transiently.

That means `isUnderAgeThreshold()` / `getAgeInMonthsByAttributes()` in
`AgeInMonths.kt` are called from both worlds:

| Caller | Data source | Type |
|--------|-------------|------|
| `EnrollmentPresenterImpl.kt:577` | `getTeiByUid(...).trackedEntityAttributeValues()` | SDK |
| `TEIDataPresenter.kt:773` | `dashboardModel.trackedEntityAttributeValues` | SDK |
| `TEIDataFragment.kt:346` | `DashboardEnrollmentModel.trackedEntityAttributeValues` | SDK |
| `TEICardMapper.kt:169, 600` | `searchTEIModel.allAttributeValues` | **domain** |

Decision: **overload, do not replace** the signature. Changing it to the domain
type would break the three SDK callers; converting domain→SDK inside
`TEICardMapper` would add a fake conversion. The function only needs
`(attribute uid, value)`, so an overload is honest about the two call sites.
`AgeInMonths.kt` is a Simprints-only file (absent from baseline), so adding an
overload creates no future conflict surface. Note Kotlin needs `@JvmName` on
one of the two `List<T>` overloads because of type erasure.

Fix plan:
1. ✅ `SearchTeiModel.java` — restored `allAttributeValues` + getter +
   `addToAllAttributes()` using the **domain** type.
2. ✅ `SearchRepositoryImplKt.kt` — `addToAllAttributes` called inside the
   attribute `forEach`, **outside** the `if (displayInList && ...)` guard.
3. ✅ `AgeInMonths.kt` — added `isUnderAgeThreshold` overload (with
   `@JvmName("isUnderAgeThresholdForSearchItems")`, required because both
   overloads erase to `List` on the JVM) plus
   `getAgeInMonthsBySearchItemAttributes`. Logic is equivalent to the SDK
   version: same lookup by configured date-of-birth uid, same null/empty
   handling, same `0` fallback — only the accessors differ
   (`.attribute`/`.value` instead of `.trackedEntityAttribute()`/`.value()`).
4. ✅ `TEICardMapper.kt` — adapted to the domain accessors
   (`.trackedEntityAttribute()` → `.attribute`, `.value()` → `.value`).
   Scope was larger than the 6 `allAttributeValues` uses first counted: the
   avatar-initials code at lines 130-134 reads `attributeValues` (baseline's
   map), which is the domain type too, and line 596 had a `.value()` inside the
   confirmation-dialog block. A commented-out block around line 576 still shows
   the old accessors; left untouched since it does not compile.

Chain verified end to end after the fix: model defines and exposes
`allAttributeValues` (domain type) → `SearchRepositoryImplKt` populates it for
every attribute → `TEICardMapper` reads it with domain accessors →
`AgeInMonths.kt` accepts the domain type through the overload. No conflict
markers left in any of the four files.

##### Judgement calls made while reapplying — check these first if search cards misbehave

Both taken in `SearchRepositoryImplKt.mapTrackedEntitySearchItemResultToSearchTeiModel()`:

- **`transformedValue` was hoisted out of the `if`.** Baseline computed it only
  for `displayInList` attributes; it now runs for every attribute, because the
  "all attributes" map needs the transformed value too. Behaviourally this
  matches the pre-merge code (the old `addToAllAttributes` did its own
  transform), but it means `getTransformedValue()` /`getUnknownLabel()` are now
  called once per attribute instead of once per listed attribute. If attribute
  values render wrong, or a perf regression shows on TEIs with many attributes,
  this is the change to look at.
- **Key is `attr.displayFormName`, not `attr.displayName`.** Chosen to match the
  pre-merge implementation, which keyed the map with
  `attribute.getDisplayFormName()` (`SearchRepositoryImpl.java:457` on the
  "ours" side). This matters because `TEICardMapper` looks entries up **by key**
  — e.g. `it.key.startsWith("Traceable address")`. If cards show odd labels or
  fail to find biometrics/NHIS attributes, check this key choice first.

One deliberate difference from the pre-merge code: the old version rebuilt a
`TrackedEntityAttributeValue` via a builder (carrying `created`, `lastUpdated`,
`trackedEntityInstance`); the new one uses `attr.copy(value = transformedValue)`
on the domain type, which has no such fields. No current consumer reads them —
`TEICardMapper` only uses the attribute uid and value — but that is why the
stored object is not a like-for-like replacement.

#### Open problem (no solution decided): duplication in `AgeInMonths.kt`

Recorded as a problem to solve later, **not** as an agreed design.

`AgeInMonths.kt` contains three near-identical routines that all do "find the
date-of-birth attribute, compute age in months", differing only in how they
read the `(uid, value)` pair from whatever type the calling flow uses:

- `getAgeInMonthsByAttributes` — SDK `TrackedEntityAttributeValue`, reads
  `.trackedEntityAttribute()` / `.value()`
- `getAgeInMonthsByFieldUiModel` — `FieldUiModel`, reads `.uid` / `.value`
- `containsAgeFilterAndIsUnderAgeThreshold` — search query map, reads the key
  and the first value

Baseline's move to `TrackedEntitySearchItemAttributeDomain` in the search flow
means the fix for this upgrade adds a **fourth** variant (the overload above),
making the duplication worse. Biometrics logic also stays coupled to whichever
attribute types Oslo happens to use, so a future type change hits every one of
these helpers again.

Scope of the problem, for whoever picks it up: 3 (soon 4) helpers in
`AgeInMonths.kt`, plus 5 call sites — `EnrollmentPresenterImpl.kt:577`,
`TEIDataPresenter.kt:773`, `TEIDataFragment.kt:346`, `TEICardMapper.kt:169` and
`:600` — spanning two attribute types that coexist by design in 3.4.1.

Why not solved now: the overload keeps the upgrade to 1 Simprints-only file and
0 call-site changes. The deciding argument is attribution, not size — if a
biometrics flow misbehaves during validation, mixing the baseline move with a
redesign makes it impossible to tell which caused it. `conflict-rules.md` says
the same: reapply the minimum delta, do not widen scope mid-merge. Same
reasoning already applied to `BasicPreferenceProvider`.

To decide after the upgrade closes, together with the deferred
`BasicPreferenceProvider` → `PreferenceProvider` question, since both touch the
same area.

Marked in code with a `TODO:` comment above the new overload in
`app/src/main/java/org/dhis2/usescases/biometrics/AgeInMonths.kt`, pointing back
to this section. Whoever picks it up should find it from either end.

### Layer 2 — `SearchRepositoryImpl.java` (resolved 2026-08-07)

Base taken from baseline, then five additive blocks reapplied — no Oslo line
modified:

| Block | Content |
|-------|---------|
| static import | `updateBiometricsAttributeValue` |
| import | `BasicPreferenceProvider` |
| field | `private final BasicPreferenceProvider preferenceProvider` |
| constructor | `preferenceProvider` added as parameter **14**, after `dispatcherProvider` |
| methods | `updateAttributeValue()` and `getUserOrgUnits()` appended at end of class |

Judgement call: `preferenceProvider` was placed **last** in the constructor
rather than at its pre-merge position. This keeps baseline's 13-parameter order
untouched and makes the fork's parameter visibly additive, but it does change
the signature relative to the pre-merge code — any caller must pass it 14th.

Discarded from the "ours" side (baseline moved this work into
`SearchRepositoryImplKt.mapTrackedEntitySearchItemResultToSearchTeiModel()`):
`transformTrackedEntity`, `setEnrollmentInfo`, `getProgramInfo`,
`setAttributesInfo`, `setAttributeValue`, `isAcceptedValueType`, the private
`addToAllAttributes` helper, and `displayOrgUnit()`. Verified 0 occurrences of
each remain in the file.

Note on the private `addToAllAttributes` helper: it is **not** recreated in the
Kotlin mapper. It existed mainly to build a `TrackedEntityAttributeValue` by
hand; baseline now supplies the transformed value and the domain object, so only
the final `searchTeiModel.addToAllAttributes(displayFormName, ...)` call was
needed. The public method of the same name on `SearchTeiModel` was restored
separately (step 1 of the casualty fix).

Cross-checked after resolving: `SearchTEModule.java` constructs
`SearchRepositoryImpl` with 14 arguments, `basicPreferenceProvider` last, and
injects `BasicPreferenceProvider` at three provider methods;
`SearchTEPresenter.java` calls `updateAttributeValue` (lines 431, 443) and
`getUserOrgUnits` (line 462).

### Conflicts resolved manually by the developer, second batch (2026-08-07)

15 files resolved directly by the developer while the agent worked on
`SearchRepositoryImpl.java`: `ProgramViewModel.kt`, `ProgramModule.kt`,
`ProgramViewModelFactory.kt`, `SearchTEModule.java`, `SearchTEPresenter.java`,
`SearchTEList.kt`, `TEIDataPresenter.kt`, `TEIDataFragment.kt`,
`TEIDataModule.kt`, `SyncGranularRxModule.kt`, `EventCaptureActivity.kt`,
`activity_dashboard_mobile.xml`, `FieldProvider.kt`, `ProgramViewModelTest.kt`,
`TeiDataPresenterTest.kt`.

Agent verification on those: no leftover conflict markers in any resolved file,
and the `SearchRepositoryImpl` constructor contract holds end to end (see
cross-check above).

### ⚠️ ACCEPTED REGRESSION: biometrics config no longer syncs with metadata

**Resolved 2026-08-08** via the `PostMetadataSyncAction` hook — see "B4
implementation" below. Deliberately not fixed during conflict resolution, to keep
the merge free of new design work; fixed in its own commit once the merge compiled.
The regression description below is kept as the record of what was lost and why.
Confirmed working on device the same day (validation flow #2, manual metadata sync).

#### What was lost

`SyncPresenterImpl.syncMetadata()` used to call `downloadBiometricsConfig()`
inside the metadata download's `doOnComplete`, alongside
`updateProyectAnalytics()`, `setUpSMS()` and WIDP's `syncNotifications()`:

```kotlin
.doOnComplete {
    updateProyectAnalytics()
    setUpSMS()
    if (BIOMETRICS_ENABLED) { downloadBiometricsConfig() }   // ← Simprints
    syncNotifications()                                       // ← WIDP
}
```

Baseline deleted that whole method. Metadata sync is now the KMP use case
`SyncMetadata` in the `:sync` module. Its Oslo siblings survived there
(`updateProjectAnalytics`, `setUpSMS`); the fork hooks did not.

**User impact.** Verified what still works after the merge:

| Scenario | Biometrics config synced? |
|----------|---------------------------|
| Logout → login into Home | **Yes** — `SyncBiometricsConfig` → `LoginModule:49` → `LoginActivity:111` (`onNavigateToHome` branch), chain intact |
| User syncs metadata from settings | Was **No — regression**; **restored and verified on device 2026-08-08** via the `PostMetadataSyncAction` hook |
| Periodic background metadata sync | Was **No — regression**; **restored 2026-08-08** by the same hook. Not exercised directly, but it goes through the same `SyncMetadataWorker` → `SyncMetadata` path that was verified. |
| First login → initial sync (`SyncActivity`, `onNavigateToSync` branch) | Not verified — that branch does not call `syncBiometricsConfig`, but it does run a metadata sync, so the hook should now cover it. Confirm on device. |

Originally config still refreshed on login but stopped refreshing on metadata
sync, so a user who stayed logged in would not receive a server-side change to
the biometrics configuration until logging out and back in. The hook restores
both metadata-sync paths.

The login-time sync is a **different use case** and does not replace this one:
it covers logout/login needing fresh config without a metadata sync. Both are
required — confirmed by the developer.

#### Why it cannot be patched locally

| Option | Verdict |
|--------|---------|
| Call it from `AndroidSyncRepository.syncMetadata()` | **Impossible.** `BiometricsConfigRepository` lives in `app/`; `:sync` only depends on `:commonskmm`. Adding `:app` would be a circular dependency (`app` already depends on `sync`). |
| Decorate the `SyncMetadata` use case from `app/` DI | **Impossible.** The consumer is `SyncMetadataWorker`, **inside** `:sync`, and it injects the concrete `SyncMetadata` class, which is `final`. A decorator registered in `app/` cannot intercept it. |
| Hook in baseline | Viable — but it is baseline design, see below. |

Every workable fix touches baseline's `:sync` module. There is no way to hook
in from outside it.

#### This is a baseline problem, not a Simprints one

WIDP needs the same thing for notifications, and the pattern will recur.
Baseline removed the extension point forks relied on; it needs to provide a new
one deliberately, knowing several forks will use it.

#### Draft design (NOT agreed — starting point only)

Contract in `:commonskmm` (visible to both modules, adds no dependency to `:sync`):

```kotlin
fun interface PostMetadataSyncAction {
    suspend operator fun invoke(): Result<Unit>
}
```

`:sync` — `SyncMetadata` takes the list with an empty default, and runs it where
the old hook was (after `setUpSMS`, before `downloadMapMetadata`, around
`input(50)`). Baseline DI injects it; each fork registers **one list** with
everything it needs:

```kotlin
// fork
factory<List<PostMetadataSyncAction>> {
    listOf(PostMetadataSyncAction { /* biometrics config */ })
}
```

An earlier variant used Koin's `getAll<PostMetadataSyncAction>()` to collect
individual registrations. Dropped: two `factory<PostMetadataSyncAction>`
definitions would overwrite each other without qualifiers, and `getAll` is used
nowhere in this project (Koin 4.1.1), so its behaviour here is unverified.

Files touched: 1 new file in `:commonskmm`, ~4 lines in `:sync/SyncMetadata.kt`,
1 line in `SyncModule.android.kt`, plus fork-only files.

#### Open questions — resolved (2026-08-08)

1. **Does Koin resolve an injected `List<T>`?** **Yes.** Verified empirically with a
   throwaway spike test on Koin 4.1.1 (since deleted): a `factory<List<T>>` of a
   functional interface registered in one module resolves into a consumer in another,
   and `getOrNull() ?: emptyList()` gives a clean no-op when nothing is registered.
   Type erasure is not a problem here.
2. **Failure isolation** — decided: **log and continue**. An action that fails, or
   throws, never fails the metadata sync, and never blocks later actions.
3. **Progress reporting** — accepted as-is. Actions run in the 50→60 jump. The
   biometrics config sync is a single small datastore call, so the freeze is not
   observable in practice. Revisit if a slow action is ever registered.
4. **Ordering** — one list per fork; the fork controls order, and the implementation
   runs the list sequentially in order (covered by a test).
5. **Naming/scope** — decided: keep the specific `PostMetadataSyncAction`. A generic
   `PostSyncAction` with a phase enum designs for a case that does not exist yet and
   widens the surface being proposed to baseline. Add a second contract if the need
   appears after *data* sync.

#### B4 implementation (2026-08-08)

**Baseline-destined (propose to `develop-eyeseetea`):**

- `commonskmm/src/commonMain/.../domain/PostMetadataSyncAction.kt` — new contract,
  no EyeSeeTea marker: it is a generic extension point, not a customization.
- `sync/src/commonMain/.../domain/SyncMetadata.kt` — third constructor parameter
  defaulting to `emptyList()` (so existing call sites and tests are unaffected), plus
  `runPostMetadataSyncActions()` invoked at `input(50)`, where the old hook ran.
- `sync/src/androidMain/.../di/SyncModule.android.kt` — `factoryOf(::SyncMetadata)`
  replaced by an explicit `factory { }`. **Required:** `factoryOf` uses constructor
  reflection and does not honour the default parameter.
- `sync/src/commonTest/.../SyncMetadataTest.kt` — 4 new tests: ordering, not-run on
  sync failure, and isolation for both a returned failure and a thrown exception.

Documented in `customizations/eyeseetea/customizations-eyeseetea.md` §6.1 — baseline
inventory, since none of it is Simprints-specific.

**Fork-side** (the only Simprints-owned part, in `customization-files.md` §2.2):

- `app/src/simprints/java/org/dhis2/di/PostMetadataSyncModule.kt` — registers the
  biometrics config action.
- The same file in the other 4 flavors (`dhis2`, `dhis2PlayServices`, `dhis2Training`,
  `eyeseetea`) as an empty module. This follows the existing per-flavor DI pattern
  (`GranularSyncModule.kt`) and keeps `KoinInitialization.kt` — a pure-Oslo file with
  zero fork drift — down to a **single added line**.

**Bug the tests caught.** The first implementation put the logging inside the outer
`try` of `invoke`. In host tests `logDebug` → `android.util.Log.d` is not mocked and
throws, which the outer `try` swallowed as a *sync failure* — the exact opposite of
the intended isolation. The logging call is now itself wrapped in `runCatching`. Worth
keeping in mind when promoting: isolation must cover the logging, not just the action.

**Known limitation (pre-existing, not introduced here).**
`BiometricsConfigRepositoryImpl.sync()` catches its own exceptions and emits nothing on
error, so a failed config refresh is reported as success to the hook. This matches the
old `downloadBiometricsConfig()` behaviour exactly; changing repository semantics was
left out of this fix deliberately.

**Second bug, caught only on device.** The action first collected the repository flow
with `firstOrNull()`. That cancels the flow with an `AbortFlowException`, which
`BiometricsConfigRepositoryImpl.sync()`'s broad `catch (e: Exception)` swallowed and
logged as an error — after the sync had already succeeded. Harmless but misleading in
logcat, and invisible to the unit tests, which mock the repository. Now uses
`collect { }`, matching what `SyncBiometricsConfig` already does.

**Verified:** `:sync` tests 16/16, `assembleSimprintsDebug` builds, `dhis2` and
`eyeseetea` flavors compile, ktlint passes on every changed module.

**Verified on device (emulator, 2026-08-08).** Manual metadata sync from settings logs
`BiometricsConfig synced!` with the 6 configs, worker returns SUCCESS, and no
`AbortFlowException`. Temporary `B4DEBUG` instrumentation confirmed the whole chain —
flavor factory called → `getOrNull()` resolved the list → action ran — and was removed
afterwards.

**Process note.** The first device test appeared to fail because the APK on the emulator
was still the pre-B4 build. Always `installSimprintsDebug` before concluding a runtime
check failed; a green `assemble` does not put the code on the device.

### `SyncPresenterImpl.kt` + `SyncGranularRxModule.kt` (resolved 2026-08-07)

Both resolved as **clean `accept_theirs`** — byte-identical to baseline, nothing
reapplied.

`SyncPresenterImpl.kt`: baseline cut it from 853 to 443 lines, keeping only the
granular sync methods and moving all orchestration
(`syncAndDownloadEvents/Teis/DataValues`, `syncMetadata`, `downloadResources`,
`syncReservedValues`, `checkSyncStatus`, `startPeriodic*Work`) into the `:sync`
module. Its constructor went from 8 parameters to 4, dropping
`biometricsConfigRepository` and `notificationRepository`.

Discarded from "ours":
- `destroy()` / `job.cancel()` — PSI customization, no caller (the live
  `.destroy()` in `SplashActivity:91` targets a different presenter)
- `syncMetadata()` — absent from baseline; its parameter type belonged to a
  worker deleted earlier in this merge
- `syncNotifications()` — WIDP, out of scope
- `downloadBiometricsConfig()` — **regression, see B4 above**
- all the orchestration methods baseline moved to `:sync`

`SyncGranularRxModule.kt` had been resolved by the developer keeping
`biometricsConfigRepository` as a 5th constructor argument. That had to be
reverted: baseline's constructor only takes 4, so it would not compile. Checked
first whether keeping the injection would help the upcoming B4 work — it would
not: the hook lives in `:sync` and the fork's action is registered in the fork's
own Koin module, never through `SyncPresenterImpl`, which no longer takes part
in the metadata flow at all. The module's `@Provides` for
`BiometricsConfigRepository` was removed too: `SyncGranularRxComponent` only
injects `SyncGranularWorker` (granular sync, no biometrics), so it had no
consumers.

Verified after: `BiometricsConfigRepository` is still provided where it is
actually used — `ProgramModule.kt` (config selection per program) and
`LoginModule.kt` (login-time sync).

### `SyncPresenterTest.kt` (resolved 2026-08-07)

Clean `accept_theirs`, closing the `SyncPresenterImpl` constructor thread: the
test now builds it with the same 4 arguments the class declares.

Discarded: the `biometricsConfigRepository` / `notificationRepository` mocks and
their imports, the `whenever(biometricsConfigRepository.sync())` stub (outside
the conflict hunks, but inside the region baseline rewrote), and 4 tests that
were only in "ours" — all four covered Matomo secondary-tracker behaviour
(`updateProyectAnalytics()`), which baseline moved into the `:sync` module along
with the rest of the orchestration. They test code this class no longer owns.

### `SearchTEIViewModel.kt` — resolved by the developer, agent-reviewed (2026-08-07)

Largest conflict of the merge (11 hunks, 1414 vs 1422 lines, result 1664).
Baseline did an architectural refactor here — KMP use cases
(`searchTrackedEntities`, `fetchSearchParameters`, `fetchOptionSetOptions`),
`queryDataList` state handling, and `ValueType` → `TrackerInputType` — so it was
resolved with a merge tool rather than hunk by hunk.

Agent review found **no problems**. Checked:
- no conflict markers left
- constructor correctly combines baseline's 3 new use cases with the fork's
  `basicPreferenceProvider` and `fromRelationships`
- `isNotBiometricText()` filter correctly placed **before** `.map` inside the
  `fold(onSuccess = ...)` of the new `fetchSearchParameters` flow
- sequential biometric search intact in `onSearch()`
  (`containsAgeFilterAndIsUnderAgeThreshold`, `isSearchByBiometricsEnabled()`,
  `SequentialSearchAction`)
- `getBiometricsSearchStatus()` present
- `ValueType` import is still needed (line 571,
  `trackerValueTypeToSDKValueType`)

Two things checked and dismissed:

1. The biometrics block in `onSearch()` runs before baseline's new
   `hasMinNumberOfAttributesToSearch()` guard, so in theory it could publish a
   sequential-search state while the search itself is refused. Not reachable in
   practice: the guard is `(program.minAttributesRequiredToSearch() ?: 0) <=
   queryDataList.size`, Simprints has no minimum configured, and the block only
   runs when a non-biometric attribute is present.
2. The `when` in `getFriendlyQueryData()` migrated from `ValueType` to
   `TrackerInputType`. No type is missing — `MULTI_TEXT`→`MULTI_SELECTION`,
   `DATETIME`→`DATE_TIME`, and the old `BOOLEAN`/`TRUE_ONLY` branches map onto
   six input-type cases. Two cosmetic behaviour changes come **from baseline**,
   not from the merge resolution: `TRUE_ONLY` now renders `"label: true"`
   instead of just `"label"`, and `BOOLEAN` only renders when the value is
   exactly `"true"`/`"false"`. Affects only the search-summary text, not
   results. Accepted as baseline behaviour.

### Second automerge casualty: `SearchTEKoinModule.kt` (resolved 2026-08-07)

Same class of problem as `addToAllAttributes`: a baseline file that merged
**without any conflict** and would not have compiled.

`SearchTEKoinModule.kt` is new in 3.4.1, part of baseline's in-progress
Dagger → Koin migration. It did not exist in this branch, so git merged it
clean. Its two definitions build `SearchTeiViewModelFactory` and
`SearchTEIViewModel` with baseline's parameter list, which omits three
parameters this fork's ViewModel requires:

| Parameter | Origin | Resolvable from Koin? |
|-----------|--------|----------------------|
| `presenter` | fork — kept from pre-migration code, future refactor | No — Dagger only |
| `basicPreferenceProvider` | biometrics customization | No — Dagger only |
| `fromRelationships` | biometrics customization | No — depends on how the screen was opened |

Resolution: **both definitions removed, module left empty with an explanatory
comment**, rather than inventing wiring.

Rationale: nothing consumes them today — `SearchTEActivity` injects the factory
through Dagger (`@Inject lateinit var viewModelFactory`) and no caller resolves
the ViewModel or factory through Koin. Since none of the three parameters can be
resolved from Koin, any wiring written now would be fictional: it would compile
and look functional without ever running or being testable. Deleting the whole
file was rejected too — it would reappear on the next upgrade, by then with the
migration further along and without the Dagger fallback.

The module is still registered in `KoinInitialization.kt:60`; an empty Koin
module is valid, so no change was needed there.

**Restore both definitions when baseline finishes the migration and removes the
Dagger `SearchTEModule`** — the file comment says the same, so it can be found
from either end.

### `FormView.kt` — field hooks moved into the ViewModel (resolved 2026-08-07)

Another case of baseline relocating logic and taking a fork extension point with
it — but unlike B4, fixable inside the merge because the hook infrastructure
already existed and only the application point moved.

**What the hook does.** `EnrollmentPresenterImpl.onFieldsLoading()` (ours) takes
the list of `FieldUiModel` and: drops the biometrics attribute when the program
is not in `full` mode; and, for `BiometricsAttributeUiModelImpl`, sets value,
editability (all mandatory fields filled) and the age-threshold flag so the field
renders as the Simprints custom component instead of a normal input. Wired from
`EnrollmentActivity:144` → `FormInjector:63` → `FormView` builder.

**What changed.** Before, `FormView` received raw `FieldUiModel` from
`viewModel.items`, ran the hooks, then mapped to sections with
`formSectionMapper`. Baseline moved that mapping into `FormViewModel` — `_items`
(private) emits `FieldUiModel`, and the public `items` already emits sections. So
`FormView` no longer sees unmapped fields, and the hooks had nowhere to run.

**Resolution (option A of two considered).** `FormViewModel` gained two nullable
listener fields, applied inside its `.map { }` immediately before
`mapFromFieldUiModelList`; `FormView` assigns them in `onCreateView` (next to
`formSectionMapper` init, before `setContent`, so it does not re-run on each
recomposition) and otherwise takes baseline's `collectAsState(emptyList())` line.

The rejected alternative was exposing the unmapped flow from the ViewModel and
keeping the mapping in `FormView`: same two baseline files touched, but it
duplicates the flow (two `shareIn` over the same source) and partially reverts
baseline's refactor, which tends to cost more in later upgrades.

Mutable state on the ViewModel was chosen over constructor injection because
`FormViewModel` already exposes `var dateFormatConfig`, `var previousActionItem`
and `var filePath`, so it introduces no new pattern — and the ViewModel is built
through `Injector.provideFormViewModelFactory(...)` with a fixed parameter list,
so constructor injection would mean touching that factory and its whole chain.

Note: `formSectionMapper` is now declared and initialised in `FormView` without
being used there — that is true of **baseline itself**, not a leftover of this
resolution, so it was left untouched.

**Third automerge casualty, caught here:** `import org.dhis2.form.model.FieldUiModel`
was present in `FormView.kt` before the merge and silently disappeared. Baseline
dropped it because its own version no longer references `FieldUiModel` in this
file — but the fork's listener signatures use it in 8 places, so the file would
not compile. Spotted by the developer, not by the conflict markers (there were
none for it). Restored.

After finding it, the same check was run across every file resolved so far
(imports present in `HEAD` but missing now, and still referenced): no other real
losses. Two apparent hits were false positives — `SearchParametersUiState` is now
imported from baseline's new package `org.dhis2.tracker.search.ui.state`, and the
`Enrollment` hits in `SearchTeiModel.java` are `DomainEnrollment` plus the word
"Enrollment" inside customization comments.

Worth repeating that scan after the remaining conflicts are resolved: dropped
imports produce no conflict markers and only surface at compile time.

### `HomeRepositoryImpl` — moved by baseline, customization reapplied (resolved 2026-08-07)

Git reported `UD` (modified here, deleted in baseline). It was not a deletion but
a **move plus rewrite**: baseline deleted `usescases/main/HomeRepositoryImpl.kt`
and created `usescases/main/data/HomeRepositoryImpl.kt` with a different
constructor, `suspend`/`Result` signatures and new methods. Rename detection did
not connect them, and since the new path did not exist here, it merged with no
conflict — so both files coexisted, with `MainModule` already importing the new
one. The old file was dead code; its deletion was accepted.

**Reapplied** onto baseline's file, at the same place it ran before (the
repository `init`, i.e. when Home is opened): the corrupted-GUID cleanup. It
deletes biometrics attribute values left in an invalid state — search or failure
placeholders never replaced by a real Simprints GUID, plus empty values — which
otherwise make a TEI look like it has biometrics registered when it does not.
Brought `deleteBiometricsAttributeValue()` with it, plus 4 imports.

Alternative entry points (a dedicated use case, post-login, `MainActivity`) were
considered and rejected by the developer: choosing a new trigger is a refactor,
and mixing it into the upgrade breaks attribution if something misbehaves later.
Same reasoning as `BasicPreferenceProvider` and `AgeInMonths`.

**Correction made mid-analysis:** `checkDeleteBiometricsPermission()` and the
`BIOMETRICS_PERMISSION` constant were initially treated as Simprints
customization and staged for reapplication. They are **not ours** — they are
Oslo's, for *device* biometrics (fingerprint/face app unlock), introduced by
`ANDROAPP-7509` / `ANDROAPP-7255`. Baseline handles that flow in
`login/.../LoginRepositoryImpl.kt` (including `cryptographyManager.deleteInvalidKey()`
at line 342), and its `HomeRepository` interface no longer declares the method.
Nothing to reapply; the constant added by mistake was removed. Simprints
biometrics is external fingerprint capture through the Simprints app
(`biometricAttributeId`), unrelated to device unlock.

### Fourth automerge casualty: `BASIC_SHARE_PREFS` (found at compile time)

Surfaced by the first compilation attempt, not by any conflict marker:

```
BasicPreferenceProviderImpl.kt:7:43 Unresolved reference 'BASIC_SHARE_PREFS'
```

`const val BASIC_SHARE_PREFS` lived in
`commonskmm/.../providers/PreferenceConstants.kt`. Upstream Oslo rewrote that
file in `1f67d3d6b1` (ANDROAPP-7497, moving data sync into the `:sync` module),
adding sync constants and dropping this one. We had never touched the file, so
it merged clean — taking the constant with it.

Restored with a customization comment. It is required because we deliberately
kept `BasicPreferenceProviderImpl`, which stores the flattened biometrics
configuration in its own SharedPreferences file.

Worth noting the pattern: this is the fourth casualty of this merge and, like
the other three, it produced **no conflict marker**. Two were caught by review
(`addToAllAttributes`, `SearchTEKoinModule`), one by the developer
(`FieldUiModel` import), and this one only by the compiler — which is exactly
why the build step is not optional.

### Post-merge compile errors: biometrics code against baseline's new domain types

Once all 65 conflicts were resolved, the build surfaced ~86 errors across 15
files. None of them had a conflict marker: they are Simprints biometrics code
calling APIs that baseline migrated from SDK types to its own domain types.

Root causes, grouped:

| Pattern | Cause |
|---------|-------|
| `.uid()`, `.lastUpdated()`, `.program()` called as functions | domain types expose them as properties |
| `queryData` unresolved | baseline replaced it with `queryDataList` |
| `isOnline`, `enrolledOrgUnit`, `programInfo`, `state`, `isHasOverdue`, `overdueDate`, `enrollments` | properties moved off `SearchTeiModel` onto `SearchTeiModel.tei` |
| `blockingSetCheck`, `hasFollowUp`, `setAttributeList`, `setStatusText` | extensions moved to `searchTrackEntity.adapters.SearchTeiModelExtensions` |
| constructor mismatches | `SearchRepositoryImpl`, `SearchRepositoryImplKt` signatures changed |

#### `BiometricsDuplicatesDialogHolder` — copy of `BaseTeiViewHolder` gone stale

The developer's hypothesis was right: the duplicates dialog was written by
copying `BaseTeiViewHolder`, and the copy never tracked the original. Confirmed
by diffing them — 118 vs 116 lines, identical except for structure (`class` vs
`abstract class`, no abstract methods, no `init` block). **Zero
Simprints-specific behaviour.**

Baseline had already migrated `BaseTeiViewHolder`, so its diff was used as the
translation table:

| Before | After |
|--------|-------|
| `teiModel.isHasOverdue` | `teiModel.tei.overDueDate?.toJavaDate() != null` |
| `teiModel.isOnline` | `teiModel.tei.isOnline` |
| `teiModel.enrolledOrgUnit` | `teiModel.tei.enrollmentOrgUnit` |
| `teiModel.tei.state()` | `teiModel.tei.aggregatedSyncState?.toSDKState()` |
| `teiModel.tei.lastUpdated()` | `teiModel.tei.lastUpdated?.toJavaDate()` |
| `enrollments.hasFollowUp()` | `tei.enrollments?.hasFollowUp()` |
| `programInfo.getEnrollmentIconsData(...)` | `tei.enrolledPrograms?.getEnrollmentIconsData(...)` |
| `selectedEnrollment.program()` | `selectedEnrollment.program` |

One difference from baseline's own file: `BaseTeiViewHolder` lives in
`searchTrackEntity.adapters`, so it gets those extensions implicitly. The dialog
holder is in another package and needs them imported explicitly
(`getEnrollmentIconsData`, `hasFollowUp`, `setAttributeList`, `setStatusText`).

#### Casualties found while fixing compile errors

Two customizations were lost silently during conflict resolution and only
surfaced at compile time:

**`SearchRepositoryImpl` constructor visibility.** It was `public` before the
merge and baseline has it package-private. Taking baseline's version dropped
the modifier, which broke `BiometricsDuplicatesDialogModule` — it lives in
another package and builds its own instance to resolve duplicate candidates.
Restored as `public` with a customization comment.

**`AppComponent.plus(LoginModule)`.** `AppComponent` was resolved as
`accept_theirs` because its only conflict hunk was WIDP's `NotificationsModule`
import — but that also silently dropped the Dagger login subcomponent
declaration, which **is** ours. Baseline migrated login to KMP + Koin (the
`login/` module) and removed the Dagger `LoginComponent`; Simprints still uses
the Dagger login, and that is where `SyncBiometricsConfig` is injected to
refresh biometrics configuration after sign-in. Restored the declaration and its
import.

Related: the developer noticed `login/build.gradle.kts` no longer declares
product flavors. Baseline removed them and that is correct — the old block
listed `widp`, `psi` and `simprints` side by side, all only setting an unused
`LOGIN_TEST` BuildConfig field (verified: zero references in the codebase).
Leftover scaffolding from the pre-baseline era, rightly cleaned up.

**Missing flavor file.** Baseline added `DownloadNewVersion` as a per-flavor
class (`dhis2`, `dhis2Training`, `eyeseetea`, `dhis2PlayServices`) but no
`simprints` variant existed, so `MainModule`/`MainViewModel` could not resolve
it. Copied the `eyeseetea` variant (direct file download; `dhis2PlayServices`
uses the Play Store mechanism instead). After this, the `simprints` flavor has
the same two files as `eyeseetea` plus its own `UserComponentFlavor`.

**Package move.** `ValueExtensions.kt` moved from `org.dhis2.commons.bindings`
to `org.dhis2.bindings`. Our `Verification.kt` and
`updateNHISNumberAttributeValue.kt` kept the old import for `blockingSetCheck`.
Checked the rest of the tree: `org.dhis2.commons.bindings` still exists and is
widely used, so only that one file moved.

#### Open problem (no solution decided): Simprints still uses the Dagger login

Baseline migrated login to KMP + Koin and dropped the Dagger `LoginComponent`,
`LoginModule` and the `AppComponent.plus(LoginModule)` declaration. Simprints
keeps all three, restored above, because `LoginActivity` injects
`SyncBiometricsConfig` through them to refresh the biometrics configuration
after sign-in.

Scope: `LoginComponent.kt`, `LoginModule.kt`, `LoginActivity` and the
`AppComponent` declaration, none of which exist in baseline. Any solution has to
find a new home for the post-login biometrics sync — and note this is a
*different* use case from the metadata-sync one in B4, so both need a place.

Not addressed during the merge: migrating to the KMP login is a refactor of a
whole subsystem. To be decided with the other deferred items.

#### Open problem (no solution decided): the dialog holder duplicates `BaseTeiViewHolder`

`BiometricsDuplicatesDialogHolder` is a ~98% copy of `BaseTeiViewHolder` with no
behavioural difference. That duplication is exactly why it broke: baseline
evolved the original and the copy silently rotted until the compiler caught it.

The same is likely true of the other `BiometricsDuplicatesDialog*` files, which
account for most of the remaining compile errors — worth checking whether they
also mirror a search-list counterpart.

Scope for whoever picks it up: `BiometricsDuplicatesDialogHolder` (116 lines)
against `BaseTeiViewHolder` (118). Any solution has to account for the original
being `abstract` with two abstract members (`itemViewClick`,
`itemConfiguration`) that the dialog does not need, and for the `init` block
that sets the Compose composition strategy.

Not addressed during the merge: it is a refactor, and mixing it in would break
attribution if the dialog misbehaves in validation. Same reasoning as
`BasicPreferenceProvider` and `AgeInMonths`. To be decided together with those.

### `BasicPreferenceProvider` decision (resolved 2026-08-07)

`develop-eyeseetea` removed the whole abstraction in `1bb3974ca1 "Remove basic
preference"` — the `BasicPreferenceProvider` interface, `BasicPreferenceProviderImpl`,
and its Dagger binding in `PreferenceModule.kt` — leaving only
`PreferenceProvider` / `PreferenceProviderImpl`.

Simprints still depends on it across the biometrics surface: `AgeInMonths.kt`,
`OrgUnitAsModuleId.kt`, `BiometricsClientFactory.kt`,
`BiometricsDuplicatesDialogPresenter.kt`, `BiometricsDuplicatesDialogModule.kt`,
`EnrollmentPresenterImpl.kt`, `EnrollmentModule.kt`, `TEIDataPresenter.kt`,
`TEIDataFragment.kt`, `TEICardMapper.kt`. The selected biometrics configuration
is flattened into preferences and read back through this provider.

Decision: **restore the abstraction for now** — interface, impl, and the DI
binding (the binding carries an `EyeSeeTea customization` comment explaining
why). Rationale: `BasicPreferenceProvider` originally existed because
`PreferenceProvider` was too complex for this use; migrating ~10 biometrics
files to `PreferenceProvider` is a refactor, not an upgrade concern, and mixing
the two inside one merge commit makes both harder to review.

Follow-up (after this upgrade closes, as a separate commit): evaluate migrating
the biometrics surface to baseline's `PreferenceProvider` and dropping the
restored abstraction.

Note: the surviving `BasicPreferenceProviderImpl` differs from the pre-deletion
baseline version on one line — `setValue()` with a null value calls
`remove(key)` here instead of `clear()`. The Simprints version is correct;
`clear()` would wipe every stored preference instead of the single key.

## Improvements to promote to `develop-eyeseetea`

Findings from this upgrade that belong in the shared baseline, not in the
Simprints branch. Each is validated by having survived a real upgrade; promote
them as a single PR against `develop-eyeseetea` when this upgrade closes
(same criterion agreed for the OpenSpec scaffolding bump below).

Status values: `pending` (found, not yet promoted) / `promoted` (in a baseline PR).

### B1. `CLAUDE.md.template` assumes the fork owns `CLAUDE.md` — it no longer does

- **Baseline file:** `eyeseetea-docs/templates/CLAUDE.md.template`
- **Status:** `pending`
- **Evidence:** upstream Oslo introduced a root `CLAUDE.md` in 3.4
  (`2deafc54c5`, PR #4778, author Andrés Miguel Rubio, present in
  `origin/upstream/3.4.1`) plus `AGENTS.md` (`5fd7ace101`, `638adaa548`,
  `b69b859546`). Both are **upstream files**, not EyeSeeTea ones.
- **Problem:** the template tells each fork to write a full fork-specific
  `CLAUDE.md` at the repo root. Since 3.4 that path is owned by Oslo, so a
  fork following the template overwrites the upstream file, drops its
  `@AGENTS.md` include, and guarantees a whole-file conflict on every future
  upgrade. Simprints hit exactly this in Phase 5.
- **Fix to promote:** rewrite the template so a fork creates
  `AGENTS-<client>.md` and adds a single `@AGENTS-<client>.md` import line to
  the upstream `CLAUDE.md`. Verified against the Claude Code docs: a
  `CLAUDE.md` supports multiple `@file` imports, arbitrary filenames, relative
  paths, and up to four hops of nesting
  (https://code.claude.com/docs/en/memory.md#import-additional-files).
  This follows the project's own placement hierarchy — "new file" beats
  "edit an Oslo file" — and cuts the recurring conflict from a whole file to
  one line.
- **Also update:** `onboarding-fork-guide.md` Phase 5, which currently
  instructs copying the template to `CLAUDE.md`.

### B2. `customization-files-template.md` has no "Feat commits" section

- **Baseline files:** `eyeseetea-docs/customizations/template/customization-files-template.md`,
  `eyeseetea-docs/onboarding-fork-guide.md` (Phase 3)
- **Status:** `pending`
- **Evidence:** `eyeseetea-docs/scripts/check_upgrade_docs.py` has
  `check_feat_commit_coverage()`, which parses a `## 4.` section for
  per-customization commit SHAs and cross-checks every code file those commits
  touched against the inventory. `conflict-rules.md` calls this
  "load-bearing" for the Automerge verification rule. The template has no such
  section, so the check silently never fires.
- **Problem:** Simprints hit this in task 1.3 — no SHAs were ever recorded, and
  reconstructing them afterwards was unreliable (209 candidate commits, mixed
  with WIDP and upstream bring-forward merges).
- **Fix to promote:** add a `## 4. Feat commits` section to the template in the
  format the script parses, and document it in Phase 3 of the onboarding guide
  so SHAs get captured while the developer still knows which commits introduced
  each customization.

### B5. New `customization-techniques.md` should live in the baseline

- **Baseline files:** `eyeseetea-docs/customization-techniques.md` (new),
  plus links from `eyeseetea-docs/README.md` and `upgrade/conflict-rules.md`
- **Status:** `pending` — written on this branch, needs promoting
- **Evidence:** `eyeseetea-docs/` documents *what* each fork customizes
  (`customization-files.md`) and *how* to resolve conflicts
  (`conflict-rules.md`), but nothing documented the **mechanisms** available for
  customizing shared code. Each fork was rediscovering them.
- **Fix to promote:** move the file and its two links to `develop-eyeseetea`.
  It currently documents five techniques found during this upgrade: field hooks
  (T1, Simprints), post-metadata-sync actions (T2, needed by Simprints and WIDP —
  **implemented and device-verified 2026-08-08**, promote together with B4),
  widening visibility (T3), extra constructor parameter (T4), and copying an Oslo
  component as an anti-pattern (T5).
- **Promote B4 and B5 together.** T2 is the reusable write-up of the mechanism B4
  implements; separating them would ship a doc describing code that is not there,
  or code with no documentation. WIDP needs both to solve its notifications case.
- **Note:** WIDP should review T1 and T5 — both were written from the Simprints
  side, and WIDP may have equivalents worth recording, or a better solution.

### B4. No extension point after metadata sync (blocks two forks)

- **Baseline files:** `sync/src/commonMain/.../domain/SyncMetadata.kt`,
  `sync/src/androidMain/.../di/SyncModule.android.kt`,
  `sync/src/commonTest/.../SyncMetadataTest.kt`, a new contract in `:commonskmm`
  (`domain/PostMetadataSyncAction.kt`), one line in
  `app/src/main/.../di/KoinInitialization.kt`, the empty `postMetadataSyncModule`
  in the 4 non-Simprints flavor source sets, and the inventory entry in
  `customizations/eyeseetea/customizations-eyeseetea.md` §6.1
- **Status:** `implemented` (2026-08-08) — ready to propose to `develop-eyeseetea`
- **Inventory note:** this is a **baseline** extension point, not a fork
  customization, so it is documented in the EyeSeeTea inventory (§6 "Extension
  points added for downstream flavors"), not in Simprints'. Simprints' §2.2 only
  records that it *consumes* the hook. Keeping the contract in the fork inventory
  would have marked baseline code as Simprints-owned and pushed the next upgrade
  to treat it as fork-specific.
- **Evidence:** the old `SyncPresenterImpl.syncMetadata()` in `app/` let forks
  hook work onto the end of a metadata sync; Simprints used it for biometrics
  config and WIDP for notifications. Baseline replaced it with the KMP
  `SyncMetadata` use case in `:sync`, which has no extension point — and `:sync`
  cannot see fork code (`:app` dependency would be circular), nor can a fork
  decorate the use case (its consumer `SyncMetadataWorker` lives inside `:sync`
  and injects the `final` concrete class).
- **Problem:** Simprints has an accepted regression right now (biometrics config
  stops refreshing on metadata sync). WIDP will hit the identical problem with
  notifications on its next upgrade.
- **Fix to promote:** a `PostMetadataSyncAction` contract in `:commonskmm` that
  `SyncMetadata` runs after a successful sync, with each fork registering its own
  list in DI. **Implemented 2026-08-08** — see "B4 implementation" below for the
  resolved open questions and the final shape.

### B3. OpenSpec Claude scaffolding is generated by an outdated CLI

- **Baseline files:** `.claude/commands/opsx/*.md`, `.claude/skills/openspec-*/SKILL.md`
- **Status:** `pending`
- **Evidence:** baseline's copies carry `generatedBy: "1.2.0"`; the current CLI
  is 1.8.0.
- **Problem:** forks inheriting the scaffolding get commands/skills several
  versions behind, and miss the two workflows 1.8.0 ships (`/opsx:sync`,
  `/opsx:update`).
- **Fix to promote:** run `openspec update` on `develop-eyeseetea` and review
  the diff. Already trialed on this branch (commit `f6e3ff3ba0`);
  `openspec validate --specs --strict` still passed 13/13 afterwards.
- **Process note for the guide:** update the scaffolding **after** merging the
  baseline, not before. Doing it before (as happened here) turns all eight
  scaffolding files into `AA` add/add conflicts during the merge.

## Open Questions

- Whether to install OpenSpec for Simprints immediately after onboarding or after the first baseline merge attempt.
- Whether `app/build.gradle.kts`, `settings.gradle.kts`, and `gradle/libs.versions.toml` contain any Simprints-specific flavor identity that should be preserved beyond pure drift cleanup.
- Whether there are Simprints biometrics customizations outside the already reviewed biometrics/search/enrollment/dashboard surface.

### Tech debt: `FormView.submitIntent` workaround for the stale `FieldUiModel.Callback` (2026-09-02)

The `registerLast` fix (see "Second causa raíz" write-up for that day, `customization-files.md` §2.10) bypasses `FieldUiModel.Callback` entirely for the biometrics field, instead of fixing why the callback goes stale on the `biometricsUiModel` instance `EnrollmentPresenterImpl` holds. It works and is verified on device, but it is a workaround: it needed two new methods across three files (`FormView.submitIntent`, `EnrollmentView.submitFormIntent`, `EnrollmentActivity` impl) plus a second unrelated Oslo-file change in the same `FormView.kt` (`LaunchedEffect(Unit)` instead of `LaunchedEffect(items)`, to keep the pre-existing `pendingSave`/`onFieldItemsRendered` mechanism firing on every emission).

A cleaner alternative worth exploring later: have `EnrollmentPresenterImpl` react to `onFieldsLoaded(fields)` — which already fires per-emission from `FormViewModel.items`' `map{}`, unaffected by the Compose repaint timing that leaves `callback == null` — instead of caching a `biometricsUiModel` reference and calling methods on it later, after Simprints has come and gone. That would remove the need for both new `submitIntent` entry points.

Deferred deliberately: the priority this session was a minimal, attributable fix to unblock the 3.4.1 upgrade, not a redesign of the biometrics field's plumbing. Revisit once the upgrade is closed and validated.

### Biometric search with multiple candidates found nothing (found 2026-09-02, fixed 2026-09-03)

Reported by the user: biometric *search* (`identify`) found no matches for TEIs that should have existed, while biometric *verify* kept working. Root cause investigation spanned two sessions.

**Root cause**: 3.3.1's `SearchRepositoryImpl.getFilteredRepository()` (`app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchRepositoryImpl.java`) has always excluded the biometric attribute from a generic normalization: when a multi-value attribute has no Oslo `CustomIntent` returning a list, its values get collapsed into one comma-joined string (`!dataId.equals(biometricAttributeId) && !customIntentRepository.attributeHasCustomIntentAndReturnsAListOfValues(...) && dataValues.size() > 1`) — because Simprints candidate GUIDs are a real multi-value list, not free text where a comma could be literal. That exclusion is still present and correct in `SearchRepositoryImpl.java` today.

The problem: there are **three independent search call paths** across `SearchTEIViewModel` and `BiometricsDuplicatesDialogPresenter`, and only one of them still goes through `SearchRepositoryImpl`.
- **List view** (`SearchTEIViewModel.loadSearchResults()`/`loadDisplayInListResults()`, used when identifying a TEI from the search screen): baseline's `[ANDROAPP-7495]` search refactor (already documented elsewhere in this file for `SearchTEIViewModel.kt`) moved this path to a new KMP use case, `SearchTrackedEntities.prepareQuery()` (`tracker/src/commonMain/kotlin/org/dhis2/tracker/search/domain/SearchTrackedEntities.kt`). That file has the same generic collapse logic as `SearchRepositoryImpl` but never had the biometric exclusion — it's new baseline code with no 3.3.1 equivalent to diff against, so the automerge-casualty checklist (which compares against a known prior version) didn't catch it. **Broken.**
- **Duplicate resolution** (`BiometricsDuplicatesDialogPresenter`, ~line 95, `searchTrackedEntities.invoke(input)`): calls the *same* `SearchTrackedEntities` use case, built by its own Dagger provider in `BiometricsDuplicatesDialogModule.kt`. The Java `searchRepository` is injected into that presenter too, but only for `updateAttributeValue`/`downloadTei` — never for the search itself. **Broken**, fixed by the same change.
- **Map view** (`SearchTEIViewModel.fetchMapResults()` → `MapDataRepository.searchTeiForMap()` → `SearchRepositoryImplKt.searchTeiForMap()` → `SearchRepositoryImpl.getFilteredRepository()`): still goes through the Java repository and was never broken — `getFilteredRepository()`'s only real caller today is `searchTeiForMap()`.

Confirmed with device tracing on 2026-09-03: `identify` returning 2+ candidates always collapsed to `"guid1,guid2"` and hit `.like()` with no match on the list-view path; a single candidate happened to skip the collapse branch (`size == 1`) and worked, which is why the bug wasn't obvious in casual testing with few candidates. Verified via device trace that the list-view log trail stopped right after `SearchTEIViewModel.onSearch()` and never reached `SearchRepositoryImpl`.

**Fix**: mirrored the exclusion in `SearchTrackedEntities.prepareQuery()` (`data.attributeId != biometricAttributeId`). Since both the list-view and duplicate-resolution paths build their own instance of the same `SearchTrackedEntities` class, this single change fixes both. To keep the diff to a single added condition in this Oslo file (no new constructor parameter), `biometricAttributeId`/`nhisNumberAttributeId` were moved from `app/src/main/java/org/dhis2/usescases/biometrics/attributes.kt` to `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/biometrics/attributes.kt`, since `:commonskmm` (unlike `:commons`, which is Android-only) is on the `commonMain` classpath for both the `androidLibrary` and `jvm("desktop")` targets that `:tracker` builds for. Verified: `:tracker:compileKotlinDesktop` still compiles, `SearchTrackedEntitiesTest` (12/12) and `SequentialSearchTest` (7/7) still pass, and the fix was confirmed end-to-end on device (Lenovo TB X304F) with real Simprints `identify` responses returning 2+ candidates.

An earlier version of this fix added an `excludedAttributeIdsFromCollapse: Set<String>` constructor parameter to `SearchTrackedEntities`, injected from `:app`'s Dagger modules — rejected as a bigger conflict surface on the constructor signature than the `commonskmm` move. See `customization-files.md` §2.9 for the full file inventory.

### DHIS2 core 2.43 API research: watch item for a future 3.4.2 upgrade (2026-09-03)

The client is moving their server to DHIS2 core 2.43 on this same 3.4.1 release (the driver for this whole upgrade, see the top of this file). Read the official 2.43 release notes (`dhis2.github.io/dhis2-releases/releases/2.43/`, GitHub `dhis2/dhis2-releases`) looking for anything that could affect the 13 Simprints customizations, specifically the biometric search fix from this same session.

**Watch item — Tracked Entity Search Performance Configuration (`ROADMAP-128`).** 2.43 lets the server define, per tracked entity attribute (via the Maintenance app), a `preferredSearchOperator` and a list of `blockedSearchOperators` (`LIKE`/`EQ`/`SW`/`EW`), and the release notes explicitly say: *"Web and Android Capture use recommended operators, restrict inefficient ones, avoid LIKE by default."* This is the exact same surface as the biometric search fix above — `SearchRepositoryImpl.getTrackedEntityQuery()` (map view) and `SearchTrackedEntityRepositoryImpl.addToQuery()` (list view/duplicate resolution, via `SearchTrackedEntities`) both fall back to `.like(dataValues[0])` when there's exactly one candidate GUID (`searchOperator == null`).

**Checked directly against the SDK we ship (`dhis2-android-sdk` tag `1.14.1-eyeseetea-fork-1`, local clone), not assumed from the release notes:**
- `TrackedEntityAttribute.preferredSearchOperator()`/`.blockedSearchOperators()` (`core/src/main/java/org/hisp/dhis/android/core/trackedentity/TrackedEntityAttribute.java`) already exist as metadata fields, synced from the server and persisted (`TrackedEntityAttributeDB.kt`).
- `git grep` across the whole SDK tag for both field names, excluding the model/DTO/DB/test files that just declare or map them, returns **zero results** — nothing in query-building code (`TrackedEntitySearchOperators.kt`, the repository/filter connectors) reads or enforces them yet.
- **Conclusion: not a blocker for this 3.4.1 upgrade.** The server can tag the biometric attribute with `blockedSearchOperators: [LIKE]` today and this SDK version will keep sending `.like()` unaffected — the restriction is metadata-only in `1.14.1-eyeseetea-fork-1`.
- **Why this matters for a future 3.4.2 (or later SDK) upgrade:** if a later SDK version starts enforcing `blockedSearchOperators` client-side, and the GHS server config marks `LIKE` as blocked for the biometric attribute (plausible, since `LIKE` is explicitly called out as "commonly associated with slow performance" and "no longer selected by default" in 2.43), the single-candidate biometric search path (`size == 1` → `.like()`) could start failing or behaving differently. **Action before any future SDK bump past `1.14.1-eyeseetea-fork-1`:** re-run this same `git grep` against the new SDK tag, and if usages appear in query-building code this time, check the biometric attribute's `blockedSearchOperators` config on the client's server and re-verify the single-candidate search path on device.

Other 2.43 items reviewed and found **not** relevant to Simprints: Enrollment AOCs (`ROADMAP-140`, backend-only in 2.43, no capture/analytics app support yet); removed error codes `E1084`/`E1085` (nothing in this codebase catches those specific codes); `changelog.tracker` config move (server-side only). The `Program`/`Enrollment` `categoryCombo`/`enrollmentCategoryCombo`/`attributeOptionCombo` builder fields becoming `@NonNull` (already hit and fixed in `EnrollmentPresenterImplTest.kt` this session, see the CI-fix commit) is consistent with Enrollment AOCs backend work landing in the SDK, but is a test-fixture concern, not a runtime customization risk.

## Automerge casualties

Pending.

Use this section only if git automerge silently drops Simprints customization code in non-conflicting hunks during the real merge.

Expected format:

- `path/to/file` — short note about what was lost and how it was recovered

## Post-merge fork identity fixes

Pending.

Use this section after the merge if Simprints-specific flavor identity needs to be restored or rechecked, for example:

- flavor source sets
- flavor definitions in Gradle
- version naming
- fork-specific dependencies
- fork-specific app id / resources / branding

## Follow-ups resolved

Pending.

Move here any issue found during merge or validation once it has been closed, with date and short resolution note.

## New rules promoted to `conflict-rules.md`

Pending.

Only add rules here when a concrete Simprints merge incident reveals a reusable resolution pattern worth keeping beyond this upgrade.

## Validation Notes

- build: not started
- targeted tests: not started
- manual flows checked: code review only, no runtime validation yet

## Finalization

- surviving customizations moved to `customization-files.md`: `yes`
- stable rules moved to `conflict-rules.md`: `no`
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `yes`
