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

### 1. Program config selection precedence

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

### 2. Biometrics mode `full`, `limited`, and `zero`

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

### 3. Age threshold and configured date-of-birth attribute

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

### 4. Biometric identify, duplicate review, and confirm identity

Preconditions:
- Simprints fixture returns one or more identification candidates for the tested person.
- At least one candidate maps to an existing DHIS2 TEI through the biometrics attribute.

Manual flow:
1. Launch biometric search from the search screen.
2. Complete the external Simprints identify flow and return to DHIS2.
3. Review the duplicate candidates shown by DHIS2.
4. Confirm identity on one candidate.

Expected result:
- Returned Simprints GUIDs are reused as biometrics attribute values in a DHIS2 search.
- Duplicate review is backed by normal DHIS2 search results, not a disconnected local list.
- Confirm identity returns to the correct DHIS2 continuation path and resets sequential biometric search state.

### 5. Credential-preserving confidence filter

Preconditions:
- Active config has a non-zero `confidenceScoreFilter`.
- Simprints fixture can return low-confidence matches both with and without linked credential data.

Manual flow:
1. Run biometric identify with candidates below the threshold and no credentials.
2. Run biometric identify with at least one below-threshold candidate that includes credential linkage.
3. If available, run dashboard verification with a result below threshold.

Expected result:
- Non-credential low-confidence identify matches are filtered out.
- Credential-linked identify matches remain visible even below threshold.
- Verification results below threshold are treated as `NoMatch`.

### 6. Module id derivation from org unit context

Preconditions:
- Active config defines `orgUnitLevelAsModuleId`.
- Test data covers one single-org-unit flow and one multi-org-unit identify flow.

Manual flow:
1. Trigger enrollment or dashboard biometrics from a TEI with known org unit path.
2. Trigger biometric identify for a user with multiple capture org units.

Expected result:
- Single-org-unit flows derive `moduleId` from the configured path offset, with the documented root and level-4 fallback behavior.
- Multi-org-unit identify derives one shared level-4 parent when possible, otherwise it falls back to the default module id.

### 7. Enrollment versus TEI dashboard behavior

Preconditions:
- One TEI is available for enrollment-form checks and dashboard checks.
- The active config enables biometrics for the tested program.

Manual flow:
1. Open enrollment or TEI form for the TEI and inspect available biometrics actions.
2. Attempt the registration flow from enrollment.
3. Open the same TEI in dashboard and inspect available biometrics actions.
4. Attempt dashboard verification.

Expected result:
- Enrollment and TEI form support registration, duplicate handling, and `registerLast`.
- Enrollment and TEI form do not expose a form-driven verification action.
- TEI dashboard supports both registration and verification and uses verification state to drive dashboard card state.

### 8. Verification persistence and declined registration timeout

Preconditions:
- Active config defines `lastVerificationDuration` and `lastDeclinedEnrolDuration`.
- Simprints fixture can produce one successful verification and one declined or failed registration outcome.

Manual flow:
1. Complete a successful biometric verification from the TEI dashboard.
2. Re-open the same TEI before the verification window expires.
3. Trigger a declined or failed registration outcome.
4. Revisit the affected screen after the configured decline window passes.

Expected result:
- Recent successful verification is persisted and remains active until its configured duration expires.
- Declined or failed registration state is visible temporarily and then clears automatically after `lastDeclinedEnrolDuration`.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `customization-files.md`
