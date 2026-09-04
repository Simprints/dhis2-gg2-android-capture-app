# Age Threshold Controls For Biometrics

## Purpose

Biometric capture is not appropriate or reliable below a certain age for the
programmes this fork serves. The app needs to consistently exclude biometric
registration, search, and verification for TEIs under the configured age
threshold, across every screen that surfaces biometrics, rather than leaving
that decision to each screen independently.

## Requirements

### Requirement: Age is derived from the configured date-of-birth attribute
The app SHALL derive age in months using the tracked entity attribute or form
field configured as `dateOfBirthAttribute`, and compare it against
`ageThresholdMonths` from the active biometrics configuration.

#### Scenario: Computing age for a threshold check
- **WHEN** the app needs to decide whether biometrics apply to a TEI or search query
- **THEN** it computes age in months from the configured date-of-birth attribute and compares it to the active `ageThresholdMonths`

### Requirement: Search skips biometric continuation below threshold
The app SHALL skip the biometric next-action path in search when the searched
attributes include a date of birth below the threshold.

#### Scenario: Attribute search returns an under-threshold date of birth
- **WHEN** the user performs an attribute search whose date of birth is below `ageThresholdMonths`
- **THEN** the next sequential search action skips biometric search and goes directly to the non-biometric continuation path

### Requirement: Enrollment and dashboard hide biometric actions below threshold
The app SHALL NOT expose biometric registration or verification actions for
TEIs below the age threshold in enrollment and TEI dashboard flows.

#### Scenario: TEI below threshold in enrollment or dashboard
- **WHEN** the current TEI's computed age in months is below the active `ageThresholdMonths`
- **THEN** biometric registration and verification actions are not shown for that TEI in enrollment and TEI dashboard

### Requirement: Missing biometric data below threshold is shown as Not Applicable
The app SHALL show missing biometric data for under-threshold TEIs as `Not
Applicable` rather than as a failure state in search and dashboard cards.

#### Scenario: Card renders an under-threshold TEI with no biometric data
- **WHEN** a search or dashboard card renders a TEI below the age threshold that has no biometric data
- **THEN** the biometric status is displayed as `Not Applicable` instead of a failure or missing-data indicator
