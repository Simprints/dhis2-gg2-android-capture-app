# Simprints customization specs

Use this file as the narrative functional draft for the Simprints fork during onboarding.

## 1. Biometric Search Integration

Status:
- `active`

Functional intent:
- Allow TEI search to launch the external Simprints biometric flow and continue the DHIS2 search process with the returned Simprints data.

Expected behavior:
- From the search screen, the app can launch the Simprints biometric app with module and org unit context.
- While the biometric app is being launched, the search list can be hidden and a dedicated loader shown to avoid confusing intermediate UI states.
- When Simprints returns identification results, the app converts them into a biometric search in DHIS2 and continues the search workflow with those returned identifiers.
- The returned Simprints candidate GUIDs are treated as values of the configured biometrics tracked entity attribute, so duplicate review and follow-up TEI search run through normal DHIS2 search infrastructure instead of a separate local cache.

## 2. Biometrics Configuration Selection Per Program Or Org Unit Group

Status:
- `active`

Functional intent:
- Allow the app to download multiple Simprints biometrics configurations and activate the one that matches the current program context or the user's capture org unit groups.

Expected behavior:
- During login or synchronization, the app downloads the list of Simprints biometrics configurations from the server and stores them locally.
- When the user enters a program, the app selects one active biometrics configuration for that program.
- Selection precedence is:
  - configuration with matching `program`
  - otherwise configuration matching one of the user's capture-scope `orgUnitGroup` values
  - otherwise configuration whose `orgUnitGroup` is `default`
- A `default` configuration is mandatory; if no default configuration exists, selection fails instead of silently guessing one.
- The selected configuration becomes the active biometrics configuration used by the app for later flows.
- The active configuration controls values such as `projectId`, `biometricsMode`, `icon`, `confidenceScoreFilter`, `ageThresholdMonths`, `dateOfBirthAttribute`, `orgUnitLevelAsModuleId`, `lastVerificationDuration`, `lastDeclinedEnrolDuration`, and `enableIdentificationForTET`.
- Program navigation applies the selection immediately before opening the target program screen, so downstream search, enrollment, and dashboard logic use the matching configuration for that program.
- Once selected, the active configuration is flattened into preferences so later flows read one effective biometrics configuration rather than resolving the full config list again.

## 3. Biometrics Mode Controls Per Program

Status:
- `active`

Functional intent:
- Allow each program to enable, limit, or disable biometric behavior through the selected Simprints configuration.

Expected behavior:
- `full` mode enables biometric search in the normal search flow and keeps biometric registration available in enrollment and dashboard flows.
- `limited` mode keeps Simprints-related verification behavior but suppresses biometric registration UI where the TEI does not already have biometric data.
- `zero` mode disables biometric UI and biometric-driven actions for the program.
- The selected mode is read from the active program configuration, so entering a different program can change biometric behavior without changing application-wide settings.

## 4. Age Threshold Controls For Biometrics

Status:
- `active`

Functional intent:
- Prevent biometric registration or biometric-driven flows from applying to TEIs and searches that fall below the configured age threshold.

Expected behavior:
- The app uses the configured `dateOfBirthAttribute` to locate the TEI or form field that represents date of birth for biometrics decisions.
- The app derives age in months from the configured date-of-birth attribute and compares it against `ageThresholdMonths` from the active biometrics configuration.
- In search, when the user performs an attribute search that includes a date of birth below the threshold, the next sequential action skips biometric search and goes directly to the non-biometric continuation path.
- In enrollment and TEI dashboard flows, TEIs below the threshold do not expose biometric registration/verification actions.
- In search and dashboard cards, biometric status is still represented, but missing biometric data for TEIs under the threshold is shown as `Not Applicable` instead of a failure state.

## 5. Configurable Date Of Birth Attribute For Biometrics

Status:
- `active`

Functional intent:
- Let each Simprints biometrics configuration define which tracked entity attribute should be treated as the date of birth when computing age-related biometrics behavior.

Expected behavior:
- The selected biometrics configuration persists `dateOfBirthAttribute` as part of the active program/org-unit biometrics settings.
- Search age checks read the date of birth from the configured attribute key in the query data.
- Enrollment age checks read the date of birth from the configured field UID in the loaded form fields.
- TEI and dashboard age checks read the date of birth from the configured tracked entity attribute in stored TEI attribute values.
- If the configured attribute is missing or empty, age-based biometric logic falls back to `0` months, which effectively treats the TEI as below the threshold unless the threshold is also `0`.

## 6. Confidence Score Filtering For Simprints Matches

Status:
- `active`

Functional intent:
- Exclude low-confidence biometric matches from Simprints results unless the result is linked to a credential that must still be shown.

Expected behavior:
- The selected biometrics configuration persists `confidenceScoreFilter` as part of the active Simprints settings.
- In identification flows, candidate matches whose confidence is below the configured threshold are filtered out.
- Credential-linked matches are kept even if their confidence is below the threshold.
- If all non-credential identification matches fall below the threshold and no credential-linked matches remain, the app treats the identification as no user found.
- In verification flows, a result in confidence bands `HIGH`, `MEDIUM`, or `LOW` is accepted as a match only when its numeric confidence is greater than or equal to the configured threshold.
- Verification results with confidence below the threshold are treated as `NoMatch`.
- Verification results with confidence band `NONE` are treated as `NoMatch`.
- The confidence filter is enforced inside the Simprints client integration layer, so search and verification flows consume already-classified results rather than applying separate UI-only filtering.

## 7. Org Unit Derived Module Id For Simprints

Status:
- `active`

Functional intent:
- Derive the Simprints `moduleId` from the user's or TEI's organisation unit context, instead of hardcoding one single module for the whole app.

Expected behavior:
- The selected biometrics configuration persists `orgUnitLevelAsModuleId` as part of the active settings.
- For enrollment and TEI dashboard biometric actions, the app derives the module id from the current organisation unit path.
- The configured value works as a path offset relative to the selected organisation unit:
  - `0` means use the selected org unit itself
  - negative values move up the organisation unit path hierarchy
- If the offset moves above the root of the available path, the app falls back to the topmost org unit in the path.
- If the derived path level would go beyond level 4, the app clamps the result to the level-4 ancestor.
- During search biometric identify, when the user has multiple capture org units, the app derives a common level-4 parent from the full org unit list; if there is not exactly one shared level-4 parent, it falls back to the Simprints default module id.

## 8. Relationship Search Identification Toggle By TE Type

Status:
- `active`

Functional intent:
- Allow biometric identification from relationship-driven search only for the tracked entity type explicitly enabled in the active biometrics configuration.

Expected behavior:
- The selected biometrics configuration persists `enableIdentificationForTET` as part of the active settings.
- In normal search flows, biometric search availability is controlled by `biometricsMode == full`.
- In relationship-driven search flows, biometric search availability ignores that normal rule and instead depends on whether the current tracked entity type UID matches `enableIdentificationForTET`.
- If the current relationship search TE type does not match `enableIdentificationForTET`, biometric identification is not enabled for that relationship search context.

## 9. Biometric Duplicate Review And Confirm Identity

Status:
- `active`

Functional intent:
- Let users review biometric matches and explicitly confirm identity when Simprints returns one or more candidate records.

Expected behavior:
- When biometric identification returns candidates, the app can show a confirmation dialog for the selected TEI.
- The dialog must use Simprints-aware display data, including biometric-specific card information when available.
- The duplicate list is resolved by running a DHIS2 tracked entity search whose query is the biometrics attribute UID mapped to the candidate GUID list returned by Simprints.
- The app supports manual confirm identity and automatic confirm identity flows, and passes the TEI/session information back to Simprints as needed.
- Candidate lists preserve credential-linked matches even when their confidence would normally be filtered out, so duplicate review can still surface credential-backed identity evidence.
- When enrollment registration returns possible duplicates, the duplicate flow allows the user to either open an existing TEI dashboard or continue with `registerLast` to capture the last biometrics step for a new enrollment.
- Sequential search state is reset after confirm identity decisions so the user does not stay in a stale biometric-search session.

## 10. Biometrics In TEI Cards, TEI Dashboard, Enrollment, And TEI Form

Status:
- `active`

Functional intent:
- Surface biometric status and Simprints-related identity information directly in the main TEI-facing screens so users can interpret and act on biometric data without leaving the workflow.

Expected behavior:
- Search cards derive avatar initials from configured first-name and last-name attributes instead of generic TEI header text.
- Search result cards derive their visible title from person-name attributes rather than a generic TEI header when those name attributes exist.
- Search result details keep biometric and NHIS-related attributes visible even when other empty attributes are hidden.
- Search result details decorate biometrics and NHIS rows with custom visual markers so users can interpret biometric availability directly from the card.
- Enrollment and TEI form flows expose biometric indicators and registration-related actions, including duplicate handling and `registerLast`, where biometric registration affects the TEI state.
- TEI dashboard exposes both biometric registration and biometric verification actions, and uses verification state to decide which dashboard biometrics card state to show.
- In landscape dashboard mode, the embedded form may be hidden to avoid duplicating complex biometric form logic that is handled in the enrollment screen.
- In `full` mode, biometric form fields stay in enrollment and the enrollment form is not treated as writable in the normal way.
- In non-`full` modes, biometric attribute form fields are filtered out of the enrollment form.
- In `zero` mode, TEI dashboard biometric cards are not shown.
- In `limited` mode, TEI dashboard biometric verification can still be shown for existing biometric data, but registration actions are hidden when no biometric value exists.

## 11. Biometric Verification Persistence

Status:
- `active`

Functional intent:
- Persist verification outcomes from Simprints and reuse them inside DHIS2 so the app can track and interpret recent biometric verification state per TEI.

Expected behavior:
- When a biometric verification succeeds, the app updates the configured biometrics tracked entity attribute with the returned identifier or value.
- The app stores verification metadata per TEI and keeps only verifications that are still valid according to the configured verification duration.
- Later workflows can read the stored verification state and treat recent verifications as active until they expire.
- Verification is a TEI dashboard capability, not an enrollment-form capability.

## 12. Time-Based Verification And Registration Failure Windows

Status:
- `active`

Functional intent:
- Control how long biometric verification remains valid and how long a declined or failed registration state remains visible before the app resets it.

Expected behavior:
- `lastVerificationDuration` defines the time window, in minutes, during which a successful biometric verification is still considered valid for a TEI.
- When biometric verification state is refreshed, verifications older than that duration are discarded and no longer treated as an active match.
- `lastDeclinedEnrolDuration` defines the time window, in minutes, during which a declined or failed registration state remains in the UI before being cleared automatically.
- In enrollment, when the biometric value represents a failure pattern, the app schedules an automatic reset after `lastDeclinedEnrolDuration`.
- In TEI dashboard registration flows, a failed registration state can remain temporarily and is cleared automatically after `lastDeclinedEnrolDuration`.

## 13. Simprints Data Exchange And Mapping

Status:
- `active`

Functional intent:
- Translate Simprints domain data into DHIS2 concepts and translate DHIS2 context into the identifiers and payloads expected by Simprints.

Expected behavior:
- The app fetches and uses Simprints configuration, including module selection and biometrics-related attribute identifiers.
- The Simprints client is created from the currently selected biometrics preferences, not directly from the remote config list.
- Client creation uses the selected `projectId`, current DHIS2 username, selected `confidenceScoreFilter`, and the app `VERSION_NAME` as `forkVersion`.
- The app maps DHIS2 TEI/enrollment context into Simprints calls for identification, confirmation, registration, verification, and `registerLast`.
- Every outbound Simprints call includes a metadata payload that can contain `forkVersion`, `trackedEntityInstanceId`, `enrollingOrgUnitId`, `enrollingOrgUnitName`, `userOrgUnits`, and `subjectAge`.
- Simprints intents are augmented with the backported `versionCode=20250102` extra so the external Simprints app returns JSON-encoded payloads that this fork knows how to parse.
- Registration, identification, verification, and confirm-identity responses are converted from Simprints JSON payloads into DHIS2-side result models, including credential information when present.
- Confirm-identity and identify responses can propagate scanned credential data back into DHIS2 so the fork can update TEI-side credential state after a biometric decision.
- If the selected biometrics preferences are missing, the factory falls back to a hardcoded default `projectId`, username `admin`, and confidence threshold `0`, so the integration still boots with a deterministic fallback client.

## 14. Areas Explicitly Out Of Scope For Preservation

Status:
- `removed`

Functional intent:
- Record the areas that still differ in this branch but must not survive the merge with `develop-eyeseetea`.

Expected behavior:
- 2FA changes are not preserved as Simprints custom behavior.
- Notifications are not preserved as Simprints custom behavior.
- Change server URL is not preserved as Simprints custom behavior.
- Granular sync flavor wiring is not preserved as Simprints custom behavior.

## Notes

- These titles are a first extraction from code structure and `EyeSeeTea customization` comments.
- They should be refined with product language before being migrated into `openspec/specs/`.
