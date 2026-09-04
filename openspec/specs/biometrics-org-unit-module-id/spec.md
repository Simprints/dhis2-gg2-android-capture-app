# Org Unit Derived Module Id For Simprints

## Purpose

Simprints partitions biometric data by `moduleId`, and this client's biometric
data needs to be partitioned by organisation unit rather than a single
hardcoded module for the whole app. This fork derives `moduleId` from
organisation unit context using a configurable offset, so field data stays
scoped to the right operational area in Simprints.

## Requirements

### Requirement: Module id offset is part of the active configuration
The app SHALL persist `orgUnitLevelAsModuleId` as part of the active biometrics
configuration.

#### Scenario: Configuration is selected for a program
- **WHEN** a biometrics configuration is selected as active
- **THEN** its `orgUnitLevelAsModuleId` value is available to module id derivation logic

### Requirement: Single-org-unit flows derive module id from the org unit path
The app SHALL derive the module id for enrollment and TEI dashboard biometric
actions from the current organisation unit path, offset by
`orgUnitLevelAsModuleId`.

#### Scenario: Offset is zero
- **WHEN** `orgUnitLevelAsModuleId` is `0`
- **THEN** the module id is derived from the currently selected organisation unit itself

#### Scenario: Offset is negative
- **WHEN** `orgUnitLevelAsModuleId` is negative
- **THEN** the module id is derived by moving up the organisation unit path hierarchy by that offset

### Requirement: Offset is clamped to the available path
The app SHALL fall back to the topmost available organisation unit when the
offset moves above the root of the path, and SHALL clamp to the level-4
ancestor when the derived level would exceed level 4.

#### Scenario: Offset moves above the root
- **WHEN** applying the configured offset would move past the top of the organisation unit path
- **THEN** the app uses the topmost organisation unit in the path

#### Scenario: Derived level exceeds level 4
- **WHEN** the derived organisation unit level would be deeper than level 4
- **THEN** the app clamps the result to the level-4 ancestor

### Requirement: Multi-org-unit search identify derives a shared parent
The app SHALL, during search biometric identify with multiple capture
organisation units, derive a common level-4 parent from the full organisation
unit list, and fall back to the Simprints default module id when there is not
exactly one shared level-4 parent.

#### Scenario: Users capture org units share one level-4 parent
- **WHEN** the user has multiple capture organisation units that share exactly one level-4 parent
- **THEN** that shared level-4 parent is used as the module id for search biometric identify

#### Scenario: Users capture org units do not share one level-4 parent
- **WHEN** the user has multiple capture organisation units that do not share exactly one level-4 parent
- **THEN** the app falls back to the Simprints default module id for search biometric identify
