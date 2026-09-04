# Customization Files Template

Use this file as the technical inventory of one client fork.

Recommended filename after copying:
- `eyeseetea-docs/customizations/<client>/customization-files.md`

## Purpose

This file is for:
- listing where each confirmed client customization is implemented
- separating direct flavor files from shared-code implementation points
- tracking the technical status of each customization against `develop-eyeseetea`

This file is not for:
- raw full diff dumps
- temporary upgrade progress
- stable merge rules
- functional intent or business justification

## Mandatory header

- Client: `<client>`
- Flavor: `<flavor>`
- Base branch: `develop-eyeseetea`
- Base commit: `<commit>`
- Generated on: `<yyyy-mm-dd>`
- Working tree status: `clean | dirty`

## Scope

This inventory is based on:
- direct flavor files under `app/src/<flavor>/` and `app/src/<flavor>Debug/`
- shared-code implementation points currently marked with `EyeSeeTea customization`
- current diffs against `develop-eyeseetea` used only as supporting evidence

## 1. Direct <client> flavor surface

### 1.1 Flavor code

- `app/src/<flavor>/...`

### 1.2 Flavor resources and branding

- `app/src/<flavor>/`
- `app/src/<flavor>Debug/`

## 2. Shared-code customization implementation points

### 2.1 [Customization title]

Status: `active | absorbed | removed | needs_validation`

Main implementation points:
- `path/to/main/implementation`
- `path/to/another/implementation`

Supporting files in the same workflow:
- `path/to/supporting/file`

Technical note:
- short explanation of why this customization still differs from `develop-eyeseetea`

### 2.2 [Customization title]

Status: `active | absorbed | removed | needs_validation`

Main implementation points:
- `path/to/main/implementation`

Technical note:
- short explanation of the current technical state

## 3. Shared drift still differing

Use this section only for temporary or still-unclassified differences.

Rules:
- every entry must include a short note explaining why it is still here
- this section must not remain open indefinitely after an upgrade is closed
- confirmed customizations must move to section 2
- absorbed or obsolete differences should be removed instead of living here forever

Example:
- `path/to/file` - pending classification because the diff is still under review

## 4. Notes

- This inventory reflects the current branch state only.
- The source of truth for functional titles is `openspec/specs/<capability>/spec.md`. Each spec starts with a `# <Title>` line; that `<Title>` is the exact string to use here as a section heading and in `// EyeSeeTea customization - [Title]` code comments.
- If code comments and functional titles diverge, prefer the title defined in the matching OpenSpec spec and update the code comment when possible.
