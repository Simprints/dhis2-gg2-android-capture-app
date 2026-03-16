# Client Customization Spec Template

Use this file as the functional spec for a new client fork.

Recommended filename after copying:
- `eyeseetea-docs/customizations/<client>/customization-specs.md`

## Purpose

This file describes:
- the functional title of each customization
- why it exists
- the expected behavior
- the current lifecycle status of the customization

This file should not become:
- a merge-progress notebook
- a raw file inventory
- a substitute for the validation checklist

## How to use

For each customization:
- use one exact title
- keep the title stable across upgrades
- describe behavior, not implementation detail
- keep the status aligned with the real baseline comparison

## Template

### 1. [Customization title]

Status:
- `active | absorbed | removed | needs_validation`

Functional intent:
- Describe why this customization exists.

Expected behavior:
- Describe what the user should experience.
- Describe what must remain true after future upgrades.

### 2. [Customization title]

Status:
- `active | absorbed | removed | needs_validation`

Functional intent:
- Describe why this customization exists.

Expected behavior:
- Describe what the user should experience.
- Describe what must remain true after future upgrades.

## Maintenance rule

When a customization still exists after an upgrade:
- keep its functional meaning here
- keep its technical file inventory in `customization-files.md`
- keep its manual validation flow in `upgrade-validation-checklist.md`

When a customization is removed or absorbed by the baseline:
- update the status here explicitly
- do not keep obsolete specs as if they were still active
