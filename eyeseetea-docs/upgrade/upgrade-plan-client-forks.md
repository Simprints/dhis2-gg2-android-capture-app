# Upgrade Plan For Client Forks

Step-by-step process for upgrading a client fork.

This file is written for:
- engineers who have never upgraded a fork before
- agents or models starting from zero context

## Entry Point

Read `eyeseetea-docs/README.md` first for the document map and source-of-truth rules.

This file focuses only on the upgrade workflow itself.

## Full decision flow

When upgrading a client fork:

1. upgrade Oslo into `develop-eyeseetea` first
2. verify the shared EyeSeeTea baseline there
3. merge `develop-eyeseetea` into the client fork
4. classify conflicts and surviving diff immediately after the merge
5. resolve easy conflicts using the rules file
6. pause for review if needed
7. resolve manual shared-code conflicts by reapplying only the minimum client logic
8. mark surviving customizations in code with the exact EyeSeeTea comment title
9. validate each surviving customization with the client validation checklist
10. keep temporary merge progress in `upgrade-<version>-notes.md`
11. keep the final stable client inventory in `customization-files.md`
12. close only when no unexplained drift remains

Never do this:

- do not merge Oslo directly into a client fork
- do not use `customization-files.md` as a temporary merge notebook
- do not use `conflict-rules.md` as a temporary merge notebook
- do not treat a technical migration as a confirmed business customization without checking the functional spec

## Which files belong in baseline vs client fork

Keep in `develop-eyeseetea`:

- shared upgrade process documents
- reusable templates for future clients
- shared EyeSeeTea baseline inventory
- SDK/setup documentation used by more than one client

Typical examples:

- `eyeseetea-docs/upgrade/upgrade-plan-client-forks.md`
- `eyeseetea-docs/new-fork.md`
- `eyeseetea-docs/customizations/template/customization-specs-template.md`
- `eyeseetea-docs/customizations/template/customization-files-template.md`
- `eyeseetea-docs/upgrade/template/upgrade-validation-checklist-template.md`
- `eyeseetea-docs/upgrade/template/upgrade-notes-template.md`
- `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md`
- `eyeseetea-docs/SDK_Setup.md`

Keep in the client branch:

- client-specific customization spec
- client-specific validation checklist
- final client-specific customization inventory

Typical examples:

- `eyeseetea-docs/customizations/<client>/customization-files.md`
- `eyeseetea-docs/customizations/<client>/customization-specs.md`
- `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md`
- `eyeseetea-docs/upgrade/<client>/upgrade-<version>-notes.md`

Decision rule:

- if the file mentions a specific client flavor or branch and would not be reusable as-is for another client, keep it in the client branch
- if the file defines process, baseline rules, or reusable templates for future client upgrades, keep it in `develop-eyeseetea`

## Rule of record

`develop-eyeseetea` is always the EyeSeeTea reference branch for upgrades.

That means:

- new Oslo changes must be brought into `develop-eyeseetea` first
- client forks must then be upgraded from `develop-eyeseetea`
- client forks must not be upgraded directly from Oslo

## Why this rule exists

If a client fork is merged directly with Oslo:
- shared EyeSeeTea customizations are mixed with client-specific ones
- the diff becomes noisy
- conflict resolution becomes harder
- the final inventory becomes unreliable

If the client fork is upgraded from `develop-eyeseetea`:
- the shared baseline is already normalized
- remaining differences are much closer to true client customizations
- conflict review is faster and safer

## Documents to use during an upgrade

- `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md`
  Reference customizations of the EyeSeeTea baseline branch.

- `eyeseetea-docs/customizations/<client>/customization-files.md`
  Final confirmed customizations for the client on top of `develop-eyeseetea`.

- `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md`
  Stable manual validation flows for that client.

- `eyeseetea-docs/upgrade/conflict-rules.md`
  Merge conflict rules and decision criteria.

- `eyeseetea-docs/upgrade/<client>/upgrade-<version>-notes.md`
  Temporary progress, decisions, and unresolved questions for the current upgrade only.

## Standard workflow

### Phase 1. Prepare the baseline

1. Bring the target Oslo version into `develop-eyeseetea`.
2. Resolve shared EyeSeeTea changes there.
3. Validate that `develop-eyeseetea` is the intended new baseline.

Expected result:
- `develop-eyeseetea` contains Oslo + shared EyeSeeTea changes only

### Phase 2. Start the client upgrade

1. Create or checkout the client upgrade branch.
2. Merge `develop-eyeseetea` into the client branch.
3. Do not merge Oslo directly into the client branch.

Expected result:
- the client branch now only needs client-specific conflict resolution

### Phase 3. Classify conflicts and surviving diff

Right after the merge, classify every affected file into one of these groups:

- direct flavor files
- easy conflicts
- manual conflicts
- shared non-conflict diffs that may still be custom
- obsolete or absorbed differences

For each file, record at least:

- classification
- expected functional delta
- linked customization title if known
- current status: `pending`, `resolved_keep_theirs`, `resolved_keep_ours`, `resolved_manual_merge`, `needs_validation`

Expected result:
- easy and hard work are separated before editing starts
- files with surviving drift are visible before the final reconciliation

### Phase 4. Resolve easy conflicts

1. Resolve direct client flavor files automatically.
2. Resolve obvious shared-base conflicts using the rules file.
3. Record the decisions taken in `upgrade-<version>-notes.md`.

Expected result:
- low-risk conflicts are resolved quickly
- difficult files remain clearly identified

### Phase 5. Pause for review

After the easy conflict batch:

1. summarize what was resolved automatically
2. list the remaining manual conflicts
3. ask the user whether to continue or review first

Why:
- this is the best checkpoint to avoid silent loss of client behavior

### Phase 6. Resolve manual conflicts

For each shared conflicted file:

1. start from `develop-eyeseetea`
2. reapply only the minimum client-specific logic
3. avoid reintroducing obsolete code
4. if the customization can be isolated cleanly, move the custom helper/function/constants block toward the end of the file
5. if the customization must remain inline, keep it at the execution point
6. add or preserve a nearby code comment for each surviving customization using this format:
   `// EyeSeeTea customization - [title]`
7. use the exact functional title from `customization-specs.md`
8. document the surviving customization in `customization-files.md` if it still exists after merge
9. if the file still differs but the business meaning is unclear, keep it in `upgrade-<version>-notes.md` and classify it as `needs_validation`

Expected result:
- the client branch keeps only intentional custom behavior
- separable custom code is easier to identify in future upgrades

### Phase 7. Validate

1. run the build if feasible
2. run targeted tests if feasible
3. inspect critical workflows manually if needed
4. confirm that each active customization has either a targeted automated test or a manual validation entry

Typical areas to validate:
- login
- enrollment
- event creation
- org unit selection
- dataset / team-change flows
- TEI dashboard

### Phase 8. Finalize documentation

1. keep `customization-files.md` as the final stable inventory
2. keep `conflict-rules.md` as stable merge guidance
3. keep `upgrade-validation-checklist.md` as the stable manual validation reference
4. keep `upgrade-<version>-notes.md` as temporary upgrade notes only, or remove/archive it when the upgrade is complete

### Phase 9. Final reconciliation

Before considering the upgrade complete:

1. compare the surviving diff against `develop-eyeseetea`
2. identify any remaining differences not linked to a known customization title
3. either document those differences or remove them
4. confirm that each customization in the functional spec is:
   - still present and validated
   - or explicitly marked as absorbed/removed
5. confirm that `Shared drift still differing` is empty or contains only short-lived items with a reason and next action
6. do not close the upgrade while unexplained drift remains in shared code

Expected result:
- there are no unexplained surviving differences
- the spec, validation checklist, inventory, and code comments describe the same final state

## What an agent should do automatically

An agent should automatically:

- diff the client branch against `develop-eyeseetea`
- classify conflicts by rule
- resolve obvious `ours` and obvious `theirs`
- update `upgrade-<version>-notes.md`
- keep the user informed at the end of each batch
- run the preclassification script before the easy batch when feasible

## What an agent should not do automatically

An agent should not automatically:

- merge Oslo directly into a client fork
- rewrite all shared conflicts by taking one side blindly
- treat `upgrade-<version>-notes.md` as a stable long-term source of truth
- remove business logic when the impact is unclear

## Decision rule for documentation

Use this test:

- if the information helps resolve the current merge and is temporary, put it in `upgrade-<version>-notes.md`
- if the information is a reusable merge rule, put it in `conflict-rules.md`
- if the information describes a confirmed customization that survives in the final code, put it in `customization-files.md`

## Minimal checklist for any future agent

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

## Customization Comment Checklist

When a customization survives the upgrade:

- use the exact title defined in `customization-specs.md`
- keep the same title in code comments, validation docs, and customization inventory
- do not invent local title variants for the same behavior
- if a customization is removed or absorbed by `develop-eyeseetea`, remove or update the obsolete title in the client docs
