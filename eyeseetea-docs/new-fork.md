# New Fork Checklist

Use this checklist when you are actually creating a new client fork.

Use `eyeseetea-docs/README.md` to understand the model first. Use this file as the execution checklist.

## Preconditions

- the new fork starts from `develop-eyeseetea`
- shared EyeSeeTea behavior belongs in `develop-eyeseetea`, not in the client folder
- do not compare the new fork directly against Oslo as the source of truth

## Developer checklist

### 1. Create the documentation

1. Use `eyeseetea-docs/upgrade/conflict-rules.md` as the shared merge guide for future upgrades.
2. Copy `eyeseetea-docs/customizations/template/customization-specs-template.md` to `eyeseetea-docs/customizations/<client>/customization-specs.md`.
3. Copy `eyeseetea-docs/upgrade/template/upgrade-validation-checklist-template.md` to `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md`.
4. Copy `eyeseetea-docs/customizations/template/customization-files-template.md` to `eyeseetea-docs/customizations/<client>/customization-files.md`.

### 2. Define the fork identity

1. Record the client name.
2. Record the client branch or branch naming convention.
3. Record the Android flavor name.
4. Record direct flavor paths such as `app/src/<flavor>/` and `app/src/<flavor>Debug/`.

### 3. Create the flavor surface

1. Create the required flavor directories in code and resources.
2. Confirm that flavor-only branding and resources stay in the flavor surface.

### 4. Build the initial customization inventory

1. List direct flavor-specific files.
2. List current shared files that already differ from `develop-eyeseetea`.
3. Populate `customization-files.md` with the initial technical inventory.
4. Populate `customization-specs.md` with the initial functional titles and expected behavior.
5. Populate `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md` with the manual validation flows that will matter in future upgrades.

### 5. Mark the customization boundaries

1. Ensure shared custom code uses `// EyeSeeTea customization - [title]` where relevant.
2. Add `Base behavior:` only when the customization replaces or restricts the base behavior.
3. Keep flavor-only branding and resource differences out of shared-code commentary.

## AI agent support

- inspect the current branch against `develop-eyeseetea`
- list flavor-specific files and shared files that already differ
- draft the first version of `customization-files.md`
- check whether customization titles in code comments and docs are aligned
- point out missing templates or incomplete fork metadata

## AI agent limits

- invent functional customization titles without developer confirmation
- classify every shared diff as a real client customization
- move shared EyeSeeTea baseline behavior into the client-specific inventory

## Done when

1. `customization-files.md` is the technical inventory for this fork.
2. `customization-specs.md` is the functional reference for this fork.
3. `upgrade-validation-checklist.md` is the manual validation reference for this fork.
4. `conflict-rules.md` remains the shared upgrade guide used later when this fork is upgraded.
