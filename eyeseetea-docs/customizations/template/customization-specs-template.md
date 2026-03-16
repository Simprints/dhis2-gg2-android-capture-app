# Client Customization Spec Template

Use this file as the functional spec for a new client fork.

Recommended filename after copying:
- `eyeseetea-docs/customizations/<client>/customization-specs.md`

## Purpose

This file describes:
- the functional title of each customization
- why it exists
- the expected behavior

This file should not become a merge-progress notebook.

## How to use

For each customization:
- use one exact title
- keep the title stable across upgrades
- describe behavior, not implementation detail

## Template

### 1. [Customization title]

Functional intent:
- Describe why this customization exists.

Expected behavior:
- Describe what the user should experience.
- Describe what must remain true after future upgrades.

Possible affected areas:
- `app/src/<flavor>/...`
- `app/src/main/...`
- `commons/...`
- `form/...`
- `aggregates/...`

### 2. [Customization title]

Functional intent:
- Describe why this customization exists.

Expected behavior:
- Describe what the user should experience.
- Describe what must remain true after future upgrades.

Possible affected areas:
- `...`

## Maintenance rule

When a customization still exists after an upgrade:
- keep it here
- keep its technical file inventory in `customization-files.md`

When a customization is removed or absorbed by the baseline:
- update this file
- do not keep obsolete specs as if they were still active
