# Biometric Verification Persistence

## Purpose

A biometric verification result from Simprints is only useful if DHIS2 can
remember and reuse it for a reasonable period, instead of forcing a new
verification on every screen that needs to know whether a TEI's identity was
recently confirmed. This fork persists verification outcomes per TEI so later
workflows can trust a recent verification without repeating it.

## Requirements

### Requirement: Successful verification updates the biometrics attribute
The app SHALL update the configured biometrics tracked entity attribute with
the returned identifier or value when a biometric verification succeeds.

#### Scenario: Verification succeeds
- **WHEN** a biometric verification for a TEI succeeds
- **THEN** the configured biometrics tracked entity attribute is updated with the value returned by the verification

### Requirement: Verification metadata is stored per TEI
The app SHALL store verification metadata per TEI and keep only verifications
that are still valid according to the configured verification duration.

#### Scenario: Verification metadata is persisted
- **WHEN** a biometric verification completes for a TEI
- **THEN** its metadata is stored per TEI, subject to the configured validity duration from [[biometrics-time-based-windows]]

### Requirement: Later workflows read stored verification state
The app SHALL let later workflows read the stored verification state and treat
a still-valid verification as active until it expires.

#### Scenario: A workflow checks verification state
- **WHEN** a workflow needs to know whether a TEI was recently verified
- **THEN** it reads the stored verification state and treats it as active if it has not yet expired

### Requirement: Verification is a dashboard capability, not an enrollment-form capability
The app SHALL expose biometric verification only as a TEI dashboard capability,
not as part of the enrollment form.

#### Scenario: User is filling the enrollment form
- **WHEN** the user is in the enrollment form (not the TEI dashboard)
- **THEN** biometric verification is not offered as a form action
