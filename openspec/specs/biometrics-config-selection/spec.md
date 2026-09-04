# Biometrics Configuration Selection Per Program Or Org Unit Group

## Purpose

Different programmes and organisation unit groups operated by the client can
require different Simprints biometrics setups (different project, thresholds,
or module logic) from a single installed app. The stock DHIS2 Android client has
no concept of biometrics configuration at all. This fork downloads the set of
configurations the server exposes and resolves exactly one active configuration
for the context the user is currently working in, so downstream biometrics
behavior always has a single, unambiguous configuration to read from.

## Requirements

### Requirement: Download and store biometrics configurations
The app SHALL download the list of Simprints biometrics configurations from the
server and store them locally during login or synchronization.

#### Scenario: Configurations are refreshed during sync
- **WHEN** login or synchronization completes successfully
- **THEN** the locally stored list of biometrics configurations reflects the server's current configurations

### Requirement: Select one active configuration per program
The app SHALL select exactly one active biometrics configuration when the user
enters a program.

#### Scenario: User navigates into a program
- **WHEN** the user opens a program
- **THEN** the app selects one biometrics configuration to be active for that program before the program screen opens

### Requirement: Configuration selection precedence
The app SHALL resolve the active configuration using, in order: a configuration
matching the program, otherwise a configuration matching one of the user's
capture-scope organisation unit groups, otherwise the configuration whose
organisation unit group is `default`.

#### Scenario: Program-specific configuration exists
- **WHEN** a biometrics configuration explicitly matches the current program
- **THEN** that configuration is selected regardless of organisation unit group matches

#### Scenario: No program match but an org unit group matches
- **WHEN** no configuration matches the current program **AND** a configuration matches one of the user's capture-scope organisation unit groups
- **THEN** that organisation-unit-group configuration is selected

#### Scenario: No program or org unit group match
- **WHEN** no configuration matches the program or any of the user's capture-scope organisation unit groups
- **THEN** the configuration whose organisation unit group is `default` is selected

### Requirement: A default configuration is mandatory
The app SHALL treat the absence of a `default` configuration as a selection
failure rather than guessing a configuration.

#### Scenario: No default configuration is present
- **WHEN** configuration selection falls through to the `default` case **AND** no configuration with organisation unit group `default` exists
- **THEN** selection fails instead of silently activating an arbitrary configuration

### Requirement: Selected configuration drives downstream biometrics behavior
The app SHALL make the selected configuration the single source for the
settings that control later biometrics flows, including `projectId`,
`biometricsMode`, `icon`, `confidenceScoreFilter`, `ageThresholdMonths`,
`dateOfBirthAttribute`, `orgUnitLevelAsModuleId`, `lastVerificationDuration`,
`lastDeclinedEnrolDuration`, and `enableIdentificationForTET`.

#### Scenario: Downstream flow reads a configuration value
- **WHEN** search, enrollment, or dashboard logic needs a biometrics setting
- **THEN** it reads that setting from the configuration selected for the current program

### Requirement: Selected configuration is flattened into preferences
The app SHALL persist the active configuration into preferences after selection
so later flows read one effective configuration instead of re-resolving the
full configuration list.

#### Scenario: A later screen reads the active configuration
- **WHEN** a screen other than the program entry point needs the active biometrics configuration
- **THEN** it reads the flattened preference values instead of re-running configuration selection
