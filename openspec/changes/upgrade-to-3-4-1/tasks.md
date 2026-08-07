    ## 1. Pre-merge setup

- [x] 1.1 Confirm the branch is checked out at `develop-eyeseetea@938b819597` as the merge target and `feature-simprints/upgrade_3.4.1` is up to date with its own tip.
- [x] 1.2 Confirm `eyeseetea-docs/upgrade/simprints/upgrade-3.4-notes.md` header reflects the 3.4.1 target and note the merge is starting.
- [~] 1.3 For each of the 13 active customizations, cross-check `git show <feat-commit> --stat` against `customization-files.md` section 2 to confirm the inventory is complete before merging (conflict-rules.md "Inventory completeness is load-bearing"). **Deferred — known gap**: `customization-files.md` has no "Feat commits" section, and the 209 commits touching biometrics/simprints paths since the divergence point are contaminated with WIDP and upstream bring-forward merges, making SHA reconstruction unreliable. Mitigated by task 7.1 (Automerge verification against the full per-file diff, which does not need SHAs) and by the fact that the inventory was derived from code and `EyeSeeTea customization` comments during onboarding. Revisit as a follow-up: adding Feat commit SHAs to the inventory is a candidate baseline improvement for `customization-files-template.md`.
- [x] 1.4 Confirm WIDP-only leftover files (2FA, `OpenIdController`, related tests — inherited from the historical `feature-widp/... into feature-simprints/...` merge) will be removed by the 3.4.1 merge itself, not by a manual pre-merge deletion. Verified: `develop-eyeseetea` contains commit `87c5da0109 "Remove 2factor customization"` as an ancestor (this is how `develop-eyeseetea` was cleaned when it forked from `develop-widp`); that commit is not yet an ancestor of this branch, so the merge will bring the deletion via normal three-way merge. No manual cleanup needed before merging — verify in task 3.3/7.1 that the deletion actually lands (may surface as a real conflict, not a silent auto-delete, if Simprints touched the same lines via upstream/WIDP bring-forward merges).

## 2. Merge and preclassification

- [ ] 2.1 Merge `develop-eyeseetea` into `feature-simprints/upgrade_3.4.1`.
- [ ] 2.2 Classify direct flavor files (`app/src/simprints/**`, `app/src/simprintsDebug/**`) as `accept_ours`.
- [ ] 2.3 Classify every file listed in `customization-files.md` section 2 (13 active customizations) — expect `manual_reapply_on_theirs` for most.
- [ ] 2.4 Classify files listed in `customization-files.md` section 3 (out-of-scope areas: login/2FA, notifications, change server URL, granular sync wiring) as `accept_theirs`.
- [ ] 2.5 Classify files listed in `customization-files.md` section 4 (shared drift: `app/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`) as `defer_after_build_verification`.
- [ ] 2.6 Classify any remaining conflicted file not covered above per `conflict-rules.md` default rules by path; if a real customization surfaces, flag the inventory gap before continuing.
- [ ] 2.7 Record the preclassification table in `upgrade-3.4-notes.md` per the `conflict-rules.md` temporary format.
- [ ] 2.8 Pause and confirm with the user before starting the `manual_reapply_on_theirs` batch.

## 3. Resolve easy conflicts

- [ ] 3.1 Resolve all `accept_ours` flavor files.
- [ ] 3.2 Resolve all `accept_theirs` files (out-of-scope areas + clear shared-base changes).
- [ ] 3.3 Run the post-merge fork identity check (`conflict-rules.md`): version/identity strings in `gradle/libs.versions.toml`, flavor source sets present, no dependency silently removed, `app/build.gradle.kts` still defines the `simprints` flavor, fork-specific files (`google-services.json`, signing config) untouched.

## 4. Resolve manual conflicts — high-risk capabilities

- [ ] 4.1 `biometric-search-integration` — reapply Simprints logic in search launch/loader/continuation files.
- [ ] 4.2 `biometrics-config-selection` — reapply config download/selection/flattening logic (`BiometricsConfigApi.kt`, `BiometricsConfigRepositoryImpl.kt`, `SelectBiometricsConfig.kt`, `ProgramViewModel.kt`, `SyncBiometricsConfig.kt`, `LoginActivity.kt`, `SyncPresenterImpl.kt`).
- [ ] 4.3 `biometrics-duplicate-review-confirm-identity` — reapply duplicate dialog, confirm identity, and `registerLast` wiring.
- [ ] 4.4 `biometrics-tei-ui-surfaces` — reapply search card/dashboard/enrollment/TEI-form biometric UI wiring.
- [ ] 4.5 `simprints-data-exchange-mapping` — reapply `BiometricsClientFactory` and `BiometricsClient` request/response mapping.
- [ ] 4.6 Pause and confirm with the user before starting the medium/low-risk manual-reapply batch.

## 5. Resolve manual conflicts — medium and low-risk capabilities

- [ ] 5.1 `biometrics-mode-controls` — reapply per-program mode gating (`full`/`limited`/`zero`).
- [ ] 5.2 `biometrics-age-threshold` — reapply age-threshold gating across search/enrollment/dashboard.
- [ ] 5.3 `biometrics-confidence-score-filtering` — reapply confidence filtering in `BiometricsClient`.
- [ ] 5.4 `biometrics-org-unit-module-id` — reapply module id derivation logic.
- [ ] 5.5 `biometrics-verification-persistence` — reapply verification persistence and read paths.
- [ ] 5.6 `biometrics-date-of-birth-attribute` — reapply configured DOB attribute reads.
- [ ] 5.7 `biometrics-relationship-search-tet-toggle` — reapply relationship-search TE-type toggle.
- [ ] 5.8 `biometrics-time-based-windows` — reapply verification/decline time-window logic.

## 6. Deferred and shared-drift resolution

- [ ] 6.1 Reconcile `app/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml` against the new baseline's flavor/composite-build blocks — minimum needed to compile (see design.md Open Questions).
- [ ] 6.2 Confirm the project builds (`./gradlew assembleSimprintsDebug`) before finalizing `defer_after_build_verification` files.

## 7. Automerge verification

- [ ] 7.1 For every file listed in `customization-files.md` (not only conflicted ones), run `git diff develop-eyeseetea -- <file>` and confirm all documented customization lines survived.
- [ ] 7.2 Recover any silently dropped customization code found in 7.1 before staging.

## 8. Validation

- [ ] 8.1 Run `./gradlew testSimprintsDebugUnitTest` and fix regressions in Simprints-specific tests (`SearchTEIViewModelTest.kt` and any others touched during manual reapply).
- [ ] 8.2 Manually validate high-risk flows from `upgrade-validation-checklist.md`: #1 Biometric Search Integration, #2 Biometrics Configuration Selection, #9 Biometric Duplicate Review And Confirm Identity, #10 Biometrics In TEI Cards/Dashboard/Enrollment/Form, #13 Simprints Data Exchange And Mapping.
- [ ] 8.3 Manually validate medium-risk flows: #3 Biometrics Mode Controls, #4 Age Threshold Controls, #6 Confidence Score Filtering, #7 Org Unit Derived Module Id, #11 Biometric Verification Persistence.
- [ ] 8.4 Manually validate low-risk flows: #5 Configurable Date Of Birth Attribute, #8 Relationship Search Identification Toggle, #12 Time-Based Verification And Registration Failure Windows.
- [ ] 8.5 Validate #14 Areas Explicitly Out Of Scope For Preservation — confirm 2FA/login, notifications, change server URL, and granular sync match baseline behavior with no Simprints-specific variant left.

## 9. Close out

- [ ] 9.1 Update `customization-files.md` with confirmed surviving customizations and technical notes from the manual-reapply tranches.
- [ ] 9.2 Update `upgrade-3.4-notes.md` progress section (`merge started: yes`, `easy conflicts resolved`, `manual conflicts pending`, `validation started`) and close it out once the upgrade is validated.
- [ ] 9.3 Run `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client simprints` and confirm only expected out-of-scope noise remains (2FA/login, `settings.gradle.kts` baseline comments).
- [ ] 9.4 Run `openspec validate --specs --strict` and confirm all 13 specs still pass unchanged.
- [ ] 9.5 If this change surfaced a genuine capability-behavior change (not just an implementation move), stop and raise it with the user as a separate change instead of folding it in here.
