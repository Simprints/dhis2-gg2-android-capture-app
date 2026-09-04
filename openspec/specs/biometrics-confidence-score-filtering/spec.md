# Confidence Score Filtering For Simprints Matches

## Purpose

Simprints can return biometric matches with varying confidence. Surfacing
low-confidence matches as if they were reliable identifications would create
false positives in identification and verification. This fork filters matches
by a server-configured confidence threshold, while still protecting
credential-linked matches that must remain visible for identity review even at
lower confidence.

## Requirements

### Requirement: Confidence threshold is part of the active configuration
The app SHALL persist `confidenceScoreFilter` as part of the active Simprints
biometrics configuration.

#### Scenario: Configuration is selected for a program
- **WHEN** a biometrics configuration is selected as active
- **THEN** its `confidenceScoreFilter` value is available to identification and verification result handling

### Requirement: Identification filters out low-confidence, non-credential matches
The app SHALL exclude identification candidate matches whose confidence is
below the configured threshold, unless the match is linked to a credential.

#### Scenario: Candidate below threshold without credential
- **WHEN** an identification response includes a candidate match with confidence below `confidenceScoreFilter` and no linked credential
- **THEN** that candidate is excluded from the results shown to the user

#### Scenario: Candidate below threshold with a linked credential
- **WHEN** an identification response includes a candidate match with confidence below `confidenceScoreFilter` but linked to a credential
- **THEN** that candidate is kept in the results

### Requirement: No matches after filtering means no user found
The app SHALL treat an identification response as "no user found" when every
non-credential match falls below the threshold and no credential-linked match
remains.

#### Scenario: All candidates filtered out
- **WHEN** every candidate in an identification response is below the confidence threshold and none are credential-linked
- **THEN** the app reports the identification as no user found

### Requirement: Verification requires confidence to meet the threshold
The app SHALL accept a verification result in confidence band `HIGH`, `MEDIUM`,
or `LOW` as a match only when its numeric confidence is greater than or equal
to `confidenceScoreFilter`.

#### Scenario: Verification confidence meets the threshold
- **WHEN** a verification result has band `HIGH`, `MEDIUM`, or `LOW` **AND** its numeric confidence is greater than or equal to `confidenceScoreFilter`
- **THEN** the result is accepted as a match

#### Scenario: Verification confidence is below the threshold
- **WHEN** a verification result has band `HIGH`, `MEDIUM`, or `LOW` **AND** its numeric confidence is below `confidenceScoreFilter`
- **THEN** the result is treated as `NoMatch`

### Requirement: NONE confidence band is always NoMatch
The app SHALL treat a verification result with confidence band `NONE` as
`NoMatch` regardless of the numeric confidence value.

#### Scenario: Verification band is NONE
- **WHEN** a verification result has confidence band `NONE`
- **THEN** the result is treated as `NoMatch`

### Requirement: Filtering is enforced in the Simprints integration layer
The app SHALL apply confidence filtering inside the Simprints client
integration layer, so search and verification flows consume already-classified
results.

#### Scenario: Search or verification flow consumes a result
- **WHEN** search or verification logic reads an identification or verification result
- **THEN** confidence filtering has already been applied by the Simprints client integration layer, with no separate UI-only filtering required
