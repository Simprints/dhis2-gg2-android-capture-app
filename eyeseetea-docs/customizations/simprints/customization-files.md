# Simprints customization files

Use this file as the technical inventory of the Simprints fork.

## Purpose

This file is for:
- listing where each confirmed Simprints customization is implemented
- separating direct flavor files from shared-code implementation points
- tracking the technical status of each customization against `develop-eyeseetea`

This file is not for:
- raw full diff dumps
- temporary upgrade progress
- stable merge rules
- functional intent or business justification

## Mandatory header

- Client: `simprints`
- Flavor: `simprints`
- Base branch: `develop-eyeseetea`
- Base branch head at onboarding time: `73a7eb8f0fcc127d37d8887144673e480c4d5b93`
- Fork divergence point against base branch: `d87193d003a0acccc53914f88026719df6fe8fc3`
- Generated on: `2026-05-12`
- Working tree status: `dirty`

## Scope

This inventory is based on:
- direct flavor files under `app/src/simprints/` and `app/src/simprintsDebug/`
- shared-code implementation points currently marked with `EyeSeeTea customization`
- current diffs against `develop-eyeseetea` used only as supporting evidence

Commit note:
- `73a7eb8f0fcc127d37d8887144673e480c4d5b93` is the `develop-eyeseetea` branch head used as the onboarding baseline reference on 2026-05-12.
- `d87193d003a0acccc53914f88026719df6fe8fc3` is the current `merge-base` between `develop-eyeseetea` and `feature-simprints/upgrade_3.4`, which is the most useful anchor for shared-drift review.

## 1. Direct simprints flavor surface

### 1.1 Flavor code

- `app/src/simprints/java/org/dhis2/data/user/UserComponentFlavor.kt`

### 1.2 Flavor resources and branding

- `app/src/simprints/`
- `app/src/simprintsDebug/`
- launcher icons, round icons, foreground/background resources, and localized `strings.xml` files under both flavor source sets

## 2. Shared-code customization implementation points

### 2.1 Biometric Search Integration

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/biometrics/`
- `app/src/main/java/org/dhis2/usescases/biometrics/`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigApi.kt`
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigRepositoryImpl.kt`
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigDTO.kt`
- `app/src/main/java/org/dhis2/data/biometrics/GetBiometricsConfig.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/usecases/SelectBiometricsConfig.kt`
- `app/src/main/res/layout/dialog_biometrics_duplicates.xml`
- `app/src/main/res/layout/item_biometrics_duplicate.xml`
- `app/src/main/res/drawable/ic_bio_available_yes.xml`
- `app/src/main/res/drawable/ic_bio_available_no.xml`

Technical note:
- Large Simprints-specific biometrics surface including client, config API, SID models, TEI attribute helpers, duplicates dialog, sequential search helpers, and dashboard mappers. This is the clearest active customization area in the fork.

### 2.2 Biometrics Configuration Selection Per Program Or Org Unit Group

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigDTO.kt`
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigApi.kt`
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigRepositoryImpl.kt`
- `app/src/main/java/org/dhis2/data/biometrics/GetBiometricsConfig.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/usecases/SelectBiometricsConfig.kt`
- `app/src/main/java/org/dhis2/usescases/main/program/ProgramViewModel.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/login/SyncBiometricsConfig.kt`
- `app/src/main/java/org/dhis2/usescases/login/LoginActivity.kt`
- `app/src/simprints/java/org/dhis2/di/PostMetadataSyncModule.kt` — registers the post-metadata-sync refresh (since 3.4.1)
- `app/src/main/java/org/dhis2/di/KoinInitialization.kt` — single line registering `postMetadataSyncModule` (Oslo file, flavor-agnostic)

Depends on the baseline `PostMetadataSyncAction` extension point — **not a Simprints customization**. Documented in `customizations/eyeseetea/customizations-eyeseetea.md` §6; see also technique T2 in `customization-techniques.md`.

Technical note:
- The fork downloads a list of biometrics configurations, stores them locally, and chooses one active configuration when the user enters a program. Selection precedence is `program` first, then matching `orgUnitGroup`, then `default`. A `default` config is required and absence is treated as an error. The chosen configuration is flattened into preferences and drives `projectId`, `biometricsMode`, thresholds, module-id logic, verification duration, and TE type identification behavior.
- The configuration is refreshed on two independent paths: at login (`SyncBiometricsConfig` → `LoginActivity`) and after every metadata sync (`PostMetadataSyncAction`). Both are required — login covers a logged-out user getting fresh config, the hook covers a user who stays logged in. Until 3.4.1 the second path lived in `SyncPresenterImpl.syncMetadata()`, which Oslo removed when metadata sync moved to the KMP `:sync` module.

### 2.3 Biometrics Mode Controls Per Program

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/usescases/main/program/ProgramViewModel.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchJavaToCompose.kt`
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentActivity.kt`
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentPresenterImpl.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEScreenState.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/entities/BiometricsConfig.kt`

Technical note:
- `ProgramViewModel` applies the selected config before navigating into a program. The active `biometricsMode` then affects downstream behavior: `full` enables biometric search and registration paths, `limited` removes biometric registration UI while preserving some biometric handling, and `zero` suppresses biometric UI/actions such as dashboard biometrics cards.

### 2.4 Age Threshold Controls For Biometrics

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/usescases/biometrics/AgeInMonths.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt`
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentPresenterImpl.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/ui/mapper/TEICardMapper.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/addAttrBiometricsIconIfRequired.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/teiDashboard/ui/TeiDetailDashboard.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataFragment.kt`

Technical note:
- The fork computes age in months from the configured date-of-birth attribute and compares it to `ageThresholdMonths`. Below the threshold, search skips the biometric next-action path, dashboard biometrics actions are suppressed, enrollment biometric UI is flagged as under-threshold, and card rendering shows `Not Applicable` for missing biometrics instead of a negative marker.

### 2.5 Configurable Date Of Birth Attribute For Biometrics

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigDTO.kt`
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigRepositoryImpl.kt`
- `app/src/main/java/org/dhis2/data/biometrics/GetBiometricsConfig.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/AgeInMonths.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt`
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentPresenterImpl.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt`

Technical note:
- `dateOfBirthAttribute` is stored as part of the selected biometrics config and acts as the attribute UID to read birth date from query data, TEI attributes, and enrollment form fields. It is the anchor used by all age-threshold decisions.

### 2.6 Confidence Score Filtering For Simprints Matches

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigDTO.kt`
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigRepositoryImpl.kt`
- `app/src/main/java/org/dhis2/data/biometrics/GetBiometricsConfig.kt`
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsClientFactory.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/BiometricsClient.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/SimprintsIdentifiedItem.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/sid/IdentificationSID.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/sid/VerificationSID.kt`

Technical note:
- `confidenceScoreFilter` is stored in the selected config and passed into `BiometricsClient`. Identification results are filtered by numeric confidence except for credential-linked matches, and verification results are only accepted as matches when their confidence meets the threshold. The filtering lives in `BiometricsClient.handleIdentifyResponse()` and `BiometricsClient.handleVerifyResponse()`, so downstream search and dashboard code consume already-normalized result models.

### 2.7 Org Unit Derived Module Id For Simprints

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigDTO.kt`
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigRepositoryImpl.kt`
- `app/src/main/java/org/dhis2/data/biometrics/GetBiometricsConfig.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/OrgUnitAsModuleId.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/OrgUnitAsModuleIdByList.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEPresenter.java`
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentPresenterImpl.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/BiometricsClient.kt`

Technical note:
- `orgUnitLevelAsModuleId` is stored in the selected config and used to transform organisation unit context into the `moduleId` sent to Simprints. For single-org-unit flows it walks the org unit path with a relative offset and clamps to level 4; for multi-org-unit search identify it tries to compute a unique shared level-4 parent and falls back to `BiometricsClient.DefaultModuleId` if no single parent exists.

### 2.8 Relationship Search Identification Toggle By TE Type

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigDTO.kt`
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigRepositoryImpl.kt`
- `app/src/main/java/org/dhis2/data/biometrics/GetBiometricsConfig.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTeiViewModelFactory.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/listView/SearchTEList.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/mapView/SearchTEMap.kt`

Technical note:
- `enableIdentificationForTET` is persisted in the selected config and is used only in relationship-driven search. In that context, biometric search is enabled only when the current tracked entity type UID matches the configured `enableIdentificationForTET` value.

### 2.9 Biometric Duplicate Review And Confirm Identity

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEActivity.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEPresenter.java`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchTEIViewModel.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/SearchRepositoryImpl.java`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/listView/SearchTEList.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/ui/mapper/TEICardMapper.kt`
- `tracker/src/commonMain/kotlin/org/dhis2/tracker/search/domain/SearchTrackedEntities.kt` (Oslo file; single condition added to `prepareQuery()`, see technical note)

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/biometrics/ui/SequentialSearch.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/ui/SequentialNextSearchAction.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/LegacyInteraction.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/duplicates/BiometricsDuplicatesDialog.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/duplicates/BiometricsDuplicatesDialogPresenter.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/BiometricsClient.kt`
- `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/biometrics/attributes.kt` (`biometricAttributeId`, `nhisNumberAttributeId` — moved here from `:app` 2026-09-03 so KMP modules like `:tracker` can read them without an Android-only dependency; see technical note)

Technical note:
- Search behavior is tightly coupled to the Simprints biometric app and duplicate handling. `BiometricsClient.handleIdentifyResponse()` keeps credential-linked matches even when below the confidence threshold, and the duplicate flow can branch into confirm identity, open existing TEI dashboard, or `registerLast` for new enrollment completion. `BiometricsDuplicatesDialogPresenter` resolves duplicate candidates by issuing a normal DHIS2 search on the biometrics attribute UID with the Simprints GUID list. Several files already carry `EyeSeeTea customization` comments that point to active fork behavior.
- **Biometric search with multiple Simprints candidates bug (found and fixed 2026-09-03, upgrade 3.4.1):** `SearchRepositoryImpl.getFilteredRepository()` already carries the correct 3.3.1 behavior — it excludes the biometric attribute from the generic "collapse multiple values into one comma-joined string" logic (`!dataId.equals(biometricAttributeId)`), because a `identify` search resolves to a list of Simprints-returned candidate GUIDs, not free text where a comma could be literal. There are **three independent, non-overlapping search call paths** across `SearchTEIViewModel` and `BiometricsDuplicatesDialogPresenter`, and this bug affected two of them:
  - **List view** (`SearchTEIViewModel.loadSearchResults()`/`loadDisplayInListResults()`): calls `searchTrackedEntities.invoke(...)`, the KMP use case `SearchTrackedEntities.prepareQuery()` (`tracker/src/commonMain/.../domain/SearchTrackedEntities.kt`). **Broken.**
  - **Duplicate resolution** (`BiometricsDuplicatesDialogPresenter`, line ~95): calls the *same* `searchTrackedEntities.invoke(input)` use case, built by its own Dagger provider in `BiometricsDuplicatesDialogModule.kt` — `searchRepository` (the Java one) is injected there too, but only for `updateAttributeValue`/`downloadTei`, never for the search itself. **Broken.**
  - **Map view** (`SearchTEIViewModel.fetchMapResults()` → `MapDataRepository.searchTeiForMap()` → `SearchRepositoryKt`/`SearchRepositoryImplKt.searchTeiForMap()` → `SearchRepositoryImpl.getFilteredRepository()`): this is `SearchRepositoryImpl.java`'s only real caller today. **Never broken** — already had the correct exclusion from 3.3.1.
  - Baseline's `[ANDROAPP-7495]` search refactor introduced `SearchTrackedEntities.kt` with the same generic collapse logic as `SearchRepositoryImpl` but **never had the biometric exclusion** — it's new baseline code with no 3.3.1 equivalent to diff against, so the automerge-casualty checklist (which compares against a known prior version) didn't catch it. Confirmed with device tracing on 2026-09-03: `identify` returning 2+ candidates always collapsed to `"guid1,guid2"` and hit `.like()` with no match on the list-view path, while a single candidate happened to skip the collapse branch (`size == 1`) and worked, which is why the bug wasn't obvious in casual testing. Verified the log trail stopped right after `SearchTEIViewModel.onSearch()` and never reached `SearchRepositoryImpl`.
  - Fixed by mirroring the same exclusion in `SearchTrackedEntities.prepareQuery()` (`data.attributeId != biometricAttributeId`), reading the constant directly from `:commonskmm` rather than adding a constructor parameter, to keep the Oslo-file diff to a single added condition. This single fix covers both the list-view and duplicate-resolution paths, since both build their own `SearchTrackedEntities` instance from the same class. `SearchRepositoryImpl.java`'s exclusion is untouched and still needed for the map-view path — not redundant, since these are genuinely separate code paths.
- **`IdentifyResult.UserNotFound` carries `scannedCredential` (added on `develop-simprints`, merged into this branch 2026-09-03):** when Simprints returns `userNotFound` for an `identify` search, the response can still carry a scanned credential (e.g. a card/QR read during the attempt). Before this fix that credential was dropped in the `UserNotFound` branch, so if the user had started from a demographic (attribute) search and scanned a credential during a failed biometric attempt, the confirmation call couldn't be sent with it. `BiometricsClient.handleIdentifyResponse()` now attaches `scannedCredential` to `UserNotFound` the same way it already did for `Completed`; `SearchTEActivity.simulateNotFoundBiometricsSearch()` gained a second `scannedCredential: ScannedCredential?` parameter to thread it through for all its callers (`null` for `BiometricsDeclined`/`Failure`/`AgeGroupNotSupported`, `result.scannedCredential` for the real `UserNotFound` case).
- **Stale program results shown after returning from Simprints (fixed on `develop-simprints`, merged into this branch 2026-09-03):** Oslo's `ANDROAPP-7647` fix (detach the list adapter and show a loading indicator on new search, re-attach once `loadStateFlow` settles, guarded by `lastSearchPagingData` against a lifecycle-drop re-subscription re-triggering the detach) was ported as-is into `SearchTEList.kt`. It replaced an earlier Simprints-only mechanism (`configureBiometricsLoader`/`configureRecyclerVisibility`, a `biometricAppLaunching` `LiveData` plus a loader `ComposeView`) that raced against `loadStateFlow`/`searchPagingData` re-emission on lifecycle re-subscription — that mechanism was reverted. On top of the ported Oslo fix, `SearchTEIViewModel`/`SearchTEActivity` now call `hideStaleProgramResults()` proactively the moment the biometric app is about to launch, instead of waiting for a new `PagingData` emission, closing a window where a lifecycle drop below `STARTED` while Simprints is foregrounded could otherwise leave stale results visible on return.

### 2.10 Biometrics In TEI Cards, TEI Dashboard, Enrollment, And TEI Form

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentActivity.kt`
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentPresenterImpl.kt`
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentView.kt`
- `app/src/main/java/org/dhis2/data/forms/dataentry/ValueStoreImpl.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/TeiDashboardMobileActivity.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataFragment.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/ui/mapper/TeiDashboardCardMapper.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/usescases/biometrics/ui/teiDashboardBiometrics/`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/DashboardProgramModel.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/addAttrBiometricsIconIfRequired.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/addAttrNHISNumberIconIfRequired.kt`
- `app/src/main/java/org/dhis2/usescases/searchTrackEntity/ui/mapper/TEICardMapper.kt`
- `form/src/main/java/org/dhis2/form/ui/FormView.kt` (Oslo file; single added method `submitIntent(intent: FormIntent)`, see technical note)

Technical note:
- Simprints extends enrollment, TEI form, dashboard, and search-card workflows with biometric status, actions, attribute handling, and registration/verification mapping. In enrollment/TEI form the active behavior is registration, duplicate handling, and `registerLast`; verification is not driven from the form flow. In TEI dashboard there are both registration and verification flows. `TEICardMapper` preserves biometrics and NHIS rows even when other empty attributes are hidden, decorates those rows with custom markers, and derives avatar initials from first-name/last-name attributes. This looks like core product behavior, not upgrade drift.
- `EnrollmentPresenterImpl.saveBiometricValue()` submits its `FormIntent.OnTextChange`/`OnSave` directly via `FormView.submitIntent()` (new method, `EnrollmentView.submitFormIntent()`), instead of `biometricsUiModel.onTextChange()/onSave()`. Reason: `BiometricsAttributeUiModelImpl` is a data class whose `callback: FieldUiModel.Callback?` field is not a constructor parameter, so `.copy()` (used by `onFieldsLoading`'s `.setValue().setEditable().setAgeUnderThreshold()`) drops it; the callback is only reattached when `Form.kt` actually paints that exact instance. Since baseline moved `onFieldsLoadingListener`/`onFieldsLoadedListener` from the `FormView` composable into `FormViewModel.items`' `map{}` (as part of its own upstream migration), the presenter-held `biometricsUiModel` reference can outlive that repaint and end up with `callback == null`, silently swallowing the save. Found and fixed 2026-09-02 (upgrade 3.4.1); see `upgrade-3.4-notes.md` for the full trace and the two other independent causes fixed alongside it (`onActivityResult` dropping non-`RESULT_OK` Simprints results, and Compose's `LaunchedEffect(items)` deduplication skipping `onFieldItemsRendered` on some emissions — the latter fixed via `LaunchedEffect(Unit) { viewModel.items.collect { render(it) } }`, also in `FormView.kt`).

**Tech debt (not addressed 2026-09-02, deferred):** this fix works but is a workaround, not a clean design — it bypasses `FieldUiModel.Callback` entirely for the biometrics field instead of fixing why the callback goes stale, and it required adding two new methods across three files (`FormView.submitIntent`, `EnrollmentView.submitFormIntent`, `EnrollmentActivity` impl) plus a second Oslo-file change (`LaunchedEffect(Unit)` in the same `FormView.kt`) just to keep the pre-existing `pendingSave` mechanism working. Worth revisiting for a cleaner approach — e.g. having `EnrollmentPresenterImpl` react to `onFieldsLoaded` (which already fires per-emission from `FormViewModel`, unaffected by the callback/Compose timing issue) instead of holding a `biometricsUiModel` reference and depending on Compose to keep its `callback` alive. Deferred deliberately to keep the upgrade fix minimal and attributable; revisit once the upgrade is closed.

### 2.11 Biometric Verification Persistence

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/biometrics/utils/Verification.kt`
- `app/src/main/java/org/dhis2/usescases/biometrics/Verification.kt`
- `app/src/main/java/org/dhis2/data/biometrics/utils/updateNHISNumberAttributeValue.kt`
- `app/src/main/java/org/dhis2/data/biometrics/utils/GetTrackedEntityAttributeValueByAttribute.kt`
- `app/src/main/java/org/dhis2/data/biometrics/utils/GetTeiByUid.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/VerifyResult.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/ConfirmIdentityResult.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/SimprintsConfirmIdentityItem.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/BiometricsClient.kt`

Technical note:
- This area covers the fork-specific path that sends data to Simprints, receives verification/identification results, and maps those results back into DHIS2 attributes and TEI state.

### 2.12 Simprints Data Exchange And Mapping

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsClientFactory.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/BiometricsClient.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/data/biometrics/GetBiometricsConfig.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/RegisterResult.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/IdentifyResult.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/VerifyResult.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/ConfirmIdentityResult.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/ScannedCredential.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/sid/RegistrationSID.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/sid/IdentificationSID.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/sid/VerificationSID.kt`
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/sid/ScannedCredentialSID.kt`

Technical note:
- `BiometricsClientFactory` creates the client from the currently selected biometrics preferences, using the selected `projectId`, the current DHIS2 username, the selected `confidenceScoreFilter`, and `BuildConfig.VERSION_NAME` as `forkVersion`. If preferences are missing, it falls back to hardcoded defaults (`projectId = Ma9wi0IBdo215PKRXOf5`, username `admin`, threshold `0`). `BiometricsClient` then acts as the integration boundary with the external Simprints app: it builds metadata containing `forkVersion`, `trackedEntityInstanceId`, `enrollingOrgUnitId`, `enrollingOrgUnitName`, `userOrgUnits`, and `subjectAge`; injects the backported `versionCode=20250102` extra to force JSON responses; and converts Simprints registration, identify, verify, confirm-identity, and `registerLast` responses into DHIS2-side result models, including scanned credential propagation when present.

### 2.13 Time-Based Verification And Registration Failure Windows

Status: `active`

Main implementation points:
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigDTO.kt`
- `app/src/main/java/org/dhis2/data/biometrics/BiometricsConfigRepositoryImpl.kt`
- `app/src/main/java/org/dhis2/data/biometrics/GetBiometricsConfig.kt`
- `app/src/main/java/org/dhis2/data/biometrics/utils/Verification.kt`
- `app/src/main/java/org/dhis2/usescases/teiDashboard/dashboardfragments/teidata/TEIDataPresenter.kt`
- `app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentPresenterImpl.kt`

Supporting files in the same workflow:
- `app/src/main/java/org/dhis2/data/biometrics/biometricsClient/models/RegisterResult.kt`

Technical note:
- `lastVerificationDuration` defines how long a saved verification remains valid before the app drops it from active verification state. `lastDeclinedEnrolDuration` defines how long failed/declined registration state remains before the UI clears it automatically in enrollment and dashboard flows.

## 3. Areas explicitly out of scope for preservation

This section documents differences that are intentionally **not** modeled as
active customizations and have no matching `openspec/specs/` entry — they are
recorded here so the merge removes them deliberately instead of by accident.
See `openspec/config.yaml` ("Out of scope for OpenSpec") for the same list.

Status: `removed`

Main implementation points:
- `login/`
- `app/src/main/java/org/dhis2/data/server/OpenIdSession.kt`
- `app/src/main/java/org/dhis2/usescases/login/`
- `app/src/main/java/org/dhis2/data/notifications/`
- `app/src/main/java/org/dhis2/usescases/notifications/`
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLComponent.kt`
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLModule.kt`
- `app/src/main/java/org/dhis2/utils/session/ChangeServerURLPresenter.kt`
- `app/src/main/java/org/dhis2/utils/session/ChangeServerUrlDialog.kt`
- `app/src/simprints/java/org/dhis2/data/user/GranularSyncModule.kt`
- `app/src/main/java/org/dhis2/data/service/SyncPresenterImpl.kt`
- `app/src/main/java/org/dhis2/utils/granularsync/SyncStatusDialog.kt`

Technical note:
- These differences are explicitly not part of the Simprints customization scope to preserve. During the merge with `develop-eyeseetea`, login/OpenID/2FA, notifications, change server URL, and granular sync flavor wiring should be removed rather than ported.

## 4. Shared drift still differing

Use this section only for temporary or still-unclassified differences.

Rules:
- every entry must include a short note explaining why it is still here
- this section must not remain open indefinitely after an upgrade is closed
- confirmed customizations must move to section 2
- absorbed or obsolete differences should be removed instead of living here forever

Current candidates:
- `app/build.gradle.kts` - includes Simprints flavor definition; likely valid flavor surface, but should be reconciled with the new baseline flavor blocks during merge
- `settings.gradle.kts` - differs heavily in this fork and will need classification against the EyeSeeTea local-SDK/composite-build baseline
- `gradle/libs.versions.toml` - likely mixes flavor release identity with technical dependency drift
- `app/src/main/java/org/dhis2/usescases/teiDashboard/DashboardViewModel.kt` (`_teiCanBeTransferred`/`checkIfTeiCanBeTransferred()`) - real, active Simprints customization (commit `7fe7f0f0d2`, not related to biometrics): caches `repository.teiCanBeTransferred()` in a `StateFlow`, populated once per `fetchDashboardModel()` call, so `checkIfTeiCanBeTransferred()` (called from `TeiDashboardMenu.kt`, inside composition) reads the cached value instead of hitting the database on every recomposition. Merged into this branch 2026-09-03 from `develop-simprints`, resolved as a merge conflict against baseline's `fetchDashboardModel()` rewrite (`CoroutineTracker.unconditionalIncrement/Decrement`, no `async`/`withContext(ui())`). Listed here rather than in section 2 because it doesn't fit any of the 13 existing customization categories (not biometrics-specific); worth deciding whether it deserves its own category or is minor enough to leave as a documented file-level note.

## 5. Notes

- This inventory reflects the current branch state only.
- The source of truth for functional titles is `openspec/specs/<capability>/spec.md`. Each spec starts with a `# <Title>` line; that `<Title>` is the exact string to use here as a section heading and in `// EyeSeeTea customization - [Title]` code comments.
- If code comments and functional titles diverge, prefer the title defined in the matching OpenSpec spec and update the code comment when possible.
