# Biometrics In TEI Cards, TEI Dashboard, Enrollment, And TEI Form

## Purpose

Biometric and identity-related status needs to be visible and actionable
directly in the main TEI-facing screens — search results, enrollment, the TEI
form, and the TEI dashboard — so field users can interpret and act on biometric
data without leaving their normal workflow.

## Requirements

### Requirement: Search cards derive identity from configured name attributes
The app SHALL derive avatar initials and the visible card title from the
configured first-name and last-name attributes when those attributes exist,
instead of a generic TEI header.

#### Scenario: Name attributes are present
- **WHEN** a search result card is rendered for a TEI that has values for the configured first-name and last-name attributes
- **THEN** the card shows avatar initials and a title derived from those attributes instead of a generic TEI header

### Requirement: Biometric and NHIS attributes stay visible when empty attributes are hidden
The app SHALL keep biometric and NHIS-related attributes visible in search
result details even when other empty attributes are hidden, and SHALL decorate
those rows with custom visual markers.

#### Scenario: Biometric attribute has no value
- **WHEN** a search result's biometric or NHIS-related attribute has no value and other empty attributes are hidden
- **THEN** the biometric/NHIS row is still shown, decorated with its custom visual marker

### Requirement: Enrollment and TEI form expose biometric indicators and actions
The app SHALL expose biometric indicators and registration-related actions,
including duplicate handling and `registerLast`, in enrollment and TEI form
flows where biometric registration affects TEI state.

#### Scenario: Biometric registration affects the TEI being enrolled
- **WHEN** the user is enrolling or filling the TEI form for a TEI where biometric registration applies
- **THEN** biometric indicators and the relevant registration actions (including duplicate handling and `registerLast`) are shown

### Requirement: TEI dashboard exposes registration and verification, state-driven
The app SHALL expose both biometric registration and biometric verification
actions in the TEI dashboard, using verification state to decide which
dashboard biometrics card state to show.

#### Scenario: Dashboard renders the biometrics card
- **WHEN** the TEI dashboard renders the biometrics card for a TEI
- **THEN** the card state shown depends on the TEI's current verification state

### Requirement: Landscape dashboard may hide the embedded form
The app SHALL allow the embedded form to be hidden in landscape dashboard mode
to avoid duplicating complex biometric form logic already handled in the
enrollment screen.

#### Scenario: Dashboard is shown in landscape mode
- **WHEN** the TEI dashboard is displayed in landscape mode
- **THEN** the embedded form may be hidden rather than duplicating enrollment-screen biometric form logic

### Requirement: Full mode keeps biometric fields in the enrollment form
The app SHALL, in `full` biometrics mode, keep biometric form fields in
enrollment and SHALL NOT treat the enrollment form as writable in the normal
way for those fields.

#### Scenario: Program is in full mode
- **WHEN** the active biometrics mode for the current program is `full`
- **THEN** biometric attribute fields remain in the enrollment form and are not editable as ordinary writable fields

### Requirement: Non-full modes filter biometric fields out of the form
The app SHALL filter biometric attribute form fields out of the enrollment form
when the biometrics mode is not `full`.

#### Scenario: Program is in limited or zero mode
- **WHEN** the active biometrics mode for the current program is `limited` or `zero`
- **THEN** biometric attribute form fields are filtered out of the enrollment form

### Requirement: Zero mode hides dashboard biometric cards
The app SHALL NOT show TEI dashboard biometric cards when the active
biometrics mode is `zero`.

#### Scenario: Program is in zero mode
- **WHEN** the active biometrics mode for the current program is `zero`
- **THEN** no biometric card is shown in the TEI dashboard

### Requirement: Limited mode shows verification but hides registration without data
The app SHALL, in `limited` mode, keep TEI dashboard biometric verification
available for existing biometric data while hiding registration actions when no
biometric value exists.

#### Scenario: Limited mode, TEI has existing biometric data
- **WHEN** the active biometrics mode is `limited` **AND** the TEI has existing biometric data
- **THEN** the dashboard shows biometric verification for that TEI

#### Scenario: Limited mode, TEI has no biometric data
- **WHEN** the active biometrics mode is `limited` **AND** the TEI has no biometric data
- **THEN** the dashboard hides registration actions for that TEI
