# Simprints validation checklist

Use this file as the manual validation checklist for the Simprints fork.

## Purpose

This file is for:
- minimal manual validation per customization
- expected result per flow
- regression checking after merge resolution

This file is not for:
- merge progress
- implementation details
- raw diff tracking
- file-level inventories

## Validation flows

### 1. Biometric Search Integration

Preconditions:
- Simprints fixture returns one or more identification candidates for the tested person.

Manual flow:
1. Launch biometric search from the search screen.
2. Confirm the search list is hidden and a loader is shown while the Simprints app is being launched.
3. Complete the external Simprints identify flow and return to DHIS2.

Expected result:
- Returned Simprints GUIDs are reused as biometrics attribute values in a DHIS2 search.
- The search workflow continues with normal DHIS2 search results, not a disconnected local list.

### 2. Biometrics Configuration Selection Per Program Or Org Unit Group

Preconditions:
- User has access to at least one program-specific config, one org-unit-group config, and one `default` config.
- Test user belongs to the capture org unit groups expected by the fixture.

Manual flow:
1. Log in and complete sync so biometrics configs are downloaded.
2. Open a program with an explicit `program` match.
3. Confirm the effective biometrics behavior for that program.
4. Open a second program without direct match but with matching capture `orgUnitGroup`.
5. Open a third program that only falls back to `default`.

Expected result:
- The active config is selected in precedence order `program -> orgUnitGroup -> default`.
- Downstream biometrics behavior changes with the selected program instead of staying application-global.

### 3. Biometrics Mode Controls Per Program

Preconditions:
- Test data exists for three programs or fixtures that expose `full`, `limited`, and `zero` modes.
- A TEI with existing biometrics and a TEI without biometrics are both available.

Manual flow:
1. Enter a `full` program and open search, enrollment, and TEI dashboard flows.
2. Enter a `limited` program and repeat the same checks.
3. Enter a `zero` program and repeat the same checks.

Expected result:
- `full` keeps biometric search and registration paths available.
- `limited` keeps only the allowed existing-biometrics behavior and suppresses new registration UI where expected.
- `zero` hides biometric UI and biometric-driven actions, including dashboard biometrics cards.

### 4. Age Threshold Controls For Biometrics

Preconditions:
- Active config defines `ageThresholdMonths` and `dateOfBirthAttribute`.
- There is one TEI below threshold and one TEI above threshold based on that configured DOB attribute.

Manual flow:
1. Search using attributes for the below-threshold TEI.
2. Open the below-threshold TEI in enrollment and dashboard contexts.
3. Repeat the same checks with the above-threshold TEI.

Expected result:
- Below threshold, the search flow skips biometric next-action behavior.
- Below threshold, enrollment and dashboard do not expose biometric actions.
- Missing biometric data for below-threshold TEIs is represented as `Not Applicable` rather than a negative/failure state.

### 5. Configurable Date Of Birth Attribute For Biometrics

Preconditions:
- Two biometrics configurations exist, each pointing `dateOfBirthAttribute` at a different tracked entity attribute/form field.

Manual flow:
1. Run an age-threshold check (search, enrollment, dashboard) under the first configuration.
2. Switch to the program/org-unit-group using the second configuration and repeat.
3. Test a TEI where the configured attribute is missing or empty.

Expected result:
- Age is computed from the attribute configured for the active configuration in each context.
- A missing/empty configured attribute falls back to `0` months.

### 6. Confidence Score Filtering For Simprints Matches

Preconditions:
- Active config has a non-zero `confidenceScoreFilter`.
- Simprints fixture can return low-confidence matches both with and without linked credential data.

Manual flow:
1. Run biometric identify with candidates below the threshold and no credentials.
2. Run biometric identify with at least one below-threshold candidate that includes credential linkage.
3. If available, run dashboard verification with a result below threshold, and one with confidence band `NONE`.

Expected result:
- Non-credential low-confidence identify matches are filtered out.
- Credential-linked identify matches remain visible even below threshold.
- Verification results below threshold, and results with band `NONE`, are treated as `NoMatch`.

### 7. Org Unit Derived Module Id For Simprints

Preconditions:
- Active config defines `orgUnitLevelAsModuleId`.
- Test data covers one single-org-unit flow and one multi-org-unit identify flow.

Manual flow:
1. Trigger enrollment or dashboard biometrics from a TEI with known org unit path.
2. Trigger biometric identify for a user with multiple capture org units.

Expected result:
- Single-org-unit flows derive `moduleId` from the configured path offset, with the documented root and level-4 fallback behavior.
- Multi-org-unit identify derives one shared level-4 parent when possible, otherwise it falls back to the default module id.

### 8. Relationship Search Identification Toggle By TE Type

Preconditions:
- Active config defines `enableIdentificationForTET` for one specific tracked entity type.

Manual flow:
1. From a relationship-driven search whose TE type matches `enableIdentificationForTET`, confirm biometric identification is available regardless of `biometricsMode`.
2. From a relationship-driven search whose TE type does not match, confirm biometric identification is not available.

Expected result:
- Relationship search biometric availability follows the TE type toggle, not the normal per-program mode rule.

### 9. Biometric Duplicate Review And Confirm Identity

Preconditions:
- Simprints fixture returns one or more identification candidates for the tested person.
- At least one candidate maps to an existing DHIS2 TEI through the biometrics attribute.

Manual flow:
1. Launch biometric search and complete identification with duplicate candidates returned.
2. Review the duplicate candidates shown by DHIS2, including a credential-linked but low-confidence candidate.
3. Confirm identity on one candidate, both manually and via the automatic path if available.
4. From enrollment, trigger a duplicate outcome and choose both "open existing TEI" and `registerLast`.

Expected result:
- Duplicate review is backed by normal DHIS2 search results, not a disconnected local list.
- Credential-linked candidates remain visible even below the confidence threshold.
- Confirm identity returns to the correct DHIS2 continuation path and resets sequential biometric search state.
- Enrollment duplicates allow both opening the existing TEI dashboard and continuing with `registerLast`.

### 10. Biometrics In TEI Cards, TEI Dashboard, Enrollment, And TEI Form

Preconditions:
- One TEI is available for enrollment-form checks and dashboard checks, with first-name/last-name and biometric/NHIS attributes populated.
- The active config enables biometrics for the tested program, in each of `full`, `limited`, and `zero` modes.

Manual flow:
1. Inspect search cards for avatar initials/title derived from name attributes, and biometric/NHIS rows staying visible when other empty attributes are hidden.
2. Open enrollment or TEI form for the TEI and inspect available biometrics actions in `full` mode, then `limited`/`zero`.
3. Attempt the registration flow from enrollment.
4. Open the same TEI in dashboard and inspect available biometrics actions per mode.
5. Attempt dashboard verification.
6. Check landscape dashboard mode for the embedded form.

Expected result:
- Search cards and result details reflect the configured name/biometric/NHIS attribute rules.
- Enrollment and TEI form support registration, duplicate handling, and `registerLast`, and filter biometric fields out of the form in non-`full` modes.
- Enrollment and TEI form do not expose a form-driven verification action.
- TEI dashboard supports both registration and verification and uses verification state to drive dashboard card state; `zero` mode hides the biometrics card; `limited` mode hides registration when no biometric value exists.

### 11. Biometric Verification Persistence

Preconditions:
- Active config defines `lastVerificationDuration`.
- Simprints fixture can produce one successful verification outcome.

Manual flow:
1. Complete a successful biometric verification from the TEI dashboard.
2. Confirm the configured biometrics attribute is updated with the returned value.
3. Re-open the same TEI before the verification window expires and confirm the state is read as active.

Expected result:
- Recent successful verification is persisted per TEI and remains active until its configured duration expires.
- Verification is only offered from the TEI dashboard, not from the enrollment form.

### 12. Time-Based Verification And Registration Failure Windows

Preconditions:
- Active config defines `lastVerificationDuration` and `lastDeclinedEnrolDuration`.
- Simprints fixture can produce one successful verification and one declined or failed registration outcome.

Manual flow:
1. Complete a successful biometric verification from the TEI dashboard and revisit after the verification window expires.
2. Trigger a declined or failed registration outcome in enrollment and in the TEI dashboard.
3. Revisit the affected screen after the configured decline window passes.

Expected result:
- Verification older than `lastVerificationDuration` is discarded and no longer treated as an active match.
- Declined or failed registration state is visible temporarily and then clears automatically after `lastDeclinedEnrolDuration`.

### 13. Simprints Data Exchange And Mapping

Preconditions:
- Active biometrics preferences are selected for the tested program.
- A scenario where biometrics preferences are missing can also be forced (e.g. before any config selection).

Manual flow:
1. Trigger identification, registration, verification, and `registerLast` and inspect the outbound metadata payload (`forkVersion`, `trackedEntityInstanceId`, `enrollingOrgUnitId`, `enrollingOrgUnitName`, `userOrgUnits`, `subjectAge`).
2. Confirm the client is built from the selected `projectId`, current username, and `confidenceScoreFilter`.
3. Return a response with scanned credential data and confirm it propagates back into TEI-side credential state.
4. Force the missing-preferences fallback path and confirm the client still boots with the hardcoded defaults.

Expected result:
- Every outbound call includes the expected metadata payload.
- Responses (registration, identify, verify, confirm-identity) are converted into DHIS2-side result models, including credential data when present.
- The missing-preferences fallback uses the hardcoded default `projectId`, username `admin`, and confidence threshold `0`.

### 14. Areas Explicitly Out Of Scope For Preservation

Preconditions:
- None — this is a negative check performed after merging `develop-eyeseetea`.

Manual flow:
1. After the upgrade merge, check that 2FA/login changes, notifications, change server URL, and granular sync flavor wiring match the `develop-eyeseetea` baseline behavior, not any prior Simprints-specific variant.

Expected result:
- No Simprints-specific behavior survives in these areas; they match upstream/baseline behavior exactly.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `customization-files.md`
