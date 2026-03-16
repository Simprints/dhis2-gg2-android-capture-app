# Customization Files Template

Use this file as the technical inventory of one client fork.

Recommended filename after copying:
- `eyeseetea-docs/customizations/<client>/customization-files.md`

## Purpose

This file is for:
- listing where each client customization is implemented
- separating direct flavor files from shared-code implementation points
- tracking supporting files that belong to the same customization area

This file is not for:
- raw full diff dumps
- temporary upgrade progress
- stable merge rules

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

- `path/to/main/implementation`
- `path/to/another/implementation`

Supporting files differing in the same workflow:
- `path/to/supporting/file`

### 2.2 [Customization title]

- `path/to/main/implementation`

## 3. Shared drift still differing but not mapped to a documented customization title

- `path/to/file`

## 4. Notes

- This inventory reflects the current branch state only.
- The source of truth for functional titles remains `customization-specs.md`.
- If code comments and functional titles diverge, prefer the title defined in `customization-specs.md` and update the code comment when possible.
