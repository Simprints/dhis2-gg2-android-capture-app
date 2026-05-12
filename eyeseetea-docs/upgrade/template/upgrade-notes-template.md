# Upgrade Notes Template

Use this file as the temporary working notes for one concrete client upgrade.

Recommended filename after copying:
- `eyeseetea-docs/upgrade/<client>/upgrade-<version>-notes.md`

## Purpose

This file is for:
- temporary upgrade progress
- conflict decisions taken during the current upgrade
- unresolved questions
- follow-up checks before closing the upgrade

This file is not for:
- stable merge rules
- final customization inventory
- long-term functional documentation

## Header

- Client: `<client>`
- Target version: `<version>`
- Base branch: `develop-eyeseetea`
- Upgrade branch: `<branch-name>`
- Started on: `<date>`
- Status: `in_progress`

## Progress

- baseline prepared: `yes/no`
- merge started: `yes/no`
- easy conflicts resolved: `yes/no`
- manual conflicts pending: `yes/no`
- validation started: `yes/no`

## Decisions

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| path/to/file | accept_theirs / accept_ours / manual_reapply_on_theirs / defer_after_build_verification | one helper call / one assertion / absorbed in base | customization title or `n/a` | pending / resolved_keep_theirs / resolved_keep_ours / resolved_manual_merge / needs_validation | short reason |

## Open Questions

- question 1
- question 2

## Validation Notes

- build:
- targeted tests:
- manual flows checked:

## Finalization

- surviving customizations moved to `customization-files.md`: `yes/no`
- stable rules moved to `conflict-rules.md`: `yes/no`
- temporary notes ready to archive/remove: `yes/no`
- unexplained shared drift remaining: `yes/no`
