# Time-Based Verification And Registration Failure Windows

## Purpose

Verification results and declined/failed registration states should not stay
authoritative or visible forever. This fork bounds both by server-configured
time windows, so a stale verification eventually stops being trusted and a
stale failure message eventually clears itself from the UI automatically.

## Requirements

### Requirement: Verification validity window
The app SHALL treat `lastVerificationDuration`, in minutes, as the time window
during which a successful biometric verification is still considered valid for
a TEI.

#### Scenario: Verification is within the window
- **WHEN** a stored verification's age is less than `lastVerificationDuration` minutes
- **THEN** it is treated as an active, valid verification

#### Scenario: Verification refresh discards expired entries
- **WHEN** biometric verification state is refreshed and a stored verification's age exceeds `lastVerificationDuration` minutes
- **THEN** that verification is discarded and no longer treated as an active match

### Requirement: Declined/failed registration visibility window
The app SHALL treat `lastDeclinedEnrolDuration`, in minutes, as the time window
during which a declined or failed registration state remains visible in the UI
before being cleared automatically.

#### Scenario: Enrollment records a failure pattern
- **WHEN** the biometric value recorded during enrollment represents a failure pattern
- **THEN** the app schedules an automatic reset of that state after `lastDeclinedEnrolDuration` minutes

#### Scenario: TEI dashboard shows a failed registration state
- **WHEN** the TEI dashboard shows a failed registration state for a TEI
- **THEN** that state is cleared automatically after `lastDeclinedEnrolDuration` minutes
