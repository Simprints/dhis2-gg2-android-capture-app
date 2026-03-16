# New Fork Checklist

Use this checklist when creating a new client fork for the first time.

## Create the documentation

1. Use `eyeseetea-docs/upgrade/conflict-rules.md` as the shared merge guide for future upgrades.
2. Copy `eyeseetea-docs/customizations/template/customization-specs-template.md` to `eyeseetea-docs/customizations/<client>/customization-specs.md`.
3. Copy `eyeseetea-docs/upgrade/template/upgrade-validation-checklist-template.md` to `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md`.
4. Copy `eyeseetea-docs/customizations/template/customization-files-template.md` to `eyeseetea-docs/customizations/<client>/customization-files.md`.

## Define the fork identity

1. Record the client name.
2. Record the client branch or branch naming convention.
3. Record the Android flavor name.
4. Record direct flavor paths such as `app/src/<flavor>/` and `app/src/<flavor>Debug/`.

## Establish the baseline

1. Confirm that `develop-eyeseetea` is the baseline branch for the fork.
2. Confirm that shared EyeSeeTea behavior belongs in `develop-eyeseetea`, not in the client folder.
3. Do not compare the new fork directly against Oslo as the source of truth.

## Build the initial customization inventory

1. List direct flavor-specific files.
2. List current shared files that already differ from `develop-eyeseetea`.
3. Populate `customization-files.md` with the initial technical inventory.
4. Populate `customization-specs.md` with the initial functional titles and expected behavior.
5. Populate `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md` with the manual validation flows that will matter in future upgrades.

## Mark the customization boundaries

1. Ensure shared custom code uses `// EyeSeeTea customization - [title]` where relevant.
2. Add `Base behavior:` only when the customization replaces or restricts the base behavior.
3. Keep flavor-only branding and resource differences out of shared-code commentary.

## Final state

1. `customization-files.md` is the technical inventory for this fork.
2. `customization-specs.md` is the functional reference for this fork.
3. `upgrade-validation-checklist.md` is the manual validation reference for this fork.
4. `conflict-rules.md` remains the shared upgrade guide used later when this fork is upgraded.
