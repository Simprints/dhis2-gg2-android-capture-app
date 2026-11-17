## 1. Pre-merge setup

Commit: none on its own. Per user decision (2026-10-22), nothing is committed until conflicts are resolved: these files go into the merge commit (6.3).

- [x] 1.1 Confirm `feature-simprints/upgrade_3.5.0` is checked out at `develop-simprints@09cad0d3e` with a clean tree, and that `develop-eyeseetea` is at `89b2f2e3b` (Oslo 3.5.0 plus PR #343). Verify with `git status` and `git log -1` on both. **Done** (2026-10-22): tree clean at `09cad0d3e`, `develop-eyeseetea` at `89b2f2e3b`.
- [x] 1.2 Create `eyeseetea-docs/upgrade/simprints/upgrade-3.5.0-notes.md` from `eyeseetea-docs/upgrade/template/upgrade-notes-template.md`. Fill in the header (base `f87bec8c3`, target `89b2f2e3b`) and an "Inherited context" table that re-checks B2, B3, B6 and B7 against `develop-eyeseetea@89b2f2e3b`. B7 is expected to be promoted via `3cd19be2e`. Verify the file exists and every B-item has a "Verified now" value. **Done** (2026-10-22): B2/B3/B6 still pending, B7 partially promoted; `blockedSearchOperators` watch item changed status (now read by baseline).
- [x] 1.3 Confirm `origin/develop-simprints` has no commits ahead of `09cad0d3e`. If it has, note their scope in the notes and plan to merge them only after this upgrade is validated. Verify with `git log 09cad0d3e..origin/develop-simprints`. **Done** (2026-10-22): no commits ahead.

## 2. Merge and preclassification

Commit: none. The merge stays uncommitted until section 6 closes, as in 3.4.2.

- [x] 2.1 Merge `develop-eyeseetea` into `feature-simprints/upgrade_3.5.0` (never `upstream/3.5.0`). Verify with `git status`, which shows `MERGE_HEAD` and the conflicted file list. **Done** (2026-10-22): 14 conflicted paths, `--no-commit`.
- [x] 2.2 Classify direct flavor files (`app/src/simprints/**`, `app/src/simprintsDebug/**`) as `accept_ours`. Diff `origin/upstream/3.4.2..origin/upstream/3.5.0 -- app/src/dhis2 app/src/dhis2PlayServices` and list each changed file that has a Simprints copy (expected: `GranularSyncModule.kt`). Verify the list is recorded in the notes. **Done**: only `GranularSyncModule.kt` (drop `WorkManagerController`). `dhis2Training` copy deleted by Oslo (`UD`), accept_theirs.
- [x] 2.3 Classify every file in `customization-files.md` §2 (14 customizations), conflicted or not. Verify each has a row in the notes' Decisions table with classification and expected delta. **Done**: 9 conflicted + 13 automerged §2 files recorded. 3 inventory gaps found.
- [x] 2.4 Classify §3 files (out-of-scope areas, including `SyncPresenterImpl.kt` and `OpenIdSession.kt`) as `accept_theirs`, after grepping each for `biometric`/`simprints`. Verify the grep results are noted. **Done**: no fork drift and no matches. `SyncPresenterImpl.kt` deleted by Oslo with no Simprints code.
- [x] 2.5 Classify §4 shared-drift files (`app/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`) as `defer_after_build_verification`. Verify they are recorded in the notes. **Done**: `libs.versions.toml` conflicted, `app/build.gradle.kts` automerged, `settings.gradle.kts` untouched.
- [x] 2.6 Classify the remaining conflicted files with the `conflict-rules.md` default rules by path. This covers `.claude/skills/openspec-*` / `.claude/commands/opsx/*` `AA` conflicts as `accept_ours` and new baseline `.claude/` skills as `accept_theirs`. Flag any inventory gap. Verify every conflicted path has a classification. **Done**: no `.claude/` `AA` conflicts. Template conflict is `accept_ours` (B7 rules). Test files are `manual_reapply_on_theirs`.
- [x] 2.7 Pause and review the preclassification with the user before resolving anything. Verify with explicit user OK.

## 3. Resolve easy conflicts

Commit: none (merge in progress).

- [x] 3.1 Resolve all `accept_ours` flavor files and port the Oslo flavor changes found in 2.2 to the Simprints copies (`WorkManagerController` removal in `GranularSyncModule.kt`). Verify the Simprints copy matches the `dhis2PlayServices` version apart from documented customizations. **Done** (2026-10-22): 3 lines removed (Oslo `238a1da2f`, ANDROAPP-7605). Diff against Oslo's `dhis2` copy is only the license header and formatting. No `accept_ours` flavor file was conflicted.
- [x] 3.2 Resolve all `accept_theirs` files. Verify no conflict markers remain in them. **Done**: `dhis2Training/GranularSyncModule.kt` deleted; `TeiDashboardMobileActivityTest.kt` reclassified to `accept_theirs`.
- [x] 3.3 Resolve the `.claude/` scaffolding conflicts per 2.6. Verify the fork's `generatedBy` stays `1.8.0`. **Done**: no `.claude/` conflicts this round; fork stays on `generatedBy: 1.8.0`. OpenSpec template kept ours (B7 rules).
- [x] 3.4 Run the post-merge fork identity check from `conflict-rules.md`. Verify: **Done** (2026-10-22): `vName = 3.5.0-simprints-fork-1`, `vCode = 159`; `simprints` flavor (`org.simprints.dhis2`) present; SDK `1.15.0-eyeseetea-fork-1`; `@AGENTS-simprints.md` import intact; `libsimprints 2024.2.2` dependency kept; no dependency removed from `app/build.gradle.kts`.
  - `vName = "3.5.0-simprints-fork-1"`;
  - the `simprints` flavor is present in `app/build.gradle.kts`;
  - SDK `1.15.0-eyeseetea-fork-1`;
  - the `CLAUDE.md` import of `@AGENTS-simprints.md` is intact;
  - no dependency was silently removed.
- [x] 3.5 Pause and review the easy batch with the user before the manual batch. Verify with explicit user OK. **Done**: reviewed with the user conflict by conflict.

## 4. Priority checks for known 3.5.0 baseline changes

Commit: none (merge in progress). Record the expected delta in the notes before editing each file.

- [x] 4.1 Custom terminology series (`206a40711`, `09a0a8d56`, `1cadd3746`, `4a3742685`, `998ff6aec`, `641c6a9e8`, `105beb771`) against §2.1, §2.8, §2.9 and §2.10. Reapply the biometric hooks on Oslo's new method shapes in the search, card-mapper and dashboard files. Verify the resulting diff against `develop-eyeseetea` matches the expected delta. **Done** (2026-10-22): the conflicted files were reapplied on Oslo's label shapes. The compile then found 8 silent breakages in Simprints-only code, all fixed:
  - `BiometricsDuplicatesDialogModule`: `customLabelProvider`, `domainErrorMapper`, plus `D2ErrorMessageProvider` / `DomainErrorMapper` providers.
  - `BiometricsDuplicatesDialogPresenter`: `TrackedEntitySearchPagingItem` errors filtered out.
  - `TEIDataPresenter.fetchDashboardModel`: now a coroutine, because `getDashboardModel` is `suspend`.
  - Dead `usescases/login/LoginRepository.kt` deleted (Oslo removed it in `e72372338`; its raw credentials went in `8f2855d70`).

  `:app:compileSimprintsDebugKotlin` passes. Duplicates-dialog design debt recorded in `refactors-pending.md`.
- [x] 4.2 `cf72c54d5` (search error messages) against biometric search result handling (§2.1). Verify that biometric "no match" and "error" states still reach the UI. The manual check is in section 8. **Done** (2026-10-22): Oslo maps `TrackedEntitySearchPagingItem.Error` to `SearchTeiErrorModel` inside `SearchTEIViewModel`. Our biometric `queryData` path and `verifyAutoNavigateToTEI` (adapter snapshot of `SearchTeiModel`) are unaffected. The duplicates dialog presenter needs the same mapping (compile error, see 4.1).
- [x] 4.3 Login / OAuth / offline-login / session-renewal series against §2.2. Verify `SyncBiometricsConfig` is still invoked from the new `LoginActivity` / `LoginModule` flow (grep, plus the manual check in section 8). **Done**: `LoginActivity.onNavigateToHome` still calls `syncBiometricsConfig`. Dagger `AppComponent.plus(LoginModule)` and `provideSyncBiometricsConfig` are intact. Oslo's renew-session / OAuth changes don't touch the callbacks.
- [x] 4.4 `TeiDashboardMenu.kt` deletion. Find where Oslo moved the transfer menu item and whether the `DashboardViewModel` transfer-check cache (§5 note, `7fe7f0f0d2`) still has a caller. Either keep it wired to the new caller or remove it as absorbed. Verify the decision is recorded in the notes and `customization-files.md` §5. **Done**: cache still needed by `TEIDataFragment`. `checkIfTeiCanBeTransferred()` restored to Oslo to avoid a race with `moreOptionsMenu`.
- [x] 4.5 Form changes (`977c1c439` section reload, `8d2c133f6` picture processing) against the biometric form field (§2.10). Verify that `Form.kt`, `FormView.kt` and `FormViewModel.kt` diffs match the expected delta. **Done**: Oslo only reformatted `items`. Hooks kept. `977c1c439` doesn't affect biometric save intents. `FormView.kt` automerge drift is identical (+64/-2).

## 5. Resolve manual conflicts: remaining active capabilities

Commit: none (merge in progress).

- [x] 5.1 Resolve the remaining `manual_reapply_on_theirs` files from 2.3, starting from `develop-eyeseetea` and reapplying only the minimum Simprints delta, with `// EyeSeeTea customization - [spec title]` comments. Verify there are no conflict markers and the diff matches the expected delta per file. **Done** (2026-10-22): all 11 manual conflicts resolved one by one with the user (see the notes' Decisions table).
- [x] 5.2 Pause and confirm with the user for any file whose diff is significantly larger than expected. Verify with an explicit user OK, or "none". **Done**: the only extra-large diffs were inherited noise, cleaned up in `TEIDataModule.kt`, plus the duplicates-dialog API migrations, recorded as debt.

## 6. Build, deferred files and merge commit

Commit: 6.3 is the merge commit. It contains everything from sections 2–6.

- [x] 6.1 Reconcile the §4 shared-drift build files, doing only what is needed to compile. Migrate Simprints code that breaks on SDK 1.15.0 / Oslo API changes mechanically. Stop and ask the user if a biometrics-needed API disappeared. Verify `./gradlew assembleSimprintsDebug` succeeds. **Done** (2026-10-22): `assembleSimprintsDebug` OK → `dhis2-v3.5.0-simprints-fork-1-feature-simprints-upgrade_3.5.0.apk`.
- [~] 6.2 Run `./gradlew ktlintCheck` (`ktlintFormat` first if needed). Verify it passes. **Not passing, accepted as pre-existing debt** (user decision 2026-10-22): 1,775 violations in 137 files, none introduced by this merge. Merge-touched lines were verified clean, and 2 `SearchTEIViewModel` conditions were fixed. Restored the baseline `app/src/dhis2/.../GranularSyncModule.kt`, whose only drift was old merge formatting. Debt recorded in `refactors-pending.md`; a separate `style:` commit or PR follows.
- [x] 6.3 Commit the merge after the personal reviews (`clean-architecture-code-smells`, `pillar-based-testing`). Verify `git log -1` shows a merge commit with two parents.

## 7. Automerge verification

Commit: one commit, "fix(simprints): recover customizations dropped by 3.5.0 automerge", only if 7.2 recovers code.

- [x] 7.1 For every file in `customization-files.md`, run `git diff develop-eyeseetea..HEAD -- <file>` (two-dot) and confirm that every documented customization line survived. Also grep each Oslo-deleted or renamed file for `biometric`/`simprints`. Verify there is a per-section result in the notes.
- [x] 7.2 Recover any silently dropped customization code found in 7.1. Verify the recovered behavior with a test or a manual-checklist entry.

## 8. Validation

Commit: one commit for checklist/notes updates.

- [x] 8.1 Clear the kapt/KSP caches, then run `./gradlew testSimprintsDebugUnitTest testAndroidHostTest` and fix regressions in Simprints-specific tests. Verify all tests pass and record the counts in the notes. **Done** (2026-11-12): 1382/1382 pass. 4 failures in `TeiDataPresenterTest` were SDK 1.15.0 requiring `trackedEntityInstance` in the test helper; fixed.
- [x] 8.2 Manually validate the flows in `upgrade-validation-checklist.md` on an emulator. Prioritize:
  - the high-risk capabilities (#1, #2, #9, #10, #13);
  - priority checks 4.2 and 4.3;
  - login plus metadata sync, which is also the pending Phase 1 check for the eyeseetea baseline.

  Verify each flow has a status in the checklist.
- [x] 8.3 Retry `connectedSimprintsDebugAndroidTest` once. If it is still blocked by the Room identity-hash mismatch, document it as a carried-forward known issue in the checklist and the PR. Verify the status is recorded. **Skipped by user decision (2026-11-13): not run, carried forward as a known issue in the checklist, the notes and the PR.**
- [x] 8.4 Fix the duplicated panel in the TEI dashboard in landscape, found on the Lenovo tablet during the 8.2 smoke test (2026-11-10). Open the dashboard in portrait, rotate to landscape: the left and right columns both render the TEI data, and the navigation bar keeps the "Details" tab. Opening the dashboard directly in landscape renders correctly (left: TEI data and events; right: Analytics; bar without "Details").
  - **Cause found:** `DashboardViewModel.loadNavigationBarItems()` adds `DETAILS` only when `isPortrait()` is true at load time. The ViewModel survives the rotation (the activity declares no `configChanges`), so the items are not recomputed and `DETAILS` stays selected, and its fragment renders next to the left column (`tei_main_view`).
  - **Origin:** not a Simprints change. `DashboardViewModel.loadNavigationBarItems()` and `TeiDashboardMobileActivity` match `origin/upstream/3.5.0`, and `develop-simprints@09cad0d3e` had the same `isPortrait()` gating. No earlier fork fix exists: PR #222 (`fix_flickering_issues_on_rotate`) targeted the ViewPager code that Oslo removed in ANDROAPP-6386. The behavior is expected on `develop-eyeseetea` too, but that was not run.
  - **Decision (2026-11-13, user):** the fix is applied here as an `// EyeSeeTea fix` (an Oslo regression fix, **not** a customization: not in `customization-files.md`) and is **not** taken to `develop-eyeseetea` until another user reports it. Origin corrected: the stale items come from ANDROAPP-7664 (3.4.1), which moved the load to an `onStart` with `WhileSubscribed(5000L)`; the `isPortrait()` gating dates from ANDROAPP-6386 (3.1.0). Not run on a 3.4.x build. Implemented in `DashboardViewModel.reloadNavigationBarItems()` (drops `DETAILS` in landscape right away, reloads the items, and selects `DETAILS` when coming back to portrait) and `TeiDashboardMobileActivity.removeRestoredDetailsFragmentInLandscape()` (the fragment manager restores the `DETAILS` fragment, which rendered the TEI data twice).
  - Verify on device: rotate portrait → landscape and landscape → portrait on the dashboard of a TEI with and without biometrics. No duplicated panel, and the bar shows the right tabs for each orientation. Then add a rotation step to flow #10 of `upgrade-validation-checklist.md`.

## 9. Close out

Commit: 9.1–9.5 in one commit, "docs(simprints): close out 3.5.0 upgrade". 9.6 is its own commit, "chore(openspec): archive upgrade-to-3-5-0 change".

- [x] 9.1 Update `customization-files.md` with the surviving customizations, the new 3.5.0 file locations and any absorbed items. Verify that every §2 entry points at existing files.
- [x] 9.2 Update `AGENTS-simprints.md`. Verify:
  - current version is `3.5.0-simprints-fork-1`;
  - the "Upgrade context" section is refreshed (it still says 3.3.1 → 3.4.1);
  - the key-docs list points at the current notes.
- [x] 9.3 Close out `upgrade-3.5.0-notes.md`: progress, finalization section and any new reusable rule proposed for `conflict-rules.md`, such as the flavor-diff check. Verify `Status: closed`.
- [x] 9.4 Run `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client simprints`. Verify only the expected noise remains.
- [x] 9.5 Run `npx openspec validate --specs --strict`. Verify 14/14 pass.
- [x] 9.6 Archive this change (`openspec/changes/upgrade-to-3-5-0/` → `openspec/changes/archive/<date>-upgrade-to-3-5-0/`). Verify `npx openspec list` no longer shows it as active.
- [x] 9.7 Review the CI setup of the forks (problems only, no design agreed; see `upgrade-3.5.0-notes.md` B10-B14): the two workflows (`ci.yml`, `eyeseetea-main.yml`) and their overlap, the Sonatype 429 fix that still has to go to `develop-eyeseetea`, B8 still missing in the baseline, the per-fork flavor line, and the rule that customization tests must not assume the Simprints flavor (candidate for `AGENTS-simprints.md`). Verify the decisions are recorded in the notes.

## 10. Pull request

- [ ] 10.1 Push `feature-simprints/upgrade_3.5.0` and open a PR against `develop-simprints`, following the style of PR #341, with known issues listed. Verify the PR URL and that the `app-testing` check is green.
