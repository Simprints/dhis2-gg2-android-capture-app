## 1. Metadata prerequisites (blocking, external)

- [x] 1.1 Create the biometric template tracked entity attribute (type `LONG_TEXT`) on the `Person` TET, in the same DHIS2 instance(s) used for development/testing, and record the assigned UID. Verify: the UID exists and is documented (e.g. in this change's notes) before task 2.1 starts. **Done**: `Biometrics Template` / `BiometricsTemplate`, UID `BP90qVFNazj`, `valueType: LONG_TEXT`, created on the `simprints-dev` instance (`172.16.0.99:8132/simprints-dev`).
- [x] 1.2 Link the new attribute as `programTrackedEntityAttribute` on `0.0 General Registration` and `1.6 Child Health`. Verify: `GET /api/programs/{programUid}.json?fields=programTrackedEntityAttributes[trackedEntityAttribute]` includes the new UID for both programs. **Done**: confirmed via API on both `Cej0ai1pPei` (0.0 General Registration) and `o5kwD20X1zR` (1.6 Child Health).
- [x] 1.3 Confirm the exact JSON shape for the two-template value with the PM (see design.md Open Questions) and record the agreed shape here or in design.md before task 2.1 starts. **Done**: captured from a real enrollment intent (the `subjectActions` extra, currently unparsed by the app) and recorded in design.md — `{"biometricReferences": [{"type": "FACE_REFERENCE", "format": "RANK_ONE_3_1", "templates": [{"template": "<base64>"}, ...]}]}`, persisted verbatim (no flattening), per-reference `id` dropped.

Commit: no code in this group — steps 1.1/1.2 are external metadata actions, 1.3 is a decision recorded in this change's docs. No commit expected from this group alone; fold the recorded decision into the commit for task 2.1 if it requires a spec/design edit.

## 2. Attribute UID wiring

- [x] 2.1 Add the new attribute UID constant (e.g. `biometricTemplateAttributeId`) to `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/biometrics/attributes.kt`, following the existing `biometricAttributeId`/`nhisNumberAttributeId` pattern. Verify: `./gradlew ktlintCheck` passes and the constant compiles. **Done**: `biometricTemplateAttributeId = "BP90qVFNazj"` added with its own `// EyeSeeTea customization - Biometrics Template Storage` comment; `commonMain` ktlint clean, `compileKotlinDesktop` succeeds.

Commit: standalone — this is metadata wiring with no behavior yet, not paired with a test.

## 3. Parse `subjectActions` and write the template attribute

- [x] 3.1 Add a behavior-level test for `BiometricsClient.handleRegisterResponse` asserting that, given an intent whose `subjectActions` extra matches the confirmed real payload shape (`schemaVersion`, `events[].payload.biometricReferences[]`), the resulting `SimprintsRegisteredItem` carries the parsed `biometricReferences` (`type`, `format`, `templates: [{template}]`); and that a missing/unparseable `subjectActions` extra yields an empty/null result rather than throwing. **Done**: 3 tests added in `BiometricsClientTest.kt` (present, absent, malformed JSON).
- [x] 3.2 Add the `biometricReferences` field to `SimprintsRegisteredItem` and the new DTOs for the nested shape, and parse the `subjectActions` extra in `handleRegisterResponse` (`BiometricsClient.kt`) to populate it, tolerating absence per 3.1. Verify: the test from 3.1 passes. **Done**: new `BiometricReference`/`BiometricTemplate` (domain) and `SubjectActionsSID`/`SubjectActionEventSID`/`SubjectActionPayloadSID`/`BiometricReferenceSID`/`BiometricTemplateSID` (SID DTOs) files; `parseBiometricReferences` helper in `BiometricsClient.kt`, tolerant of missing/malformed `subjectActions` (returns empty list, logs via Timber). All 24 tests pass.
- [x] 3.3 Add a behavior-level test asserting that writing a template value persists it under the new attribute UID for the given TEI, JSON-encoded exactly per the shape in design.md — assert on the persisted attribute value (SDK/repository boundary), not on which internal method was called. **Done**: `UpdateBiometricTemplateAttributeValueTest.kt`, D2 mocked with `RETURNS_DEEP_STUBS` (same pattern as `ValueStoreTest`), asserts the exact JSON written via `blockingSet`, plus a case for empty `biometricReferences` (no write).
- [x] 3.4 Implement `updateBiometricTemplateAttributeValue` (new file, shaped like `updateNHISNumberAttributeValue.kt`: direct `blockingSetCheck` write to `d2.trackedEntityModule().trackedEntityAttributeValues().value(attrUid, teiUid)`, serializing the parsed `biometricReferences` to JSON) to make the test pass. Verify: the test from 3.3 passes. **Done**: skips the write entirely when `biometricReferences` is empty. Full `app` test suite passes (982/982), ktlint clean. Reviewed with the `pilares` skill: both test files are correctly scoped at their real boundary (Simprints `Intent`, D2 SDK); applied two fixture-maintainability fixes — `givenASubjectActionsJson` now serializes the real `SubjectActionsSID` DTOs via `Gson()` instead of hand-built string interpolation, and the expected JSON in `UpdateBiometricTemplateAttributeValueTest` is generated from the same `BiometricReferencesValue` (made `internal`) via `Gson()` instead of a hardcoded string literal.

Commit: two commits — (3.1 + 3.2) for intent parsing, (3.3 + 3.4) for the write function — each its own red → green pair; they touch different files/concerns (`BiometricsClient.kt` parsing vs. a new SDK-write function).

## 4. Wire into `onBiometricsCompleted`

- [ ] 4.1 Add a behavior-level test covering `onBiometricsCompleted()` in `EnrollmentPresenterImpl` (currently uncovered beyond intent parsing) asserting the template attribute is written when `RegisterResult.Completed` carries a valid credential with a non-empty `biometricReferences`, and NOT written when the credential is invalid or `biometricReferences` is empty/null.
- [ ] 4.2 Call `updateBiometricTemplateAttributeValue` from `EnrollmentPresenterImpl.onBiometricsCompleted()` (`EnrollmentPresenterImpl.kt`), alongside the existing GUID/NHIS calls, guarded by the same `item.hasCredential && item.scannedCredential?.type != null` condition. Verify: the test from 4.1 passes.
- [ ] 4.3 Add the equivalent behavior-level test for `TEIDataPresenter.onBiometricsCompleted()`.
- [ ] 4.4 Call `updateBiometricTemplateAttributeValue` from `TEIDataPresenter.onBiometricsCompleted()` (`TEIDataPresenter.kt`), mirroring 4.2. Verify: the test from 4.3 passes.

Commit: two commits — (4.1 + 4.2) for `EnrollmentPresenterImpl`, (4.3 + 4.4) for `TEIDataPresenter` — each is its own red → green pair, split per presenter per the project's per-customization task-splitting rule.

## 5. Hide the attribute via program rule

- [x] 5.1 Design and create the DHIS2 program rule (metadata, external) that hides the new template attribute on `0.0 General Registration` and `1.6 Child Health`, per PM guidance to avoid a hardcoded type-check filter. Verify: opening the enrollment/TEI form in the app does not show the template field, with no corresponding hardcoded filter added to `BiometricsAttributeUiModelImpl` or similar. **Done**: `Hide Biometrics Template in General Registration` (`cqYE1buKkxg`) and `Hide Biometrics Template in Child Health` (`W9yagzFawV1`), both `condition: "true"` + `HIDEFIELD` on `BP90qVFNazj`, created in `simprints-dev`. App-side verification (no form field shown) still to be done once 2.1 wires the UID constant.

Commit: no app code — metadata-only; if any incidental code change is needed to support the rule (unexpected), it becomes its own commit.

## 6. Manual validation

- [ ] 6.1 Add an entry to `eyeseetea-docs/upgrade/simprints/upgrade-validation-checklist.md` covering: registering a new TEI with biometrics and confirming the template attribute is populated with both templates; performing "register last" after a biometric search and confirming the same; confirming the field is hidden in both the enrollment form and the TEI dashboard form.

Commit: standalone docs commit.
