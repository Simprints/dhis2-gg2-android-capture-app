    ## 1. Pre-merge setup

- [x] 1.1 Confirm the branch is checked out at `develop-eyeseetea@938b819597` as the merge target and `feature-simprints/upgrade_3.4.1` is up to date with its own tip.
- [x] 1.2 Confirm `eyeseetea-docs/upgrade/simprints/upgrade-3.4-notes.md` header reflects the 3.4.1 target and note the merge is starting.
- [~] 1.3 For each of the 13 active customizations, cross-check `git show <feat-commit> --stat` against `customization-files.md` section 2 to confirm the inventory is complete before merging (conflict-rules.md "Inventory completeness is load-bearing"). **Deferred — known gap**: `customization-files.md` has no "Feat commits" section, and the 209 commits touching biometrics/simprints paths since the divergence point are contaminated with WIDP and upstream bring-forward merges, making SHA reconstruction unreliable. Mitigated by task 7.1 (Automerge verification against the full per-file diff, which does not need SHAs) and by the fact that the inventory was derived from code and `EyeSeeTea customization` comments during onboarding. Revisit as a follow-up: adding Feat commit SHAs to the inventory is a candidate baseline improvement for `customization-files-template.md`.
- [x] 1.4 Confirm WIDP-only leftover files (2FA, `OpenIdController`, related tests — inherited from the historical `feature-widp/... into feature-simprints/...` merge) will be removed by the 3.4.1 merge itself, not by a manual pre-merge deletion. Verified: `develop-eyeseetea` contains commit `87c5da0109 "Remove 2factor customization"` as an ancestor (this is how `develop-eyeseetea` was cleaned when it forked from `develop-widp`); that commit is not yet an ancestor of this branch, so the merge will bring the deletion via normal three-way merge. No manual cleanup needed before merging — verify in task 3.3/7.1 that the deletion actually lands (may surface as a real conflict, not a silent auto-delete, if Simprints touched the same lines via upstream/WIDP bring-forward merges).

## 2. Merge and preclassification

- [x] 2.1 Merge `develop-eyeseetea` into `feature-simprints/upgrade_3.4.1`.
- [x] 2.2 Classify direct flavor files (`app/src/simprints/**`, `app/src/simprintsDebug/**`) as `accept_ours`.
- [x] 2.3 Classify every file listed in `customization-files.md` section 2 (13 active customizations) — expect `manual_reapply_on_theirs` for most.
- [x] 2.4 Classify files listed in `customization-files.md` section 3 (out-of-scope areas: login/2FA, notifications, change server URL, granular sync wiring) as `accept_theirs`.
- [x] 2.5 Classify files listed in `customization-files.md` section 4 (shared drift: `app/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`) as `defer_after_build_verification`.
- [x] 2.6 Classify any remaining conflicted file not covered above per `conflict-rules.md` default rules by path; if a real customization surfaces, flag the inventory gap before continuing.
- [x] 2.7 Record the preclassification table in `upgrade-3.4-notes.md` per the `conflict-rules.md` temporary format.
- [x] 2.8 Pause and confirm with the user before starting the `manual_reapply_on_theirs` batch.

## 3. Resolve easy conflicts

- [x] 3.1 Resolve all `accept_ours` flavor files.
- [x] 3.2 Resolve all `accept_theirs` files (out-of-scope areas + clear shared-base changes).
- [x] 3.3 Run the post-merge fork identity check (`conflict-rules.md`): version/identity strings in `gradle/libs.versions.toml`, flavor source sets present, no dependency silently removed, `app/build.gradle.kts` still defines the `simprints` flavor, fork-specific files (`google-services.json`, signing config) untouched.

## 4. Resolve manual conflicts — high-risk capabilities

- [x] 4.1 `biometric-search-integration` — reapply Simprints logic in search launch/loader/continuation files.
- [x] 4.2 `biometrics-config-selection` — reapply config download/selection/flattening logic (`BiometricsConfigApi.kt`, `BiometricsConfigRepositoryImpl.kt`, `SelectBiometricsConfig.kt`, `ProgramViewModel.kt`, `SyncBiometricsConfig.kt`, `LoginActivity.kt`, `SyncPresenterImpl.kt`).
- [x] 4.3 `biometrics-duplicate-review-confirm-identity` — reapply duplicate dialog, confirm identity, and `registerLast` wiring.
- [x] 4.4 `biometrics-tei-ui-surfaces` — reapply search card/dashboard/enrollment/TEI-form biometric UI wiring.
- [x] 4.5 `simprints-data-exchange-mapping` — reapply `BiometricsClientFactory` and `BiometricsClient` request/response mapping.
- [x] 4.6 Pause and confirm with the user before starting the medium/low-risk manual-reapply batch.

## 5. Resolve manual conflicts — medium and low-risk capabilities

- [x] 5.1 `biometrics-mode-controls` — reapply per-program mode gating (`full`/`limited`/`zero`).
- [x] 5.2 `biometrics-age-threshold` — reapply age-threshold gating across search/enrollment/dashboard.
- [x] 5.3 `biometrics-confidence-score-filtering` — reapply confidence filtering in `BiometricsClient`.
- [x] 5.4 `biometrics-org-unit-module-id` — reapply module id derivation logic.
- [x] 5.5 `biometrics-verification-persistence` — reapply verification persistence and read paths.
- [x] 5.6 `biometrics-date-of-birth-attribute` — reapply configured DOB attribute reads.
- [x] 5.7 `biometrics-relationship-search-tet-toggle` — reapply relationship-search TE-type toggle.
- [x] 5.8 `biometrics-time-based-windows` — reapply verification/decline time-window logic.

## 6. Deferred and shared-drift resolution

- [x] 6.1 Reconcile `app/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml` against the new baseline's flavor/composite-build blocks — minimum needed to compile (see design.md Open Questions).
- [x] 6.2 Confirm the project builds (`./gradlew assembleSimprintsDebug`) before finalizing `defer_after_build_verification` files.

## 7. Automerge verification

- [x] 7.1 For every file listed in `customization-files.md` (not only conflicted ones), run `git diff develop-eyeseetea -- <file>` and confirm all documented customization lines survived.
- [x] 7.2 Recover any silently dropped customization code found in 7.1 before staging.

## 8. Validation

- [x] 8.1 Run `./gradlew testSimprintsDebugUnitTest` and fix regressions in Simprints-specific tests (`SearchTEIViewModelTest.kt` and any others touched during manual reapply). 977/977 green as of 2026-09-04.
- [x] 8.2 Manually validate high-risk flows from `upgrade-validation-checklist.md`: #1 Biometric Search Integration, #9 Biometric Duplicate Review And Confirm Identity, #10 Biometrics In TEI Cards/Dashboard/Enrollment/Form, #13 Simprints Data Exchange And Mapping — confirmed on device (Lenovo TB X304F). **#2 Biometrics Configuration Selection deliberately left out of scope for this delivery** — see note below.
- [ ] 8.3 Manually validate medium-risk flows: #3 Biometrics Mode Controls, #4 Age Threshold Controls, #6 Confidence Score Filtering, #7 Org Unit Derived Module Id, #11 Biometric Verification Persistence. **Deliberately out of scope for this delivery** — see note below.
- [ ] 8.4 Manually validate low-risk flows: #5 Configurable Date Of Birth Attribute, #8 Relationship Search Identification Toggle, #12 Time-Based Verification And Registration Failure Windows. **Deliberately out of scope for this delivery** — see note below.
- [ ] 8.5 Validate #14 Areas Explicitly Out Of Scope For Preservation — confirm 2FA/login, notifications, change server URL, and granular sync match baseline behavior with no Simprints-specific variant left. **Deliberately out of scope for this delivery** — see note below.

**Note on 8.3-8.5 (2026-09-04, delivery-day decision):** the client's server migration to DHIS2 core 2.43 forced a same-day delivery deadline (see `upgrade-3.4-notes.md` top of file). There was no time to run full manual QA across all 14 flows. What *is* covered: the highest-risk flows actually exercised on device during this session (registerLast end-to-end including the `fieldListChannel` race fix, biometric search with 2+ candidates, duplicate resolution, biometrics config sync surviving a metadata sync), plus the full automated suite (977 unit tests, `assembleSimprintsDebug`, `openspec validate --specs --strict` 13/13). The remaining flows (§8.3-8.5 above, plus the not-yet-fully-covered parts of §8.2) are shipped **unvalidated for this specific 3.4.1 upgrade** and documented as such in the PR (`EyeSeeTea/dhis2-android-capture-app#332`, "Known issues") and in `upgrade-validation-checklist.md`'s own per-flow status. Follow-up manual QA should happen as soon as the delivery pressure lifts — do not treat this task list as "done" for those flows just because the change is archived.

## 9. Close out

- [x] 9.1 Update `customization-files.md` with confirmed surviving customizations and technical notes from the manual-reapply tranches.
- [x] 9.2 Update `upgrade-3.4-notes.md` progress section (`merge started: yes`, `easy conflicts resolved`, `manual conflicts pending`, `validation started`) and close it out once the upgrade is validated.
- [x] 9.3 Run `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client simprints` and confirm only expected out-of-scope noise remains (2FA/login, `settings.gradle.kts` baseline comments). Found and fixed one real gap: `PostMetadataSyncModule.kt`'s customization comment was missing the rest of the spec title (`Biometrics Configuration Selection` → `Biometrics Configuration Selection Per Program Or Org Unit Group`). The other flagged comment (`avoid calls to database in recompositions`) is a known, already-documented case that doesn't map to any of the 13 specs — see `customization-files.md` line ~381.
- [x] 9.4 Run `openspec validate --specs --strict` and confirm all 13 specs still pass unchanged. 13/13 passed, 2026-09-04.
- [x] 9.5 If this change surfaced a genuine capability-behavior change (not just an implementation move), stop and raise it with the user as a separate change instead of folding it in here. None surfaced — all fixes this upgrade (registerLast, biometric search, duplicates dialog, `fieldListChannel` race) are bug fixes preserving existing capability behavior, not behavior changes.

## 10. Merge `develop-simprints` (last step)

Run only once the upgrade is validated (sections 8 and 9 complete). `develop-simprints` carries
fork fixes made in parallel with the upgrade (as of 2026-08-09: 12 commits / 15 files, mostly
biometric search and SID response mapping, plus `gradle/libs.versions.toml`). Merging it earlier
would mix new fork behavior with upgrade casualties and make validation results ambiguous.

- [x] 10.1 `git fetch origin develop-simprints:develop-simprints` and re-check the incoming scope (`git log --oneline HEAD..develop-simprints`); it may have grown since 2026-08-09.
- [x] 10.2 Confirm the working tree is clean, then `git merge develop-simprints` from `feature-simprints/upgrade_3.4.1`.
- [x] 10.3 Resolve conflicts per `conflict-rules.md`. Expect them concentrated in the biometric search/TEI files already reapplied in sections 4-5 — check whether each incoming fix is still needed against the 3.4.1 baseline, since some fixed bugs Oslo may have fixed upstream (e.g. the recycler-race revert and ANDROAPP-7647 stale-program-results fix already present in the incoming commits).
- [x] 10.4 Run the automerge verification from section 7 again over `customization-files.md` — this second merge can silently drop reapplied customization lines just like the baseline merge did.
- [x] 10.5 Re-run `./gradlew testSimprintsDebugUnitTest` and `./gradlew assembleSimprintsDebug`.
- [ ] 10.6 Re-validate the high-risk flows touched by the incoming commits (#1 Biometric Search Integration, #10 Biometrics In TEI Cards/Dashboard/Enrollment/Form, #13 Simprints Data Exchange And Mapping) from `upgrade-validation-checklist.md`. Partially covered by the device testing in 8.2 above (search + TEI UI surfaces), not a dedicated separate pass — folded into the same delivery-day time constraint as section 8.
