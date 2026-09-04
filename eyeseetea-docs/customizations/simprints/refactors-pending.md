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

## Medium/higher risk (touches validated flows — re-evaluate before picking up)

Each entry notes which validated flow it touches, so risk can be
re-assessed against what's changed since.

- **Extract `SimprintsResponseMapper` from `BiometricsClient.kt`** — splits the ~684-line file (register/identify/verify/confirmIdentify intent-building + SID→domain response parsing + logging) into a separate response-mapping class, keeping `BiometricsClient`'s public API unchanged. Real impact: isolates the most testable part (pure mapping, no `Activity`/`Fragment`) from the part that launches intents; roughly halves the file. Touches the file most tested on 2026-09-03 (identify/register flows, release build on device). Re-validate: full identify/register/verify/registerLast cycle on device after refactor.
  **Safety net done (2026-09-03): `BiometricsClientTest.kt` (21 tests, all `handle*Response()` methods) now exists and is green — see `customization-files.md` §2.12.** Even with the tests as a net, the extraction itself is not zero-risk: the mapper needs `confidenceScoreFilter` threaded in (used in `handleIdentifyResponse`/`getVerificationJudgementByDhis2`), and this file's models were the ones hit by the R8/minify deserialization bug fixed via `@SerializedName` in PR #312 — a restructuring here should get a release-build compile + device install pass (not just unit tests) before being trusted, since unit tests don't exercise obfuscation. Do the extraction after the delivery window, not before.
- **Redesign `EnrollmentPresenterImpl.biometricsUiModel` to stop caching a stale-prone `data class` reference** — the root cause of the `registerLast` bug fixed 2026-09-02 is that `BiometricsAttributeUiModelImpl.callback` isn't a constructor property, so `.copy()` (`onFieldsLoading()` line ~589-592: `.setValue().setEditable().setAgeUnderThreshold()`) drops it; a cached instance that Compose never repaints keeps `callback == null` forever. **Correction (2026-09-03, verified against the file): this is NOT a simple "swap the field for a `String uid`" change** — a full read of every `biometricsUiModel` usage in `EnrollmentPresenterImpl.kt` shows several call sites read `.value` directly (lines ~376, ~425, ~437, ~541 — failure-pattern checks, duplicate GUID comparison), which a bare UID can't answer without another way to look up the current value. More importantly, `onFieldsLoaded()` (lines ~507-539) registers **three** callbacks on the cached instance — `setBiometricsRegisterListener`, `setSaveTEI`, `setRegisterLastAndSave` — which is the *same* stale-callback pattern as the bug already fixed, just not yet confirmed broken in practice. A real fix needs to also address how those three listeners are wired (likely the same `FormView.submitIntent()`-style approach already used for `saveBiometricValue()`) and how `.value` is read without the cached reference — not just retype one field. Touches the exact flow validated end-to-end on device 2026-09-02/03 (`registerLast`). Re-validate: full `registerLast` cycle on device after refactor, including the register/save/registerLast listener paths specifically, not just the save-value path already covered by the 2026-09-02 fix.
  **Explicitly deferred 2026-09-03: do not start this before the 2026-09-04 delivery — it's the highest-risk item in this whole document, on the exact flow due tomorrow.** Two follow-ups worth doing first, once the deadline has passed: (1) investigate whether `setBiometricsRegisterListener`/`setSaveTEI`/`setRegisterLastAndSave` actually hit the stale-callback bug in practice (reproduce on device, don't just reason about it) — if they do, that's a second live bug in the same family as the one fixed 2026-09-02, not just tech debt; (2) only then scope the actual redesign.
- **Extract the biometric-attribute collapse-exclusion condition to a shared `:commonskmm` function** — today duplicated verbatim between `SearchTrackedEntities.prepareQuery()` (`tracker/src/commonMain/.../domain/SearchTrackedEntities.kt:76-98`) and `SearchRepositoryImpl.getFilteredRepository()` (`app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchRepositoryImpl.java:154-189`) — same condition, two languages, one fixed on 2026-09-03. Lower risk than the other two here (new shared file, doesn't change either call site's behavior), but still touches both files from the most-validated area of the 2026-09-03 session. Re-validate: biometric search (list view) + map view search with 2+ candidates.
- **Rename `usescases/biometrics/Verification.kt`** (confusing name collision with `data/biometrics/utils/Verification.kt` — not duplication, `usescases/biometrics/Verification.kt:8` holds the pure `isLastVerificationValid()` domain function, imported by the other file for orchestration) — `git mv` + update 1 import in `TEIDataPresenter.kt:54`. Lowest risk of this group (pure rename, existing tests unaffected), but still a diff on a file in the verification-persistence area.

## Explicitly not recommended

Evaluated and rejected — not worth revisiting unless the underlying constraints change.

- **Unify `addAttrBiometricsIconIfRequired.kt`/`addAttrNHISNumberIconIfRequired.kt`** — structurally similar but with real behavioral differences (age-threshold branch, `BIOMETRICS_ENABLED` guard, an extra non-emoji icon function only on the biometrics side). Parameterizing them would add more signature complexity than the ~10 duplicated lines it would save.
- **Migrate the map-view search path (`SearchRepositoryImpl.java`) to use the `SearchTrackedEntities` KMP use case**, to fully unify with the list-view path — would require rewriting `fetchMapResults()`/`MapDataRepository` to consume the KMP use case. Non-trivial architecture change for a coincidental duplication (see the shared-function proposal above for a lower-cost partial fix).

## Origin

First compiled 2026-09-03, from a 4-block parallel analysis of all 13 active
Simprints customizations (see `customization-files.md` §2.1-2.13), run after
closing the 3.4.1 upgrade's biometric search fix and the `develop-simprints`
merge. Session: `feature-simprints/upgrade_3.4.1`.
