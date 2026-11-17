# Simprints upgrade 3.5.0 notes

Use this file as the temporary working notes for the Simprints upgrade to 3.5.0.

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
- Target version: `3.5.0`
- Base branch: `develop-eyeseetea`
- Base commit at previous upgrade close: `f87bec8c3` (3.4.2; fork closed at `develop-simprints@09cad0d3e`, PR #341)
- `develop-eyeseetea` current head: `89b2f2e3b` (Oslo 3.5.0 via PR #343, plus docs commits `3cd19be2e` and `8e0200bcc`)
- Oslo range: `origin/upstream/3.4.2..origin/upstream/3.5.0` (164 commits)
- Upgrade branch: `feature-simprints/upgrade_3.5.0`
- Plan: `openspec/changes/archive/2026-11-13-upgrade-to-3-5-0/`
- Started on: `2026-10-22`
- Status: `closed`

Previous upgrade (3.4.2) closed and archived on `2026-09-17`. See
`upgrade-3.4.2-notes.md` (kept as a historical record) and
`openspec/changes/archive/2026-09-17-upgrade-to-3-4-2/`.

`origin/develop-simprints` has no commits ahead of `09cad0d3e` (checked
2026-10-22), so no parallel fork work needs merging after this upgrade.

## Inherited context from the 3.4.2 upgrade: carry into this one

Each item was checked against `develop-eyeseetea@89b2f2e3b` before starting
this merge.

| Item | Status at 3.4.2 close | Verified now | Action for 3.5.0 |
|------|------------------------|--------------|-------------------|
| B2: "Feat commits" section in `customization-files-template.md` | pending | **still pending** (no "Feat commits" in the baseline template) | not blocking; pick it up only if this round touches that template |
| B3: OpenSpec CLI scaffolding bump 1.2.0→1.8.0 | pending | **still pending** (baseline `.claude/skills/openspec-*` still `generatedBy: "1.2.0"`) | expect `AA` conflicts on these files again; resolve `accept_ours`, as in 3.4.1 / 3.4.2 |
| B6: `fieldListChannel` `DROP_OLDEST` race | pending | **still pending** (`FormViewModel.kt:127` still `BufferOverflow.DROP_OLDEST`) | the Simprints 500ms debounce mitigation in `EnrollmentActivity` must survive; re-verify on device during the `registerLast` validation |
| B7: TDD / small-commit rules in `openspec-config.yaml.template` | applied locally, not promoted | **partially promoted** (`3cd19be2e`): baseline template has the test-first and `Commit:` granularity rules, but not the two test-quality rules (assert on observable behavior; name tests by contract) | keep the fork's `openspec/config.yaml` as is; the remaining 2 rules still need a baseline PR |

Open problems carried over (upgrade-relevant context, not tech debt):
- Simprints still uses the Dagger `LoginComponent` / `LoginModule`
  (`AppComponent.plus(LoginModule)`) to inject `SyncBiometricsConfig` after
  sign-in. Oslo 3.5.0 reworks login heavily (OAuth, offline login lockout,
  session renewal), so this is priority check 4.3 in `tasks.md`. Re-run
  the 3.4.1 restoration check if `AppComponent.java` or `LoginActivity.kt`
  conflict.
- DHIS2 core 2.43 `blockedSearchOperators` / `preferredSearchOperator` watch
  item: **status changed**. Baseline now reads both fields when building
  search parameters
  (`tracker/src/androidMain/.../search/data/SearchParametersRepositoryImpl.kt:233,238`).
  Check during priority checks 4.1 and 4.2 whether this affects the
  biometric search query path (§2.1).
- `connectedSimprintsDebugAndroidTest` is blocked by a Room identity-hash
  mismatch whose root cause was not found in 3.4.2. Retry it once on
  3.5.0 / SDK `1.15.0-eyeseetea-fork-1` (task 8.3).
  **Decision (2026-11-13, user):** not retried in this upgrade; carried forward as a known issue
  (also in the checklist and the PR).

New context from the baseline 3.5.0 upgrade (PR #343) that applies here:
- Oslo changed its `dhis2` / `dhis2PlayServices` `GranularSyncModule.kt`
  (removed `WorkManagerController`), and git raises no conflict on flavor
  copies. Port the change to `app/src/simprints/` (task 2.2 / 3.1).
- ANDROAPP-7666 is fixed in Oslo 3.5.0. Baseline dropped its
  `// EyeSeeTea fix`.
- Oslo migrated `UserManagerImpl.java` to Kotlin, without `logIn()`.
- AGP 9.0.1 → 9.3.1. Android Studio must be Quail 2+ / Rabbit 1; older IDEs
  fail the Gradle sync.
- Oslo's new `.github/workflows/ci.yml` is disabled at repo level
  (`gh workflow disable ci.yml`). Our CI is `eyeseetea-main.yml` (`app-testing`).

## Progress

- baseline prepared: `yes`, `develop-eyeseetea@89b2f2e3b`
- merge started: `yes` (2026-10-22). `git merge develop-eyeseetea --no-commit`, 14 conflicted paths, uncommitted until conflicts are resolved (user decision)
- easy conflicts resolved: `yes`
- manual conflicts pending: `no`
- validation started: `yes` (2026-11-09); flows 1 to 14 verified, flow 15 blocked (Simprints app version), flow 9 step 5 blocked (duplicates in the Simprints project); see `upgrade-validation-checklist.md`
- merge committed: `yes`, `fadb67e69a`; PR #346 against `develop-simprints`

## Decisions

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| `app/src/simprints/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` | accept_ours + port Oslo flavor change | drop `WorkManagerController` import, parameter and argument; then it equals Oslo's `dhis2` flavor copy plus license header and formatting | n/a (flavor copy of `dhis2`) | resolved_manual_merge | not conflicted. Found by diffing `origin/upstream/3.4.2..3.5.0 -- app/src/dhis2 app/src/dhis2PlayServices`. Only flavor file with a Simprints copy that Oslo touched (`dhis2Training/DownloadNewVersion.kt` deletion has no relevance: Simprints keeps its own Play-Services-style copy) |
| `app/src/dhis2Training/java/org/dhis2/utils/granularsync/GranularSyncModule.kt` | accept_theirs (delete) | file removed | n/a | resolved_keep_theirs | `UD`: Oslo deleted it, fork only had a whitespace/indent change from an earlier merge |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt` | manual_reapply_on_theirs | biometric search, mode, age, DOB, TET toggle and duplicate-review hooks reapplied on Oslo's new shape | §2.3, §2.4, §2.5, §2.8, §2.9 | resolved_manual_merge | priority checks 4.1 / 4.2 (custom terminology, search error messages `cf72c54d5`). Resolved by the user: Oslo's `teTypeLabel`/`teTypePluralLabel` replace `teTypeName`, plus `teType`, `sequentialSearch`, the `onSearch()` block and the `AttributeSearch` conditions. Fixed a duplicated `-> {` |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTeiViewModelFactory.kt` | manual_reapply_on_theirs | extra constructor dependencies for the Simprints view model | §2.8 | resolved_manual_merge. Oslo only reindented `) :` and added `@Suppress`. Kept our 2 commented params; the diff equals the fork's previous drift |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/listView/SearchTEList.kt` | manual_reapply_on_theirs | TET toggle and duplicate-review UI hooks | §2.8, §2.9 | resolved_manual_merge. Resolved by the user: `teTypeName` → `teTypeLabel`, sequential-search blocks kept. Restored the `isLoaded == true` guard (fork commit `1013288a4`, "Avoid crashes") that had been dropped |
| `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataFragment.kt` | manual_reapply_on_theirs | biometrics UI surfaces / age threshold on the TEI data screen | §2.4, §2.10 | resolved_manual_merge | also carries the "avoid calls to database in recompositions" fork fix (§5). Imports: both of ours plus Oslo's `CustomLabelContext`. Args: Oslo's formatting plus `lastBiometricsSessionID` |
| `app/src/main/java/org/dhis2/usescases/teiDashboard/DashboardViewModel.kt` | manual_reapply_on_theirs | `_teiCanBeTransferred` cache, only if it still has a caller | §5 note (`7fe7f0f0d2`) | resolved_manual_merge | priority check 4.4: Oslo deleted `TeiDashboardMenu.kt`, the cache's only known caller. Priority check 4.4: the cache is still needed, because Oslo's `TEIDataFragment` still calls `checkIfTeiCanBeTransferred()` in composition. Oslo moved the menu into the view model (`moreOptionsMenu`, built in `io`). `checkIfTeiCanBeTransferred()` was restored to Oslo's repository call: using the cache there would race, since `dashboardModel` is emitted before the cache is filled and the cache is not part of the `combine`, so 'Transfer' could be missing. The cache is now consumed only by `TEIDataFragment` via `teiCanBeTransferredState`. Update the §5 note in 9.1 |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchRepositoryImplKt.kt` | manual_reapply_on_theirs | `searchTeiModel.addToAllAttributes(...)` block that keeps every attribute for search cards | §2.10 | resolved_manual_merge | **inventory gap**: has an `// EyeSeeTea customization` comment but isn't in `customization-files.md`. Oslo only reformatted the hunk with ktlint. `transformedValue` stays hoisted out of the `if`, in Oslo's brace format |
| `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataModule.kt` | manual_reapply_on_theirs | Dagger wiring: `BasicPreferenceProvider`, `ResourceManager`, `OrgUnitRepository` (`OrgUnitD2Repository`), `lastBiometricsSearchSessionId` into `TEIDataPresenter` | §2.7 / §2.10 / §2.13 / §2.14 (presenter wiring) | resolved_manual_merge | **inventory gap**: wiring file for `TEIDataPresenter` customizations isn't listed. The rest of the fork's diff is ktlint reformatting; take Oslo's format. Oslo's args plus our 4; `provideQuickActionMapper` is Oslo's (`customLabelProvider`). Cleaned inherited noise: 2 unused imports, 4 reformatted one-liners, 2 missing trailing commas. The diff is now only Simprints wiring (+19 lines), including the `basicPreferenceProvider` passed to `TeiDataRepositoryImpl` |
| `form/src/main/java/org/dhis2/form/ui/FormViewModel.kt` | manual_reapply_on_theirs | `onFieldsLoadingListener` / `onFieldsLoadedListener` hooks applied before `formSectionMapper.mapFromFieldUiModelList` | §2.10 | resolved_manual_merge | priority check 4.5. B6 `DROP_OLDEST` stays (Oslo's). Oslo only reformatted `items` (ktlint); hooks kept in Oslo's chained format. `977c1c439` (`isRepeatableIntent` adds `OnSection`) doesn't touch the biometric save intents |
| `app/src/test/java/org/dhis2/usescases/searchTrackEntity/SearchRepositoryTest.kt` | manual_reapply_on_theirs | `basicPreferenceProvider` mock passed to the repository | test wiring for §2.x | resolved_keep_ours | |
| `app/src/test/java/org/dhis2/usescases/searchTrackEntity/ui/mapper/TEICardMapperTest.kt` | manual_reapply_on_theirs | 3 confirmation-dialog-title tests | §2.9 | resolved_manual_merge | **inventory gap** (test file). Kept Oslo's `customLabelProvider` and follow-up stub, plus our biometrics-mode prefs mocks and 3 tests. Dropped the obsolete `marked_follow_up` resource stub. The diff is insertions only (+112) |
| `app/src/androidTest/java/org/dhis2/usescases/teidashboard/TeiDashboardMobileActivityTest.kt` | accept_theirs (reclassified) | none | n/a | resolved_keep_theirs | the fork diff was merge noise only: an unused `BasicPreferenceProvider` import and a dropped trailing comma (from `f81f1dc3f`, WIDP 3.3.0). Oslo replaced `featureConfigRepository` with `customLabelProvider` in the `DashboardRepositoryImpl` constructor |
| `gradle/libs.versions.toml` | defer_after_build_verification | Oslo versions plus `vName = "3.5.0-simprints-fork-1"`; `vCode` 159 | §4 shared drift | resolved_manual_merge | resolved by the user in the IDE: vCode 159, vName `3.5.0-simprints-fork-1`. Build check still pending (6.1). `gradle` 9.3.1, `kotlin` 2.4.10, `dhis2sdk` 1.15.0-eyeseetea-fork-1 come from baseline |
| `eyeseetea-docs/templates/openspec-config.yaml.template` | accept_ours | keep the 2 test-quality rules that baseline hasn't promoted yet (B7) | n/a (B7) | resolved_keep_ours | baseline added the other 2 rules in `3cd19be2e`; the conflict is only the 2 missing ones |
| Automerged §2 files: `EnrollmentPresenterImpl.kt`, `EnrollmentPresenterImplTest.kt`, `TEIDataPresenter.kt`, `TEICardMapper.kt`, `DashboardProgramModel.kt`, `TeiDashboardMobileActivity.kt`, `FormView.kt`, `LoginActivity.kt`, `ProgramViewModel.kt`, `SearchJavaToCompose.kt`, `SearchTEPresenter.java`, `SearchTEActivity.kt`, `tracker/.../SearchTrackedEntities.kt` | automerged, needs verification | customization lines present | §2.2–§2.5, §2.7, §2.9, §2.10, §2.13, §2.14 | needs_validation | changed by baseline, no conflict. Section 7 automerge verification plus priority checks 4.1 (labels), 4.3 (`LoginActivity`), 4.5 (`FormView`) |
| §3 files: `OpenIdSession.kt`, `SyncStatusDialog.kt`, `SyncPresenterImpl.kt` (deleted by Oslo) | accept_theirs | none | out of scope | resolved_keep_theirs | automerged. No fork drift against baseline and no `biometric`/`simprints` match in the fork's version |
| `app/build.gradle.kts` (§4) | defer_after_build_verification | `simprints` flavor block kept | §4 shared drift | needs_validation | automerged; verify in 3.4 / 6.1 |

Preclassification findings (2026-10-22):
- No `.claude/skills/openspec-*` `AA` conflicts this time: baseline didn't
  touch those files in this range. B3 is still pending but caused no conflict.
- No file deleted by Oslo in this range carried Simprints code. Every
  deleted file was grepped for `biometric|simprints|EyeSeeTea` at
  `09cad0d3e`, with zero matches.
- 2026-10-22: `DashboardViewModel.kt`, `SearchTEIViewModel.kt` and
  `SearchRepositoryImplKt.kt` (plus the OpenSpec template earlier) were found
  overwritten with the merge-base (stage 1) version and no markers,
  apparently from the IDE merge tool. Restored with `git checkout -m`. Don't
  open conflicted files in the IDE merge tool while resolving here.
- 3 inventory gaps for `customization-files.md` (fix in 9.1):
  `SearchRepositoryImplKt.kt`, `TEIDataModule.kt` and `TEICardMapperTest.kt`.
  `FormViewModel.kt` is only mentioned in prose in §2.10, without a path entry.
- §2.1, §2.6, §2.11 and §2.12 have no shared file changed by baseline in
  this range.

Silent breakages found by compiling (no git conflict), 2026-10-22:

| File | Oslo change | Fix |
|------|-------------|-----|
| `usescases/biometrics/duplicates/BiometricsDuplicatesDialogModule.kt` | `customLabelProvider` added to `SearchRepositoryImplKt` / `TEICardMapper` (`206a40711`, `105beb771`); `domainErrorMapper` added to `SearchTrackedEntityRepositoryImpl` (`cf72c54d5`) | pass them through, mirroring `SearchTEModule`; module-local `DomainErrorMapper` and `D2ErrorMessageProvider` providers, as Oslo does per module |
| `usescases/biometrics/duplicates/BiometricsDuplicatesDialogPresenter.kt` | search paging emits `TrackedEntitySearchPagingItem` (`cf72c54d5`) | `Item` → `SearchTeiModel`; `Error` logged and dropped (the dialog adapter has no error row) |
| `usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt` | `DashboardRepository.getDashboardModel()` is `suspend` (`105beb771`) | `fetchDashboardModel()` uses a coroutine (`io` for the query, `ui` for state). Dropped a dead duplicate call. Added the customization comment |
| `usescases/login/LoginRepository.kt` | raw `testing_credentials` / `training_credentials` deleted (`8f2855d70`) | file deleted. It was dead code that Oslo removed in `e72372338`, resurrected by an old fork merge (`750792ebf`) |

Design debt recorded: the duplicates dialog is a hand copy of the search wiring (see `refactors-pending.md`).

## Improvements to promote to `develop-eyeseetea`

Found during this upgrade. The generic CI and build fixes (B8, B10, B12, B15) were pushed
straight to `develop-eyeseetea` (user decision, no PR). What is still open to promote is
marked on each item.

- **B8. ktlint failure in the baseline itself.**
  `app/src/eyeseetea/java/org/dhis2/utils/granularsync/GranularSyncModule.kt:51`
  breaks `standard:function-signature`: `provideSyncUiStateMapper`'s body fits on one line.
  Fix: join the expression onto the signature line, matching Oslo's `dhis2` copy.
  Applied in the Simprints branch (`91c153f0b8`, so CI `lint-check` passes). **In `develop-eyeseetea`
  since `78800e1a9a`.**
- **B9. Flavor-copy drift rule for `conflict-rules.md`.** After merging Oslo
  (into baseline) or baseline (into a fork), run
  `git diff --name-status <old> <new> -- app/src/dhis2 app/src/dhis2PlayServices`
  and port each change into every flavor copy. Git never raises a conflict on
  flavor-only files. This hit `GranularSyncModule.kt` in both the baseline and
  Simprints in 3.5.0.
- **B10. (stale, corrected 2026-11-12) Oslo's `.github/workflows/ci.yml` is active in
  the repo**, not disabled: `gh workflow list` shows `CI active`, and it ran on PR #346.
  It needs BrowserStack, Sonar and Slack secrets the EyeSeeTea repo doesn't have, and
  it fails PRs over 400 changed lines unless the title has `[skip size]`. WIDP guarded
  the first two (`0aaa6feb41`, `1b1f551684`); the Slack job (`notification`) was
  guarded here. The three guards were pushed to `develop-eyeseetea`
  (`d0b1fc434c`, `3e30cc21d1`, `e0e9af16bf`), so the runbook should say: keep ci.yml
  active, with the guards and `[skip size]` in the title of upgrade PRs.
- **B11. Fork CI never ran ktlint** (resolved for Simprints, 2026-11-12). Oslo's
  `ci.yml` runs `ktlintCheck`, which found 1,767 violations in 136 Simprints files
  (baseline clean). `ktlintFormat` (`bcb11a1cc8`) plus 32 manual fixes (`7138cefc38`,
  `@Suppress` where the name is an external contract: enum entries read from JSON and
  saved preferences, constants, file names) made `ktlintCheck` pass.
- **B12. Sonatype answers HTTP 429 to CI runners.** `settings.gradle.kts` asks
  `central.sonatype.com/repository/maven-snapshots` before `google()` for every
  artifact. `app-testing` and `unit-tests` failed on it in every PR #346 run, and it
  also hit WIDP once and the baseline run after the CI-guards push. Two workflows run
  per push, so the requests double. Mitigation applied on the Simprints branch:
  `snapshotsOnly()` on that repository (marked `// EyeSeeTea fix`). **In `develop-eyeseetea` since `d56ca188d5`.**
- **B13. Customization tests must not assume the Simprints flavor.** Oslo's `ci.yml`
  runs `testDhis2DebugUnitTest`, and `app/src/test` runs once per flavor.
  `BIOMETRICS_ENABLED` is true only in the `simprints` flavor, so 4
  `EnrollmentPresenterImplTest` tests failed in `dhis2` (`6ec4527ca9` made them
  `assumeTrue`). I only ran `testSimprintsDebugUnitTest` locally, which hid it.
- **B14. Forks run two workflows with overlapping tests.** `ci.yml` (Oslo, shared):
  size, `ktlintCheck`, `testDebugUnitTest testDhis2DebugUnitTest testAndroidHostTest`,
  `assembleDhis2Debug`. `eyeseetea-main.yml` (baseline, one flavor line edited per
  fork): unit tests of the fork's own flavor only. The flavor line conflicts or
  differs in every fork and every upgrade (`testEyeseeteaDebugUnitTest` vs
  `testSimprintsDebugUnitTest` vs `testWidpDebugUnitTest`). Review at the end of the
  upgrade whether both are still needed and how the fork flavor should be chosen.
- **B15. Duplicate `PostMetadataSyncModule` in the `dhis2Training` flavor breaks
  `build-test-apks`.** The `dhis2Training` source set also compiles `src/dhis2/java`
  (`app/build.gradle.kts`), so `app/src/dhis2Training/java/org/dhis2/di/PostMetadataSyncModule.kt`
  (identical to the `dhis2` copy, added with the extension point in `4e5635da57`) is a
  `Conflicting declarations` error (`compileDhis2TrainingDebugKotlin`). It is in
  `develop-eyeseetea` too, so the baseline has it. WIDP removed it in `9a4f7aff83`.
  Removed here. **Removed in `develop-eyeseetea` too (`28b1dcc67a`).**
- **B16. Duplicated panel in the TEI dashboard after rotating to landscape.** Oslo
  regression from ANDROAPP-7664 (3.4.1): the navigation bar items are loaded in an `onStart`
  with `WhileSubscribed(5000L)`, so they stay the portrait ones after a rotation, and the
  fragment manager restores the `DETAILS` fragment next to the left column. Fixed here as
  an `// EyeSeeTea fix` in `DashboardViewModel.kt` and `TeiDashboardMobileActivity.kt`
  (48 added lines, none of Oslo's lines changed). It is a fix, not a customization, so it is
  not in `customization-files.md`. **Not taken to `develop-eyeseetea` until another user
  reports it**: on the next upgrade, check whether Oslo fixed it and drop it if so.
- **B7 (carried over).** 2 test-quality rules are still missing from the
  baseline `openspec-config.yaml.template`.

## CI review of the forks (task 9.7, 2026-11-13)

Problems only; no design agreed. Evidence: `.github/workflows/ci.yml`, `eyeseetea-main.yml`,
`git diff develop-eyeseetea -- .github`, and the runs of PR #346.

- **The two workflows overlap.** On a push to a PR branch both run: `ci.yml` through
  `pull_request`, `eyeseetea-main.yml` through `push` (any branch). Each sets up Gradle and
  resolves dependencies on its own, so the requests to Sonatype double (see B12).
- **Different flavors are tested.** `ci.yml` runs `testDebugUnitTest testDhis2DebugUnitTest
  testAndroidHostTest` and builds `dhis2` APKs only. `eyeseetea-main.yml` runs only the fork
  flavor (`testSimprintsDebugUnitTest`). No job builds or tests the fork flavor in the
  shared workflow, so tests written for the fork flavor fail in `dhis2` and the reverse
  (B13).
- **The fork flavor is a hand-edited line** in `eyeseetea-main.yml`, different in every
  fork and in every upgrade (B14).
- **`ci.yml` is Oslo's file and stays active in the forks**, with the secret guards added
  on top (4 jobs). Every Oslo change to it can conflict with those lines. The size check
  fails upgrade PRs over 400 changed lines unless the title has `[skip size]` (B10).
- **`ci.yml` has a daily `schedule` trigger** (`0 0 * * *`). It was not checked whether
  it runs in the fork on its default branch or what it costs.
- **The two workflows pin actions differently:** `ci.yml` pins commit SHAs, while
  `eyeseetea-main.yml` uses `@v3` / `@v4` tags and its own Gradle cache step next to the one
  in `setup-java`.
- **Failures that only CI showed in this upgrade:** ktlint (B11), Sonatype 429 (B12),
  `dhis2`-flavor tests (B13) and the duplicate module in `dhis2Training` (B15). None was
  visible with `testSimprintsDebugUnitTest` alone, which is what `run_tests.sh`-style local
  checks run.

Decisions: none taken. Still to decide in a separate task: whether both workflows are needed
and how the fork flavor should be chosen.

## Open Questions

- Does Oslo 3.5.0's custom terminology change any label that Simprints
  testers rely on in biometric flows? Not reported during the manual validation
  (2026-11-09 to 2026-11-13); not specifically checked label by label.

## Validation Notes

- build: compiles (`testSimprintsDebugUnitTest` / `testAndroidHostTest` ran to completion, 2026-11-11).
- targeted tests (8.1, 2026-11-11, without clearing kapt/KSP caches first):
  `./gradlew testSimprintsDebugUnitTest testAndroidHostTest --continue`.
  Aggregated: 1382 run, 1378 passed, **4 failed**, all in
  `TeiDataPresenterTest` (`:app`):
  - `should_not_return_biometrics_model_if_biometrics_mode_is_zero` and
    `..._limited_and_biometrics_is_empty`: `UninitializedPropertyAccessException`
    (`lateinit trackedEntityInstance`, test line 689).
  - `Should write the biometric template attribute when register completes with
    credential and templates` and `Should return null when apply effects of hide
    program stage with no events`: `UnfinishedStubbingException` at
    `givenADashboardModel` (test line ~1655). The second one is probably
    collateral of the first (a dangling stub on a shared mock).
  **Fixed 2026-11-12.** Cause: SDK 1.15.0's `TrackedEntityAttributeValue.builder().build()` requires
  `trackedEntityInstance`, and the helper `givenADashboardModel` did not set it (the two
  `UnfinishedStubbingException` were collateral of that exception thrown inside `whenever(...)`).
  Fix: `.trackedEntityInstance(teiUid)` in the helper. Not related to `fetchDashboardModel()`.
  Re-run `testSimprintsDebugUnitTest testAndroidHostTest --continue`: 1382 run, 1382 passed.
- manual flows checked: see `upgrade-validation-checklist.md` ("Status: 3.5.0 upgrade").

## Close-out checks (tasks 9.4 and 9.5, 2026-11-13)

- `check_upgrade_docs.py --client simprints` exits 1 with only expected noise: the list of files
  that still differ from `develop-eyeseetea` (the fork delta, plus `build/` output directories
  that the script also walks), and 2 code-comment titles with no matching spec heading:
  `Include SDK's modules` (a baseline marker in `settings.gradle.kts`) and
  `avoid calls to database in recompositions` (`DashboardViewModel.kt`, `TEIDataFragment.kt`;
  not biometrics, no spec; documented in §1.3 of `customization-files.md`).
  No spec/inventory title mismatch and no Feat-commit coverage issue was reported.
- `npx openspec validate --specs --strict`: 14 passed, 0 failed.

## Automerge verification (task 7.1, 2026-11-11)

Method: for each path listed in `customization-files.md` (and every non-flavor
file carrying an `// EyeSeeTea customization|fix` marker), compare the set of
unique lines matching `biometric|simprints|EyeSeeTea` in `HEAD`
(`09cad0d3e`, pre-merge fork) against the working tree. A line present in
`HEAD` and absent now counts as lost. Limit: it cannot see a dropped line that
has none of those words, so the diff-scan of the heavily merged files in the
Decisions table remains the reference for those.

- 82 existing inventory files: **0 lost lines**, except `gradle/libs.versions.toml`
  (expected: `dhis2sdk` `1.14.2` → `1.15.0-eyeseetea-fork-1`, `vName` `3.4.2` → `3.5.0`).
- 21 files carry a marker but have no path entry in the inventory. **0 lost lines**
  in 20 of them. They are the inventory gaps for 9.1:
  `AppComponent.java`, `BiometricsDuplicatesDialogAdapter.kt`,
  `BiometricsDuplicatesDialogModule.kt`, `BiometricsDuplicatesDialogView.kt`,
  `BiometricsSearchConfirmationDialog.kt`, `SearchRepository.java`,
  `SearchRepositoryImplKt.kt`, `SearchTeiModel.java`, `SearchTEKoinModule.kt`,
  `TeiDataRepository.kt`, `LastSelection.kt`, `SearchTEIViewModelTest.kt`,
  `TEICardMapperTest.kt`, `PreferenceModule.kt`, `ListCardUiModel.kt`,
  `PreferenceConstants.kt`, `FormRepositoryImpl.kt`, `FormSectionMapper.kt`,
  `DateProvider.kt`, `InputsForTextValueTypeProvider.kt`, plus `settings.gradle.kts`
  (named in the inventory without a path). Several are probably covered by name in
  prose; 9.1 must give each one a path entry.
- `FormViewModel.kt`: 1 lost line, the `// EyeSeeTea fix` for ANDROAPP-7666
  (completed-event "mark as complete?" dialog). **Expected, not a casualty**:
  Oslo fixed it in 3.5.0 and `develop-eyeseetea` dropped it too (tree and baseline
  both have 0 `EyeSeeTea fix` lines there). The remaining diff against baseline is
  the `onFieldsLoadingListener` / `onFieldsLoadedListener` hooks.
- Inventory entries that do not resolve to a file (fix in 9.1):
  `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt` (deleted by Oslo,
  0 Simprints matches in `HEAD`),
  `app/src/simprints/java/org/dhis2/data/user/GranularSyncModule.kt` (wrong path, already
  noted in the inventory), `customizations/eyeseetea/customizations-eyeseetea.md`
  (path prose, file not in the fork) and `tracker/src/commonMain/.../SearchTrackedEntities.kt`
  (ellipsis; the real file exists and has 0 lost lines).
- Oslo-deleted or renamed files (`git diff --cached -M HEAD`, status `D`/`R`): none
  had `biometric|simprints|EyeSeeTea` in their `HEAD` version.

## Finalization

- surviving customizations moved to `customization-files.md`: `yes` (9.1: 21 marker files and `FormViewModel.kt` listed, stale entries fixed, §4 emptied into §1.3)
- stable rules moved to `conflict-rules.md`: `no`. Proposed, still to add: B9 (flavor-copy drift check after a merge) and B13 (customization tests must not assume the fork flavor)
- temporary notes ready to archive/remove: `yes`
- unexplained shared drift remaining: `none` (§4 of `customization-files.md` is empty)

Carried forward: `connectedSimprintsDebugAndroidTest` not run (task 8.3), landscape fix B16 kept in the fork until another user reports it, task 9.7 (CI review) below, flows 15 and 9 step 5 blocked by Simprints.
