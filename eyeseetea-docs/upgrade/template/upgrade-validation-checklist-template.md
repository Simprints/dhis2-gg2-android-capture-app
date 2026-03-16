# Client Validation Checklist Template

Use this file as the manual validation checklist for a new client fork.

Recommended filename after copying:
- `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md`

## Purpose

This file is for:
- minimal manual validation per customization
- expected result per flow
- regression checking after merge resolution

This file is not for:
- merge progress
- implementation details
- raw diff tracking

## Template

### 1. [Customization title]

Main files:
- `path/to/file`

Minimal manual flow:
1. Step one.
2. Step two.
3. Step three.

Expected result:
- expected visible or functional outcome

### 2. [Customization title]

Main files:
- `path/to/file`

Minimal manual flow:
1. Step one.
2. Step two.

Expected result:
- expected visible or functional outcome

## Maintenance rule

When a customization survives an upgrade:
- keep its validation flow here
- keep its functional description in `customization-specs.md`
- keep its technical inventory in `customization-files.md`
