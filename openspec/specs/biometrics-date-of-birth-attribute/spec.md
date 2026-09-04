# Configurable Date Of Birth Attribute For Biometrics

## Purpose

Different programmes model date of birth with different tracked entity
attributes or form fields. Age-based biometrics decisions ([[biometrics-age-threshold]])
need one consistent way to find "the" date of birth for a TEI regardless of
which attribute a given programme uses, driven by server-side configuration
rather than a hardcoded attribute UID.

## Requirements

### Requirement: Configuration persists which attribute represents date of birth
The app SHALL persist `dateOfBirthAttribute` as part of the active
program/org-unit biometrics settings.

#### Scenario: Configuration is selected for a program
- **WHEN** a biometrics configuration is selected as active for a program
- **THEN** its `dateOfBirthAttribute` value is available to all age-related biometrics logic for that program

### Requirement: Search reads date of birth from the configured query attribute
The app SHALL read date of birth from the configured attribute key inside
search query data when performing search-time age checks.

#### Scenario: Age check during search
- **WHEN** search needs to evaluate the age threshold for a search query
- **THEN** it reads the date of birth from the query data key matching the configured `dateOfBirthAttribute`

### Requirement: Enrollment reads date of birth from the configured form field
The app SHALL read date of birth from the form field whose UID matches the
configured `dateOfBirthAttribute` when performing enrollment-time age checks.

#### Scenario: Age check during enrollment
- **WHEN** enrollment needs to evaluate the age threshold for the TEI being enrolled
- **THEN** it reads the date of birth from the loaded form field whose UID matches `dateOfBirthAttribute`

### Requirement: TEI and dashboard flows read date of birth from stored attribute values
The app SHALL read date of birth from the stored tracked entity attribute value
matching `dateOfBirthAttribute` when performing TEI and dashboard age checks.

#### Scenario: Age check on an existing TEI
- **WHEN** the TEI dashboard or a TEI-level flow needs to evaluate the age threshold for an existing TEI
- **THEN** it reads the date of birth from the TEI's stored attribute value for `dateOfBirthAttribute`

### Requirement: Missing configured attribute falls back to zero months
The app SHALL treat a missing or empty configured date-of-birth value as `0`
months of age.

#### Scenario: Configured attribute has no value
- **WHEN** the value for the configured `dateOfBirthAttribute` is missing or empty for a TEI or query
- **THEN** the computed age is `0` months, which is below the age threshold unless the threshold itself is `0`
