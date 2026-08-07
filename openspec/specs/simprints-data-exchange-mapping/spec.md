# Simprints Data Exchange And Mapping

## Purpose

Simprints and DHIS2 model identity, organisation, and biometric results
differently. This fork is the translation boundary between the two: it builds
Simprints requests from DHIS2/session context, and converts every Simprints
response back into DHIS2-side result models the rest of the app can consume
without knowing about the Simprints wire format.

## Requirements

### Requirement: Simprints client is built from the selected biometrics preferences
The app SHALL create the Simprints client from the currently selected
biometrics preferences rather than directly from the remote configuration list.

#### Scenario: Client is created for a request
- **WHEN** the app needs a Simprints client to perform a biometrics operation
- **THEN** the client is built from the currently selected/flattened biometrics preferences, not by re-reading the remote configuration list

### Requirement: Client creation parameters
The app SHALL create the Simprints client using the selected `projectId`, the
current DHIS2 username, the selected `confidenceScoreFilter`, and the app
`VERSION_NAME` as `forkVersion`.

#### Scenario: Building client creation parameters
- **WHEN** the Simprints client is constructed
- **THEN** it uses the active configuration's `projectId`, the logged-in DHIS2 username, the active `confidenceScoreFilter`, and the app's `VERSION_NAME` as `forkVersion`

### Requirement: DHIS2 context maps into Simprints calls
The app SHALL map DHIS2 TEI/enrollment context into the corresponding Simprints
calls for identification, confirmation, registration, verification, and
`registerLast`.

#### Scenario: A biometrics action is triggered
- **WHEN** the user triggers a biometrics action (identify, confirm, register, verify, or `registerLast`)
- **THEN** the app maps the current TEI/enrollment context into the matching Simprints call

### Requirement: Outbound calls include a metadata payload
The app SHALL include a metadata payload in every outbound Simprints call that
can contain `forkVersion`, `trackedEntityInstanceId`, `enrollingOrgUnitId`,
`enrollingOrgUnitName`, `userOrgUnits`, and `subjectAge`.

#### Scenario: Sending a Simprints request
- **WHEN** the app sends any Simprints call
- **THEN** the request includes a metadata payload populated with the applicable fields among `forkVersion`, `trackedEntityInstanceId`, `enrollingOrgUnitId`, `enrollingOrgUnitName`, `userOrgUnits`, and `subjectAge`

### Requirement: Intents request JSON-encoded responses
The app SHALL augment Simprints intents with the backported
`versionCode=20250102` extra so the external Simprints app returns
JSON-encoded payloads this fork can parse.

#### Scenario: Launching a Simprints intent
- **WHEN** the app launches an intent to the external Simprints app
- **THEN** the intent includes the `versionCode=20250102` extra

### Requirement: Responses are converted into DHIS2-side result models
The app SHALL convert registration, identification, verification, and
confirm-identity responses from Simprints JSON payloads into DHIS2-side result
models, including credential information when present.

#### Scenario: Simprints returns a response
- **WHEN** the external Simprints app returns a registration, identification, verification, or confirm-identity response
- **THEN** the app parses the JSON payload into the corresponding DHIS2-side result model, including any credential information present

### Requirement: Scanned credential data propagates back into DHIS2
The app SHALL let confirm-identity and identify responses propagate scanned
credential data back into DHIS2 so TEI-side credential state can be updated
after a biometric decision.

#### Scenario: Response includes scanned credential data
- **WHEN** a confirm-identity or identify response includes scanned credential data
- **THEN** that data is propagated back so the app can update TEI-side credential state

### Requirement: Missing preferences fall back to a deterministic default client
The app SHALL fall back to a hardcoded default `projectId`, username `admin`,
and confidence threshold `0` when the selected biometrics preferences are
missing, so the integration still boots with a deterministic client.

#### Scenario: No biometrics preferences are available
- **WHEN** the app needs a Simprints client but no biometrics preferences have been selected
- **THEN** the factory falls back to the hardcoded default `projectId`, username `admin`, and confidence threshold `0`
