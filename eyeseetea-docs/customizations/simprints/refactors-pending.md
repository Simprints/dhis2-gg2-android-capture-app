# Simprints refactors pending

Tracks simplification/maintainability work identified for the Simprints fork's
customizations, deliberately deferred rather than applied immediately.

## Purpose

This file is for:
- proposed simplifications to Simprints customizations, with concrete impact/risk
- proposed tests to close coverage gaps, with concrete impact/risk
- a place to re-evaluate and pick up items at each future upgrade

This file is not for:
- completed work (move to `customization-files.md`'s technical notes, or just
  let the diff speak for itself)
- upgrade-specific progress tracking (that's `upgrade-3.4-notes.md`)
- functional specs (that's `openspec/specs/`)

## How to use this file

At the start of each upgrade (or whenever there's slack time), review the
items below. Zero-risk items (tests only, no production code change) can be
picked up any time, independent of upgrade timing. Items with real risk
(touch validated flows) should be re-evaluated against what's been changed
since the note was written — a "medium risk" item can become safer once the
flow it touches has been re-validated for other reasons in a later upgrade.

When an item is done, remove it from here.

## Zero-risk (tests only, no production code change)

These can be picked up any time without re-validation, since they don't
change runtime behavior.

- **`SearchTrackedEntitiesTest.kt`: add a case for `biometricAttributeId`** (`tracker/src/commonTest/.../domain/SearchTrackedEntitiesTest.kt`) — the multi-candidate biometric search bug fixed 2026-09-03 has no regression test. Same pattern as the existing "should join multiple values when custom intent does not return list" test, just with `data.attributeId = biometricAttributeId` and asserting the values are NOT collapsed.
- **Regression test for `registerLast`/`saveBiometricValue`** (`app/src/test/java/org/dhis2/usescases/enrollment/EnrollmentPresenterImplTest.kt`) — the stale-`callback`-after-`.copy()` bug fixed 2026-09-02 has no test reproducing it. Mock `EnrollmentView`, call `onFieldsLoaded(fields)` then `onFieldsLoading(fields)` (applies `.copy()`), trigger `registerLastFailure()`/`onBiometricsCompleted()`, verify `enrollmentView.submitFormIntent(FormIntent.OnSave(...))` fires without going through a null callback.
- **Test for `isSearchByBiometricsEnabled()`** (`app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt:1620-1626`) — trivial pure function, no test today.
- **`SelectBiometricsConfigTest.kt`: add the "no default config" case** (`app/src/test/java/org/dhis2/usescases/biometrics/SelectBiometricsConfigTest.kt`) — `getDefaultConfig()` throws an uncaught exception when no `default` org-unit-group config exists (confirmed 2026-09-03, documented in `upgrade-validation-checklist.md` §2). No test covers this failure path.
- **Complete `AgeInMonthsTest.kt`** — only 1 of 4 age-calculation functions (`getAgeInMonthsByAttributes`, `getAgeInMonthsBySearchItemAttributes`, `getAgeInMonthsByFieldUiModel`, `containsAgeFilterAndIsUnderAgeThreshold`) has a test. Do this before touching the `TODO` already in `AgeInMonths.kt:82-91` about unifying them.
- **`TeiDashboardCardMapperTest.kt`** — doesn't exist. `TEICardMapper` has test coverage for its biometric/NHIS decoration; `TeiDashboardCardMapper` shares the pattern but has none.

- **ktlint debt in Simprints code: `./gradlew ktlintCheck` fails with ~1,775 violations in 137 files** (measured 2026-10-22, during the 3.5.0 upgrade):
  - 87 Simprints-only files, ~1,120 violations;
  - 49 Oslo files carrying Simprints blocks, ~654 violations. Worst offenders: `SearchTEActivity.kt`, `EnrollmentActivity.kt`, `TEIDataFragment.kt`, `TEIDataPresenter.kt`, `EnrollmentPresenterImplTest.kt`, `EnrollmentPresenterImpl.kt`.

  Not caused by the upgrade: `eyeseetea-main.yml` never runs ktlint, so violations have accumulated since the fork began. `AGENTS.md` requires ktlint to pass before committing.
  - **Scope:** formatting only, no behavior change. However, an `ktlintFormat` over the 49 Oslo files will rewrite Simprints blocks inside Oslo code. Also note that the merge-touched lines of the 3.5.0 upgrade were checked to add no new violations.
  - **Agreed approach (user, 2026-10-22):** keep it out of the upgrade merge commit and do it as a separate `style:` commit or PR with its own review.
  - **Not decided:** whether to also add ktlint to the fork CI afterwards.

## Medium/higher risk (touches validated flows — re-evaluate before picking up)

Each entry notes which validated flow it touches, so risk can be
re-assessed against what's changed since.

- **Extract `SimprintsResponseMapper` from `BiometricsClient.kt`** — splits the ~684-line file (register/identify/verify/confirmIdentify intent-building + SID→domain response parsing + logging) into a separate response-mapping class, keeping `BiometricsClient`'s public API unchanged. Real impact: isolates the most testable part (pure mapping, no `Activity`/`Fragment`) from the part that launches intents; roughly halves the file. Touches the file most tested on 2026-09-03 (identify/register flows, release build on device). Re-validate: full identify/register/verify/registerLast cycle on device after refactor.
  **Safety net done (2026-09-03): `BiometricsClientTest.kt` (21 tests, all `handle*Response()` methods) now exists and is green — see `customization-files.md` §2.12.** Even with the tests as a net, the extraction itself is not zero-risk: the mapper needs `confidenceScoreFilter` threaded in (used in `handleIdentifyResponse`/`getVerificationJudgementByDhis2`), and this file's models were the ones hit by the R8/minify deserialization bug fixed via `@SerializedName` in PR #312 — a restructuring here should get a release-build compile + device install pass (not just unit tests) before being trusted, since unit tests don't exercise obfuscation. Do the extraction after the delivery window, not before.
- **Redesign `EnrollmentPresenterImpl.biometricsUiModel` to stop caching a stale-prone `data class` reference** — the root cause of the `registerLast` bug fixed 2026-09-02 is that `BiometricsAttributeUiModelImpl.callback` isn't a constructor property, so `.copy()` (`onFieldsLoading()` line ~589-592: `.setValue().setEditable().setAgeUnderThreshold()`) drops it; a cached instance that Compose never repaints keeps `callback == null` forever. **Correction (2026-09-03, verified against the file): this is NOT a simple "swap the field for a `String uid`" change** — a full read of every `biometricsUiModel` usage in `EnrollmentPresenterImpl.kt` shows several call sites read `.value` directly (lines ~376, ~425, ~437, ~541 — failure-pattern checks, duplicate GUID comparison), which a bare UID can't answer without another way to look up the current value. More importantly, `onFieldsLoaded()` (lines ~507-539) registers **three** callbacks on the cached instance — `setBiometricsRegisterListener`, `setSaveTEI`, `setRegisterLastAndSave` — which is the *same* stale-callback pattern as the bug already fixed, just not yet confirmed broken in practice. A real fix needs to also address how those three listeners are wired (likely the same `FormView.submitIntent()`-style approach already used for `saveBiometricValue()`) and how `.value` is read without the cached reference — not just retype one field. Touches the exact flow validated end-to-end on device 2026-09-02/03 (`registerLast`). Re-validate: full `registerLast` cycle on device after refactor, including the register/save/registerLast listener paths specifically, not just the save-value path already covered by the 2026-09-02 fix.
  **Explicitly deferred 2026-09-03: do not start this before the 2026-09-04 delivery — it's the highest-risk item in this whole document, on the exact flow due tomorrow.** Two follow-ups worth doing first, once the deadline has passed: (1) investigate whether `setBiometricsRegisterListener`/`setSaveTEI`/`setRegisterLastAndSave` actually hit the stale-callback bug in practice (reproduce on device, don't just reason about it) — if they do, that's a second live bug in the same family as the one fixed 2026-09-02, not just tech debt; (2) only then scope the actual redesign.
  **Follow-up (1) answered 2026-09-04: not this bug.** Reproduced a `registerLast` hang on delivery day and traced it with `Timber` at every hop. `setRegisterLastAndSave`'s listener was live (`registerLastAndSave() invoked ... listener is null=false`) and its body ran correctly (`pendingSave = true`, `view.registerLast(...)`) — the stale-callback pattern did **not** reproduce for these three listeners. The actual cause was a different, independent bug: a race in `FormViewModel`'s `fieldListChannel` (Oslo) that can silently drop the `finish=true` signal when `registerLast`'s two-step save (biometric value + auto-triggered form save) fires as a tight burst of intents. See `customization-files.md` §2.10 ("registerLast hang, second cause") and `upgrade-3.4-notes.md` B6 for the full trace and fix (a 500ms debounce in `EnrollmentActivity`, scoped to Simprints). This redesign item's risk assessment stands as written above — the three listeners are still a latent stale-callback risk in principle, just not the one that fired this time — so it stays deferred, re-evaluate independently of the channel-race fix.
- **Extract the biometric-attribute collapse-exclusion condition to a shared `:commonskmm` function** — today duplicated verbatim between `SearchTrackedEntities.prepareQuery()` (`tracker/src/commonMain/.../domain/SearchTrackedEntities.kt:76-98`) and `SearchRepositoryImpl.getFilteredRepository()` (`app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchRepositoryImpl.java:154-189`) — same condition, two languages, one fixed on 2026-09-03. Lower risk than the other two here (new shared file, doesn't change either call site's behavior), but still touches both files from the most-validated area of the 2026-09-03 session. Re-validate: biometric search (list view) + map view search with 2+ candidates.
- **Rename `usescases/biometrics/Verification.kt`** (confusing name collision with `data/biometrics/utils/Verification.kt` — not duplication, `usescases/biometrics/Verification.kt:8` holds the pure `isLastVerificationValid()` domain function, imported by the other file for orchestration) — `git mv` + update 1 import in `TEIDataPresenter.kt:54`. Lowest risk of this group (pure rename, existing tests unaffected), but still a diff on a file in the verification-persistence area.

- **Rethink the biometric duplicates dialog: it is a hand-copied fork of the search screen's wiring, so every Oslo change to search breaks it silently** (`app/src/main/java/org/dhis2/usescases/biometrics/duplicates/`: `BiometricsDuplicatesDialogModule.kt`, `BiometricsDuplicatesDialogPresenter.kt`, `BiometricsDuplicatesDialogAdapter.kt`). The dialog was born as a copy of the search flow:
  - its Dagger module re-declares by hand what `SearchTEModule` provides (`SearchRepositoryImplKt`, `SearchTrackedEntityRepositoryImpl`, `SearchTrackedEntities`, `TEICardMapper`, `DomainErrorMapper`);
  - its presenter re-implements the paging-to-`SearchTeiModel` mapping that `SearchTEIViewModel` already does;
  - its adapter re-renders search cards outside `SearchTeiLiveAdapter`.

  Because these are copies and not shared code, upstream changes never conflict in git. They only surface as compile errors or, worse, as behavior drift. The 3.5.0 upgrade (2026-10-22) hit this 4 times in one go: `customLabelProvider` added to `SearchRepositoryImplKt` and `TEICardMapper` (`206a40711`, `105beb771`), `domainErrorMapper` added to `SearchTrackedEntityRepositoryImpl` (`cf72c54d5`), and the search paging type changing to `TrackedEntitySearchPagingItem` (`cf72c54d5`), which forced a dialog-specific decision on how to handle per-row errors that Oslo renders with a view holder the dialog doesn't have.
  - **Scope of the problem:** any Oslo change to the search module's constructors, the search use case's output type, or card mapping. Each upgrade needs a manual re-sync of the dialog against `SearchTEModule` / `SearchTEIViewModel`, and nothing flags when that's needed except a broken build.
  - **Not decided:** how to fix it. Options haven't been evaluated or agreed. Record any proposal here first, before implementing.
  - **Touches:** §2.9 "Biometric Duplicate Review And Confirm Identity", a high-risk, validated flow. Re-validate: identify with 2+ Simprints candidates → duplicates dialog → confirm identity, online and offline.

- **The TEI dashboard has no embedded enrollment form in landscape** (`app/src/main/java/org/dhis2/usescases/teiDashboard/TeiDashboardMobileActivity.kt`, `setFormViewForLandScape()`; `app/src/main/res/layout-land/activity_dashboard_mobile.xml`). Oslo's landscape dashboard has three columns: TEI data, an embedded enrollment form (`tei_form_view`), and Analytics/Relations/Notes. The fork hides the form column and adds an "Edit person" button that opens `EnrollmentActivity` instead (commit `6bbf480bb2`).
  - **Problem:** the biometric attribute handling lives in `EnrollmentPresenterImpl.onFieldsLoading()` (hides `Biometrics Template`, filters the biometric field outside `full` mode, sets editable from mandatory-field completion, sets the under-age flag), wired through `EnrollmentActivity` (`onFieldsLoading = { presenter.onFieldsLoading(fields) }`). The dashboard's embedded form is built with `buildEnrollmentForm(...)` without that listener, so it would render the biometric field and `Biometrics Template` unfiltered.
  - **Scope of the problem:** landscape tablets only. The user cannot edit the TEI inline in landscape and has to go through "Edit person". Any Oslo change to the landscape dashboard layout has to be re-checked against the hidden column.
  - **Not decided:** whether to restore the inline form. Options haven't been evaluated or agreed. Record any proposal here first, before implementing.
  - **Touches:** §2.10 "Biometrics In TEI Cards, TEI Dashboard, Enrollment, And TEI Form", a high-risk, validated flow. Re-validate: dashboard in landscape in `full`, `limited` and `zero` modes, plus the "Edit person" path.

- **The biometrics search placeholder is stored on the TEI** (`SearchRepositoryImpl.saveToEnroll`, `SequentialSearch.finalQueryData`, `BiometricsTEIRegistration`, `HomeRepositoryImpl` init cleanup)
  - **Problem:** after a biometric search, "Enroll" copies the whole query as attribute values of the new TEI, including the biometrics attribute as `BIOMETRICS_SEARCH_<session>_<uids>`. It is replaced by a real GUID on `register`/`registerLast`, or by a failure placeholder on decline. A TEI saved without registering biometrics keeps it, and can sync to the server. At least 10 places check the placeholder prefixes by hand, and `HomeRepositoryImpl` cleans them up only when Home is created. Simprints reported the placeholder reaching `confirmIdentity` (2026-11-12); `selectedGuidOrNone` in `BiometricsClient` stops it at the exit, not at the source.
  - **Scope of the problem:** TEIs created through the sequential search, found later as confirm-identity candidates (suspected case, not reproduced: a credential-only match). `saveToEnroll` is an Oslo file, and `BiometricsTEIRegistration` reads the placeholder to get the search session id for "link last biometrics".
  - **Not decided:** whether and how to stop persisting the placeholder. Options haven't been evaluated or agreed. Needs a real device log (`confirmIdentify` logs `guid: ...`) to confirm how the placeholder arrives.
  - **Touches:** §2.1 "Biometric Search Integration" and §2.9 "Biometric Duplicate Review And Confirm Identity". Re-validate: biometric search with no match → Enroll → save without registering → search again and confirm identity on that TEI.

## Explicitly not recommended

Evaluated and rejected — not worth revisiting unless the underlying constraints change.

- **Unify `addAttrBiometricsIconIfRequired.kt`/`addAttrNHISNumberIconIfRequired.kt`** — structurally similar but with real behavioral differences (age-threshold branch, `BIOMETRICS_ENABLED` guard, an extra non-emoji icon function only on the biometrics side). Parameterizing them would add more signature complexity than the ~10 duplicated lines it would save.
- **Migrate the map-view search path (`SearchRepositoryImpl.java`) to use the `SearchTrackedEntities` KMP use case**, to fully unify with the list-view path — would require rewriting `fetchMapResults()`/`MapDataRepository` to consume the KMP use case. Non-trivial architecture change for a coincidental duplication (see the shared-function proposal above for a lower-cost partial fix).

## Origin

First compiled 2026-09-03, from a 4-block parallel analysis of all 13 active
Simprints customizations (see `customization-files.md` §2.1-2.13), run after
closing the 3.4.1 upgrade's biometric search fix and the `develop-simprints`
merge. Session: `feature-simprints/upgrade_3.4.1`.
