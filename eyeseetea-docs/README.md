# EyeSeeTea Docs

This folder documents how EyeSeeTea manages:
- the shared baseline in `develop-eyeseetea`
- client-specific customizations
- client upgrades on top of that baseline

## Structure

### Shared baseline and process

- `upgrade/upgrade-plan-client-forks.md`
  Main upgrade workflow for client forks.
- `upgrade/conflict-rules.md`
  Reusable merge rules and conflict strategy.
- `new-fork.md`
  Checklist for creating a new client fork for the first time.
- `customizations/eyeseetea/customizations-eyeseetea.md`
  Shared EyeSeeTea customizations that belong in `develop-eyeseetea`.
- `SDK_Setup.md`
  Shared SDK/setup documentation.

### Client-specific documentation

Per client, use:
- `customizations/<client>/customization-specs.md`
  Functional meaning of the client customizations.
- `customizations/<client>/customization-files.md`
  Technical inventory of where those customizations live in code.
- `upgrade/<client>/upgrade-validation-checklist.md`
  Manual validation flows for that client.
- `upgrade/<client>/upgrade-<version>-notes.md`
  Temporary notes for one concrete upgrade only.

### Templates

- `customizations/template/customization-specs-template.md`
- `customizations/template/customization-files-template.md`
- `upgrade/template/upgrade-validation-checklist-template.md`
- `upgrade/template/upgrade-notes-template.md`

## Which document to read

### I want to understand the whole model

Read:
1. `README.md`
2. `upgrade/upgrade-plan-client-forks.md`
3. `upgrade/conflict-rules.md`

### I want to create a new client fork

Read:
1. `README.md`
2. `new-fork.md`
3. `customizations/eyeseetea/customizations-eyeseetea.md`

### I want to start or continue a client upgrade

Read:
1. `README.md`
2. `upgrade/upgrade-plan-client-forks.md`
3. `upgrade/conflict-rules.md`
4. `customizations/<client>/customization-specs.md`
5. `upgrade/<client>/upgrade-validation-checklist.md`
6. `customizations/<client>/customization-files.md`
7. `upgrade/<client>/upgrade-<version>-notes.md` if the upgrade is already in progress

### I want to know whether something belongs in the baseline or in a client branch

Read:
1. `README.md`
2. `upgrade/upgrade-plan-client-forks.md`

## Source of truth

- `develop-eyeseetea` is the shared upgrade baseline.
- `customizations/eyeseetea/customizations-eyeseetea.md` is the source of truth for shared EyeSeeTea behavior.
- `upgrade/conflict-rules.md` is the source of truth for reusable merge rules.
- `customizations/<client>/customization-specs.md` is the source of truth for client customization titles and functional intent.
- `upgrade/<client>/upgrade-validation-checklist.md` is the source of truth for manual validation of a client.
- `customizations/<client>/customization-files.md` is the source of truth for the surviving technical customization inventory of a client.
- `upgrade/<client>/upgrade-<version>-notes.md` is temporary and should not be treated as a stable long-term source of truth.
