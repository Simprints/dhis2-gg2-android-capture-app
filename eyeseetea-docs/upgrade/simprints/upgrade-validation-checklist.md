# Simprints validation checklist

Use this file as the manual validation checklist for the Simprints fork.

## Purpose

This file is for:
- minimal manual validation per customization
- expected result per flow
- regression checking after merge resolution
- the validation status of the upgrade in progress (the table below; reset it at the start of each upgrade)

This file is not for:
- merge progress
- implementation details
- raw diff tracking
- file-level inventories

## Status: 3.5.0 upgrade

Build under test: `3.5.0-simprints-fork-1` debug APK (built 2026-11-05 from the uncommitted merge state), Lenovo TB-X304F tablet, `simprints-dev` server. Biometric config for `1.6 Child Health` (`o5kwD20X1zR`): `full` for 2026-11-09, changed to `limited` and then `zero` for 2026-11-10 (data store `simprints/config`, restored afterwards). The Simprints app itself was not used on 2026-11-10; the flows that need it were tested by the user on 2026-11-09.

Legend: **Verified** = every manual step of the flow was run. **Partial** = only the steps listed. **Blocked** = cannot be completed because of an external dependency. **Not tested**.

| # | Flow | Status | What was verified | Still pending |
|---|------|--------|-------------------|---------------|
| 1 | Biometric search | Verified | 2026-11-09 (user): biometric search from the search card → no match → attribute search → no match → new patient with `registerLast`; and biometric search → auto-navigation to the TEI dashboard. Both worked (steps 1-3). | Marked verified by the user on 2026-11-13 without running the steps that were still pending: Step 4 (lifecycle drop while Simprints is in the foreground) and step 5 (search by GUID as text). |
| 2 | Config selection | Verified | 2026-11-10: the program-level entry for Child Health is applied (behavior followed its `limited` / `zero` value after a config sync). 2026-11-13 (user): the other selections work too (`orgUnitGroup`, `default`). `GetBiometricsConfig.kt` is unchanged against `develop-simprints` apart from formatting. | |
| 3 | Mode controls | Verified | 2026-11-10, Child Health in `limited` and `zero`, portrait and landscape. Search: no biometric search in either mode. Enrollment: no biometric field in either mode. Dashboard: `limited` shows **Verify** for a TEI with biometrics and no registration; `zero` shows no biometrics card; a TEI without biometrics shows none in both. The "Biometrics" row on cards and dashboard header stays visible in `zero`: same as earlier versions, not an error (user, 2026-11-10). | Marked verified by the user on 2026-11-13 without running the steps that were still pending: `full` mode across enrollment and dashboard in this build, and `limited` registration from the dashboard for a TEI without biometric value on a program that has one. |
| 4 | Age threshold | Verified | 2026-11-13: the biometric button is not enabled for a TEI below 6 months; `AgeInMonthsKt` logs `Age in months: 426` (and 243, 269, 316, 488 for other TEIs), derived from the configured DOB attribute. The age is not sent in the identify `metadata`. Accepted as tested by the user. | |
| 5 | DOB attribute | Verified | 2026-11-13 (user): works. The age comes from the DOB attribute of the active config (`AgeInMonthsKt` logs `Age in months: 426`). `AgeInMonths.kt` is unchanged against `develop-simprints` apart from ktlint formatting. Not run with two configs that have different DOB attributes. | |
| 6 | Confidence filter | Verified | 2026-11-13 (user): the filter works on the tablet (`confidenceScoreFilter: 55` in the log). The filter logic in `BiometricsClient` is unchanged against `develop-simprints` (`git diff -w`: only ktlint formatting and the `selectedGuidOrNone` patch). | |
| 7 | Org unit module id | Verified | 2026-11-13: identify logs `moduleId: A1E2YiXxdKR` (two searches, 00:42:28 and 00:45:07). Not yet compared with the org unit of the session. | Marked verified by the user on 2026-11-13 without running the steps that were still pending: Compare the module id with the expected org unit; multi org unit fallback to the default module id. |
| 8 | Relationship search toggle | Verified | Step 1 verified on device 2026-09-03 (3.4 build). 2026-11-13 (user): verified again on 3.5.0. | |
| 9 | Duplicate review and confirm identity | Verified | 2026-11-12 (user): confirm identity worked on the auto-navigation path (step 4, automatic), and also from the confirmation dialog when searching by biometrics only, from a search without a card (step 4, manual). The auto-navigation of flow 1 (`isVerified=true`) also ran. Identify with 2 candidates (two people enrolled with the same face): the confirmation dialog showed both, each with its own properties (user, 2026-11-12). 2026-11-13 (user): the biometric row of the confirmation dialog is OK (step 3). 2026-11-13 (user): the manual-confirm cases of step 6 work. | Marked verified by the user on 2026-11-13 without running the steps that were still pending: Step 5 (duplicate outcome from enrollment) is **blocked**: registering the second person with the same face did not show the duplicates dialog, so duplicate detection at registration does not seem enabled in the Simprints project. |
| 10 | Cards, dashboard, enrollment, TEI form | Verified | 2026-11-09 (user): identify from the TEI dashboard worked; `registerLast` worked once. 2026-11-10: cards, enrollment and dashboard per mode (see flow 3). | Marked verified by the user on 2026-11-13 without running the steps that were still pending: Step 7 twice, pressing save right after the capture (the B6 hang). Step 6: embedded form in landscape (hidden by design, see `refactors-pending.md`). Landscape rotation with the dashboard open (task 8.4). |
| 11 | Verification persistence | Verified | 2026-11-09 (user): verification from the dashboard worked. 2026-11-13 (user): verification persistence tested, including the expiry of the verification window (flow 12). | |
| 12 | Time windows | Verified | 2026-11-13 (user): verification expiry (`lastVerificationDuration`) and the declined/failed registration window (`lastDeclinedEnrolDuration`) both work. | |
| 13 | Data exchange and mapping | Verified | The `userNotFound` identify path from the attribute search (step 4) ran and led to `registerLast`. 2026-11-13, logcat (`BiometricsClient`): step 1 outbound metadata checked for identify (`forkVersion`, `userOrgUnits`), `registerLast` (04:20:59, all six fields; `subjectAge` 59, confirmed by the user to be a `registerLast`) and verify at 04:31:00 (`trackedEntityInstanceId` `BOaSt6XwrHi`, `enrollingOrgUnitId` `BSMNCApHj6P`, `enrollingOrgUnitName` `Anto CHPS`, `userOrgUnits`, `subjectAge` 426, matching `Age in months: 426`, `moduleId` `A1E2YiXxdKR`). Step 2: client built with `projectId` `q3g2oCFr45hdfflPwF4M`, `userId` `admin2`, `confidenceScoreFilter` 55. Step 3: identify responses carry `scannedCredential` (NHISCard `22233311`) at 00:45:45 and 04:19:44; the TEI stores only the NHIS number (not the whole credential), and it is stored (user, 2026-11-13). | Marked verified by the user on 2026-11-13 without running the steps that were still pending: The missing-preferences fallback (step 5). |
| 14 | Config sync on login | Verified | 2026-11-10: login triggered `SyncBiometricsConfig` and stored the 6 configs (log `BiometricsConfigRepositoryImpl$sync`, 06:08:46). A config sync from Settings also fetched a changed config (06:50:06). | |
| 15 | Template storage | Blocked | 2026-11-10, `registerLast` with a scanned credential, two TEIs: `marga perez` (`BOaSt6XwrHi`, registered 2026-11-09) and `Vza3iz3WbGm` (registered 2026-11-10). Both have the `Biometrics` GUID and **no `Biometrics Template`**: checked on the server API for the first, and in the tablet database for both. Cause found in the logcat: the Simprints app (`2026.3.1+175.1`) answers the registration with `scannedCredential`, `hasCredential`, `biometricsComplete`, `sessionId`, `deviceId` and `enrolment.guid`, but **no `subjectActions`**, so `parseBiometricReferences()` returns an empty list and `updateBiometricTemplateAttributeValue()` returns without writing. Not a fork regression: the code matches `develop-simprints@09cad0d3e`. Server side is fine: the attribute is linked to the program and the September TEIs hold a valid value (1 `FACE_REFERENCE`, 2 templates). | Needs a Simprints app version that returns `subjectActions` on registration (precondition of this flow). Then steps 1 to 4. Plain enrollment and dashboard registration were not observed. Steps 3 and 6 (the value survives saving, `confirmIdentify` does not touch it) can be run now on a TEI that already has a template, for example the September ones. |

Findings from this round:
- `connectedSimprintsDebugAndroidTest` was not run in 3.5.0 (task 8.3, user decision 2026-11-13). It was blocked by a Room identity-hash mismatch in 3.4.1 / 3.4.2, root cause not found: carried forward as a known issue.
- Landscape TEI dashboard shows a duplicated panel when rotated with the dashboard open (inherited from Oslo, `tasks.md` 8.4).
- The embedded form is hidden in the landscape dashboard by design (`refactors-pending.md`).

## Validation flows

### 1. Biometric Search Integration

Preconditions:
- Simprints fixture returns one or more identification candidates for the tested person.

Manual flow:
1. Launch biometric search from the search screen.
2. Confirm the search list is hidden and a loader is shown while the Simprints app is being launched.
3. Complete the external Simprints identify flow and return to DHIS2.
4. Repeat the launch, but drop the app below `STARTED` while Simprints is foregrounded (e.g. lock the screen or switch apps) before returning, and confirm the previous (stale) program results are not shown once the search list reappears.
5. Note the GUID of a TEI found via biometric search, clear the search, and manually enter that same GUID as a text value into the biometrics attribute's search field.

Expected result:
- Returned Simprints GUIDs are reused as biometrics attribute values in a DHIS2 search.
- The search workflow continues with normal DHIS2 search results, not a disconnected local list.
- Stale program results from before the biometric app launch are never shown on return, including after a lifecycle drop while Simprints is in the foreground.
- Manually searching by that same GUID as text returns the identical TEI, confirming the match comes from the normal DHIS2 search index on the biometrics attribute, not a fork-only in-memory store.

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
6. Force a fixture with no `default` config present (e.g. remove/misconfigure it server-side before sync) and open any program that would fall through to `default`.

Expected result:
- The active config is selected in precedence order `program -> orgUnitGroup -> default`.
- Downstream biometrics behavior changes with the selected program instead of staying application-global.
- With no `default` config present, the program still opens normally (`SelectBiometricsConfig`/`ProgramViewModel.onItemClick` never blocks navigation), but biometrics config selection fails silently in the background (`SelectBiometricsConfig.getDefaultConfig()` throws, uncaught) and biometrics behavior for that program stays without an active config until metadata is re-synced with a valid `default` configured on the server.

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
1. From a relationship-driven search whose TE type matches `enableIdentificationForTET`, confirm biometric identification is available regardless of `biometricsMode`. — [x] Verified on device 2026-09-03 (created a relationship, biometric search available and worked)
2. From a relationship-driven search whose TE type does not match, confirm biometric identification is not available. — [ ] Not yet verified
3. From a normal (non-relationship) search on the tracked entity type configured in `enableIdentificationForTET`, with `biometricsMode` set to `limited` or `zero` for that program, confirm biometric identification is not available. — [ ] Not yet verified

Expected result:
- Relationship search biometric availability follows the TE type toggle, not the normal per-program mode rule.
- Normal (non-relationship) search availability always follows `biometricsMode == full`, even when the current TE type matches `enableIdentificationForTET` — the TE type toggle never leaks into the normal search entry point.

### 9. Biometric Duplicate Review And Confirm Identity

Preconditions:
- Simprints fixture returns one or more identification candidates for the tested person.
- At least one candidate maps to an existing DHIS2 TEI through the biometrics attribute.

Manual flow:
1. Launch biometric search and complete identification with duplicate candidates returned.
2. Review the duplicate candidates shown by DHIS2, including a credential-linked but low-confidence candidate.
3. Inspect the confirmation dialog card for a candidate whose biometric attribute would otherwise be empty and confirm the biometric row still appears with its distinct icon/marker, instead of being hidden like other empty attributes.
4. Confirm identity on one candidate, both manually and via the automatic path if available.
5. From enrollment, trigger a duplicate outcome and choose both "open existing TEI" and `registerLast`.
6. Using a SID version that does not auto-navigate (no `isVerified=true`, e.g. `2026.2.1+167.1`), and a TEI whose previous verification has expired (do not identify right after registering, since registration stores a still-valid verification), manually confirm identity and open the TEI dashboard for each case:
   - Linked credential + confidence at or above the threshold.
   - No credential + confidence at or above the threshold.
   - Linked credential + confidence below the threshold.
   - No credential + confidence below the threshold.

Expected result:
- Duplicate review is backed by normal DHIS2 search results, not a disconnected local list.
- The confirmation dialog shows Simprints-aware display data: the biometric attribute row stays visible (with its icon) even when empty, unlike other empty attributes which are hidden.
- Credential-linked candidates remain visible even below the confidence threshold.
- Confirm identity returns to the correct DHIS2 continuation path and resets sequential biometric search state.
- Enrollment duplicates allow both opening the existing TEI dashboard and continuing with `registerLast`.
- Manual confirm identity with confidence at or above the threshold shows `Biometrics verified` on the dashboard, whether or not the candidate is linked to a credential.
- A credential-linked candidate below the threshold stays visible and shows `Verification Failed` after manual confirm; a candidate without a credential below the threshold is not shown.
- Confirm identity on a TEI whose biometrics attribute holds a `BIOMETRICS_SEARCH_` / failure placeholder or no value sends `none_selected` to Simprints, never the placeholder (unit-tested in `BiometricsClientTest`; not yet reproduced on device, see `refactors-pending.md`).

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
7. Attempt `registerLast` ("Use Last Biometrics Now") twice, from a new TEI enrollment, with all mandatory attributes already filled from the search screen so the form needs no further input — press save immediately after biometric capture completes, with no other field edited first.

Expected result:
- Search cards and result details reflect the configured name/biometric/NHIS attribute rules.
- Enrollment and TEI form support registration, duplicate handling, and `registerLast`, and filter biometric fields out of the form in non-`full` modes.
- Enrollment and TEI form do not expose a form-driven verification action.
- Step 7's `registerLast` saves and navigates to the TEI dashboard within a few seconds of biometric capture completing, both times — this is the exact scenario that reproduced a save/navigation hang (2026-09-04, see `customization-files.md` §2.10 and `upgrade-3.4-notes.md` B6).
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
3. Return a response with scanned credential data on a successful identify and confirm it propagates back into TEI-side credential state.
4. Return an identify response where no candidate is found (`userNotFound`) but scanned credential data is present, from a demographic (attribute) search entry point, and confirm the confirm-identity call still carries that credential.
5. Force the missing-preferences fallback path and confirm the client still boots with the hardcoded defaults.

Expected result:
- Every outbound call includes the expected metadata payload.
- Responses (registration, identify, verify, confirm-identity) are converted into DHIS2-side result models, including credential data when present.
- Scanned credential data is preserved on the `userNotFound` identify outcome, not only on a successful match.
- The missing-preferences fallback uses the hardcoded default `projectId`, username `admin`, and confidence threshold `0`.

### 14. Biometrics Config Sync On Login

Preconditions:
- None.

Manual flow:
1. Log in and confirm login triggers a biometrics config sync (`SyncBiometricsConfig`, wired from `LoginActivity.kt`/`LoginModule.kt`) — the one Simprints customization inside `login/`, everything else in that area is upstream/baseline behavior with no fork-specific preservation to check.

Expected result:
- Biometrics config sync on login still runs and updates the active biometrics configuration.

Note (2026-09-03): this flow used to be a broader "check 2FA/login/notifications/Change Server URL/granular sync match baseline" negative check. Narrowed to just the one real customization worth a manual check — Change Server URL no longer exists anywhere in the codebase, and granular sync flavor wiring (`app/src/simprints/java/org/dhis2/utils/granularsync/GranularSyncModule.kt`) is active Simprints DI wiring documented under its own customization, not an out-of-scope area. See `customization-files.md` §3 for the corrected inventory.

### 15. Biometrics Template Storage

Preconditions:
- The `Biometrics Template` tracked entity attribute (`BP90qVFNazj`) exists on the instance and is linked wherever the `Biometrics` attribute is linked: the `Person` TET and every program that carries `Biometrics` (on `simprints-dev`, `0.0 General Registration` and `1.6 Child Health`). On a new instance, set it up with `eyeseetea-docs/scripts/simprints/replicate-biometrics-template.ts`: run `--mode plan`, review the programs it leaves out, then `--mode validate` and `--mode commit` (see `customization-files.md` §2.14).
- The attribute is hidden by code (`EnrollmentPresenterImpl.onFieldsLoading()`), not by a program rule — a `HIDEFIELD` program rule was tried first and reverted because the rule engine blanks the field's value on every form save, wiping the value this feature writes. There is no program rule for this attribute; if one exists on the instance, it is stale and should be removed.
- Simprints fixture/device enrollment returns a `subjectActions` extra containing at least one `FACE_REFERENCE` with two templates (the default capture count).

Manual flow:
1. Register a new TEI (no prior biometric search) and complete biometric enrollment.
2. Inspect the `Biometrics Template` attribute value for that TEI via the API (`/api/tracker/trackedEntities/{uid}?fields=attributes`) and confirm it holds a JSON payload with `biometricReferences[0].templates` containing two entries.
3. Complete/save the enrollment form (not just reach the point where the template is written) and re-inspect the same attribute value — confirm it is still present and unchanged, not blanked out.
4. Perform a biometric search, choose "New person"/"New patient", and confirm "use last biometrics" (`registerLast`). Repeat steps 2-3 for the newly created TEI.
5. Open the enrollment form and the TEI dashboard form for a TEI with a Person tracked entity type and confirm the `Biometrics Template` field is not shown as an editable or visible field in either screen.
6. Select an existing TEI from biometric search results and confirm identity without registering a new TEI (`confirmIdentify`); confirm the `Biometrics Template` attribute value for that TEI is unchanged afterwards.

Expected result:
- Both registration flows (`register`/plain enrollment and `registerLast`) persist both captured templates in a single `Biometrics Template` attribute value, JSON-encoded as `{"biometricReferences": [{"type", "format", "templates": [{"template"}, ...]}]}`.
- The attribute is hidden from manual data entry in both the enrollment form and the TEI dashboard form, via a code-level filter — no program rule and no hardcoded type-check filter shared with the GUID attribute are involved.
- The written value survives completing/saving the enrollment form — it is not blanked out afterward.
- Confirming identity on an existing TEI (`confirmIdentify`) never writes or updates the `Biometrics Template` attribute — this flow is explicitly out of scope for this customization.

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `openspec/specs/<capability>/spec.md` (SHALL/MUST + WHEN/THEN scenarios)
- keep its technical inventory in `customization-files.md`
