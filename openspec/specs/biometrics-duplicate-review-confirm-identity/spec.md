# Biometric Duplicate Review And Confirm Identity

## Purpose

When Simprints returns one or more biometric candidates, the field user needs a
reliable way to review those candidates against DHIS2 data and explicitly
confirm the identity being worked with, whether that means recognizing an
existing TEI or proceeding as a new enrollment. Without this, a biometric match
alone is not enough evidence to safely merge or reuse a TEI.

## Requirements

### Requirement: Confirmation dialog for returned candidates
The app SHALL show a confirmation dialog for the selected TEI when biometric
identification returns one or more candidates.

#### Scenario: Identification returns candidates
- **WHEN** biometric identification returns at least one candidate
- **THEN** the app shows a confirmation dialog for reviewing those candidates against the selected TEI

### Requirement: Dialog shows Simprints-aware display data
The app SHALL populate the confirmation dialog using Simprints-aware display
data, including biometric-specific card information when available.

#### Scenario: Candidate has biometric-specific data
- **WHEN** a candidate in the confirmation dialog has biometric-specific card information
- **THEN** that information is shown as part of the candidate's display data

### Requirement: Duplicate list is resolved through DHIS2 search
The app SHALL resolve the duplicate candidate list by running a DHIS2 tracked
entity search using the biometrics attribute UID as the query key and the
candidate GUID list returned by Simprints as the query value.

#### Scenario: Resolving candidates into TEIs
- **WHEN** the app needs to resolve Simprints candidate GUIDs into DHIS2 TEIs for duplicate review
- **THEN** it runs a tracked entity search whose query is the biometrics attribute UID mapped to the candidate GUID list

### Requirement: Manual and automatic confirm identity are both supported
The app SHALL support both a manual confirm-identity flow driven by user choice
and an automatic confirm-identity flow, passing the relevant TEI/session
information back to Simprints as needed.

#### Scenario: User manually confirms identity
- **WHEN** the user selects a candidate and confirms identity manually
- **THEN** the app passes the resulting TEI/session information back to Simprints

#### Scenario: Identity is confirmed automatically
- **WHEN** confirm identity is resolved automatically without manual user selection
- **THEN** the app passes the resulting TEI/session information back to Simprints

### Requirement: Credential-linked matches survive duplicate review
The app SHALL keep credential-linked candidate matches visible in duplicate
review even when their confidence would otherwise be filtered out per
[[biometrics-confidence-score-filtering]].

#### Scenario: Low-confidence credential-linked candidate
- **WHEN** a candidate is linked to a credential but its confidence is below the configured threshold
- **THEN** it still appears in the duplicate review list

### Requirement: Enrollment duplicates offer dashboard or registerLast
The app SHALL, when enrollment registration returns possible duplicates, let
the user either open an existing TEI's dashboard or continue enrollment with
`registerLast` to capture the last biometrics step for the new enrollment.

#### Scenario: Enrollment registration finds possible duplicates
- **WHEN** enrollment registration returns possible duplicate matches
- **THEN** the user can choose to open the existing TEI dashboard or continue the new enrollment with `registerLast`

### Requirement: Sequential search state resets after a confirm identity decision
The app SHALL reset sequential search state after a confirm identity decision so
the user does not remain in a stale biometric-search session.

#### Scenario: User completes a confirm identity decision
- **WHEN** a confirm identity decision (manual or automatic) completes
- **THEN** the sequential search session state is reset
