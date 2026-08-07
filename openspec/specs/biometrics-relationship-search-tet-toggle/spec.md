# Relationship Search Identification Toggle By TE Type

## Purpose

Biometric identification is not appropriate for every tracked entity type
reachable through relationship-driven search (e.g. searching for a household
member from a case record). This fork lets the server restrict biometric
identification in that specific search context to a single, explicitly
configured tracked entity type, independent of the normal per-program mode
rule.

## Requirements

### Requirement: Allowed TE type is part of the active configuration
The app SHALL persist `enableIdentificationForTET` as part of the active
biometrics configuration.

#### Scenario: Configuration is selected for a program
- **WHEN** a biometrics configuration is selected as active
- **THEN** its `enableIdentificationForTET` value is available to relationship search logic

### Requirement: Normal search follows the per-program mode rule
The app SHALL, in normal (non-relationship) search flows, control biometric
search availability using `biometricsMode == full` as defined in
[[biometrics-mode-controls]].

#### Scenario: Normal search checks availability
- **WHEN** biometric search availability is evaluated in a normal search flow
- **THEN** it follows the `biometricsMode == full` rule, not the relationship search TE type rule

### Requirement: Relationship search uses the TE type toggle instead
The app SHALL, in relationship-driven search flows, ignore the normal mode rule
and instead enable biometric identification only when the current tracked
entity type UID matches `enableIdentificationForTET`.

#### Scenario: Relationship search TE type matches the configured value
- **WHEN** the user searches from a relationship context whose tracked entity type UID matches `enableIdentificationForTET`
- **THEN** biometric identification is enabled for that relationship search, regardless of `biometricsMode`

#### Scenario: Relationship search TE type does not match
- **WHEN** the user searches from a relationship context whose tracked entity type UID does not match `enableIdentificationForTET`
- **THEN** biometric identification is not enabled for that relationship search
