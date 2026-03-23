# Upgrade Plan For Client Forks

Use this file when you are actually executing a client upgrade.

Use `eyeseetea-docs/README.md` to understand the model first. Use this file as the upgrade runbook.

## Preconditions

- `develop-eyeseetea` is always the EyeSeeTea reference branch for upgrades
- new Oslo changes must be brought into `develop-eyeseetea` first
- client forks must then be upgraded from `develop-eyeseetea`
- client forks must not be upgraded directly from Oslo
- do not use `customization-files.md` as a temporary merge notebook
- do not use `conflict-rules.md` as a temporary merge notebook
- do not treat a technical migration as a confirmed business customization without checking the functional spec

## Documents to keep open during the upgrade

- `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md`
- `eyeseetea-docs/customizations/<client>/customization-specs.md`
- `eyeseetea-docs/customizations/<client>/customization-files.md`
- `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md`
- `eyeseetea-docs/upgrade/conflict-rules.md`
- `eyeseetea-docs/upgrade/<client>/upgrade-<version>-notes.md`

## Developer checklist by phase

### Phase 1. Prepare the baseline

1. Bring the target Oslo version into `develop-eyeseetea`.
2. Resolve shared EyeSeeTea changes there.
3. Validate that `develop-eyeseetea` is the intended new baseline.

Done when:
- `develop-eyeseetea` contains Oslo plus shared EyeSeeTea changes only

### Phase 2. Start the client upgrade

1. Create or checkout the client upgrade branch.
2. Merge `develop-eyeseetea` into the client branch.
3. Do not merge Oslo directly into the client branch.

Done when:
- the client branch now only needs client-specific conflict resolution

### Phase 3. Review classification

1. Review the affected files grouped as:
   - direct flavor files
   - easy conflicts
   - manual conflicts
   - shared non-conflict diffs that may still be custom
   - obsolete or absorbed differences
2. For each file, confirm:
   - classification
   - expected functional delta
   - linked customization title if known
   - current status: `pending`, `resolved_keep_theirs`, `resolved_keep_ours`, `resolved_manual_merge`, `needs_validation`

Done when:
- easy and hard work are separated before editing starts
- files with surviving drift are visible before final reconciliation

### Phase 4. Review the easy batch

1. Review what was resolved automatically.
2. Review the remaining manual conflicts.
3. Decide whether to continue or inspect first.

Why:
- this is the best checkpoint to avoid silent loss of client behavior

### Phase 5. Review manual shared-code conflicts

For each shared conflicted file:

1. Confirm the minimum client-specific behavior that must survive.
2. Confirm whether the customization still exists or has been absorbed.
3. Confirm the exact title from `customization-specs.md`.
4. Decide whether the final diff is acceptable.

Done when:
- the client branch keeps only intentional custom behavior

### Phase 6. Validate

1. Run the build if feasible.
2. Run targeted tests if feasible.
3. Inspect critical workflows manually if needed.
4. Confirm that each active customization has either a targeted automated test or a manual validation entry.

Typical areas to validate:
- login
- enrollment
- event creation
- org unit selection
- dataset or team-change flows
- TEI dashboard

### Phase 7. Finalize

1. Compare the surviving diff against `develop-eyeseetea`.
2. Identify any remaining differences not linked to a known customization title.
3. Either document those differences or remove them.
4. Confirm that each customization in the functional spec is:
   - still present and validated
   - or explicitly marked as absorbed or removed
5. Confirm that `Shared drift still differing` is empty or contains only short-lived items with a reason and next action.
6. Do not close the upgrade while unexplained drift remains in shared code.

Done when:
- there are no unexplained surviving differences
- the spec, validation checklist, inventory, and code comments describe the same final state

## AI agent support

The AI agent may help with:
- diffing the client branch against `develop-eyeseetea`
- classifying conflicts by rule
- resolving obvious `ours` and obvious `theirs`
- updating `upgrade-<version>-notes.md`
- keeping the user informed at the end of each batch
- running the preclassification script before the easy batch when feasible

When resolving shared conflicted files, the agent should:
- start from `develop-eyeseetea`
- reapply only the minimum client-specific logic
- avoid reintroducing obsolete code
- if the customization can be isolated cleanly, move the custom helper, function, or constants block toward the end of the file
- if the customization must remain inline, keep it at the execution point
- add or preserve a nearby code comment for each surviving customization using this format:
  `// EyeSeeTea customization - [title]`
- use the exact functional title from `customization-specs.md`
- document the surviving customization in `customization-files.md` only if it still exists after merge
- if the file still differs but the business meaning is unclear, keep it in `upgrade-<version>-notes.md` and classify it as `needs_validation`

## AI agent limits

The AI agent must not automatically:
- merge Oslo directly into a client fork
- rewrite all shared conflicts by taking one side blindly
- treat `upgrade-<version>-notes.md` as a stable long-term source of truth
- remove business logic when the impact is unclear

## Documentation placement rule

- if the information helps resolve the current merge and is temporary, put it in `upgrade-<version>-notes.md`
- if the information is a reusable merge rule, put it in `conflict-rules.md`
- if the information describes a confirmed customization that survives in the final code, put it in `customization-files.md`

## Minimal agent checklist

1. Confirm current branch and base branch.
2. Confirm that the upgrade source is `develop-eyeseetea`.
3. Read `conflict-rules.md`.
4. Classify conflicts and surviving diff before editing files.
5. Resolve easy conflicts first.
6. Pause for user review after the easy batch unless instructed otherwise.
7. Resolve manual conflicts by reapplying minimal client logic.
8. Ensure each surviving customization has a nearby comment:
   `// EyeSeeTea customization - [title]`
9. When possible, keep separable custom helpers grouped near the end of the file.
10. Update `customization-files.md` only with confirmed surviving customizations.
11. Do not close the upgrade while shared drift remains unexplained.

## Done when

- the branch was upgraded from `develop-eyeseetea`, not directly from Oslo
- temporary decisions live in `upgrade-<version>-notes.md`
- final surviving customizations live in `customization-files.md`
- customization titles are aligned across code comments, spec, checklist, and inventory
- unexplained shared drift does not remain open
