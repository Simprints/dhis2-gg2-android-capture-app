# Biometrics Mode Controls Per Program

## Purpose

Not every programme that uses biometrics wants the same level of biometric
involvement — some need full registration and search, some only need to keep
existing biometric data usable, and some need biometrics turned off entirely.
This fork lets the active Simprints configuration control that per program,
without a separate application-wide setting.

## Requirements

### Requirement: Full mode enables search and registration
The app SHALL, when the active configuration's `biometricsMode` is `full`,
enable biometric search in the normal search flow and keep biometric
registration available in enrollment and dashboard flows.

#### Scenario: Program configured as full mode
- **WHEN** the active biometrics configuration for the current program has `biometricsMode == full`
- **THEN** biometric search is available in search, and biometric registration actions are available in enrollment and TEI dashboard

### Requirement: Limited mode suppresses registration UI without existing data
The app SHALL, when `biometricsMode` is `limited`, keep Simprints verification
behavior available but suppress biometric registration UI for TEIs that do not
already have biometric data.

#### Scenario: Program configured as limited mode, TEI has no biometric data
- **WHEN** the active biometrics configuration has `biometricsMode == limited` **AND** the TEI has no existing biometric value
- **THEN** biometric registration UI is not shown for that TEI

#### Scenario: Program configured as limited mode, TEI already has biometric data
- **WHEN** the active biometrics configuration has `biometricsMode == limited` **AND** the TEI already has biometric data
- **THEN** biometric verification remains available for that TEI

### Requirement: Zero mode disables biometrics entirely
The app SHALL, when `biometricsMode` is `zero`, disable biometric UI and
biometric-driven actions for the program.

#### Scenario: Program configured as zero mode
- **WHEN** the active biometrics configuration has `biometricsMode == zero`
- **THEN** no biometric UI or biometric-driven action is shown for that program

### Requirement: Mode is read from the active per-program configuration
The app SHALL read `biometricsMode` from the configuration selected for the
current program, so entering a different program can change biometric behavior
without a global application setting.

#### Scenario: User switches between two programs with different modes
- **WHEN** the user leaves a program configured as `full` and enters a different program configured as `zero`
- **THEN** biometric UI and actions reflect `zero` mode immediately in the new program, without requiring an app-wide setting change
