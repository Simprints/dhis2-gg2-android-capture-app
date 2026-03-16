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
3. confirm the `Baseline ready in dev` gate
4. merge `develop-eyeseetea` into the client fork
5. classify the surviving diff immediately after the merge
6. resolve easy conflicts using the rules file
7. pause for review if needed
8. resolve manual shared-code conflicts by reapplying only the minimum client logic
9. mark surviving customizations in code with the exact EyeSeeTea comment title
10. validate each surviving customization with the client validation checklist
11. reconcile the final diff against `develop-eyeseetea`
12. confirm the `Client upgrade done` gate

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
- temporary notes for one concrete upgrade

Typical examples:

- `eyeseetea-docs/customizations/<client>/customization-files.md`
- `eyeseetea-docs/customizations/<client>/customization-specs.md`
- `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md`
- `eyeseetea-docs/upgrade/<client>/upgrade-<version>-notes.md`

Decision rule:

- if a guide, rule, template, or checklist can be reused as-is by any future fork and no longer names one client specifically, it belongs in `develop-eyeseetea`
- if a file describes behavior, validation, inventory, or upgrade notes for one concrete client, it stays in the client branch

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

- `eyeseetea-docs/customizations/<client>/customization-specs.md`
  Stable source of truth for functional titles, intent, and lifecycle status.

- `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md`
  Stable manual validation flows for that client.

- `eyeseetea-docs/upgrade/conflict-rules.md`
  Merge conflict rules and decision criteria.

- `eyeseetea-docs/upgrade/<client>/upgrade-<version>-notes.md`
  Temporary progress, decisions, and unresolved questions for the current upgrade only.

## Formal gates

### Baseline ready in dev

Do not start the client merge until all of these are true:

- the target baseline compiles if building is viable in the current environment
- at least one smoke validation or one critical functional validation has been run
- `customizations/eyeseetea/customizations-eyeseetea.md` matches the real shared baseline state
- shared rules and templates are updated if the new baseline changed the upgrade flow

### Client upgrade done

Do not close the client upgrade until all of these are true:

- the final diff against `develop-eyeseetea` has no unexplained drift
- every documented customization is in one of these states: `active`, `absorbed`, `removed`, `needs_validation`
- the client manual checklist has been reviewed
- `upgrade-<version>-notes.md` has been archived or removed

## Standard workflow

### Phase 1. Prepare the baseline

1. Bring the target Oslo version into `develop-eyeseetea`.
2. Resolve shared EyeSeeTea changes there.
3. Validate that `develop-eyeseetea` is the intended new baseline.
4. Reconcile `customizations/eyeseetea/customizations-eyeseetea.md`.
5. Update shared rules/templates if the flow changed.

Expected result:
- `develop-eyeseetea` contains Oslo + shared EyeSeeTea changes only
- the `Baseline ready in dev` gate can be checked explicitly

### Phase 2. Start the client upgrade

1. Create or checkout the client upgrade branch.
2. Merge `develop-eyeseetea` into the client branch.
3. Do not merge Oslo directly into the client branch.

Expected result:
- the client branch now only needs client-specific conflict resolution

### Phase 3. Classify the diff right after the baseline merge

Immediately after merging `develop-eyeseetea`, classify the surviving diff into:

- `direct flavor files`
- `shared confirmed customizations`
- `shared drift not yet classified`
- `obsolete or absorbed differences`

Operational rule:

- nothing goes into `customization-files.md` unless it is confirmed
- nothing goes into `customization-specs.md` unless it represents real functional behavior
- everything temporary goes into `upgrade-<version>-notes.md`

Expected result:
- the team knows which differences are real client behavior and which still need review

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
8. document the surviving customization in `customization-files.md` only when the customization is confirmed
9. move temporary observations or pending classifications to `upgrade-<version>-notes.md`

Expected result:
- the client branch keeps only intentional custom behavior
- separable custom code is easier to identify in future upgrades

### Phase 7. Validate

1. run the build if feasible
2. run targeted tests if feasible
3. inspect critical workflows manually if needed
4. update customization states when a behavior was absorbed, removed, or still needs validation

Typical areas to validate:
- login
- enrollment
- event creation
- org unit selection
- dataset / team-change flows
- TEI dashboard

### Phase 8. Finalize documentation

1. keep `customization-specs.md` as the stable functional source of truth
2. keep `customization-files.md` as the final stable technical inventory
3. keep `conflict-rules.md` as stable merge guidance
4. keep `upgrade-validation-checklist.md` as the stable manual validation reference
5. keep `upgrade-<version>-notes.md` as temporary upgrade notes only, then archive/remove it when the upgrade is complete

### Phase 9. Final reconciliation

Before considering the upgrade complete:

1. compare the surviving diff against `develop-eyeseetea`
2. identify any remaining differences not linked to a known customization title
3. either document those differences or remove them
4. confirm that each customization in the functional spec is:
   - still present and validated
   - or explicitly marked as `absorbed`, `removed`, or `needs_validation`
5. confirm that `Shared drift still differing` is empty or contains only short-lived entries with a reason

Expected result:
- there are no unexplained surviving differences
- the spec, validation checklist, inventory, and code comments describe the same final state
- the `Client upgrade done` gate can be checked explicitly

## What an agent should do automatically

An agent should automatically:

- diff the client branch against `develop-eyeseetea`
- classify conflicts by rule
- resolve obvious `ours` and obvious `theirs`
- update `upgrade-<version>-notes.md`
- keep the user informed at the end of each batch
- run the lightweight doc consistency checks before closing the upgrade when feasible

## What an agent should not do automatically

An agent should not automatically:

- merge Oslo directly into a client fork
- rewrite all shared conflicts by taking one side blindly
- treat `upgrade-<version>-notes.md` as a stable long-term source of truth
- remove business logic when the impact is unclear
- mark drift as confirmed customization without classification

## Decision rule for documentation

Use this test:

- if the information helps resolve the current merge and is temporary, put it in `upgrade-<version>-notes.md`
- if the information is a reusable merge rule, put it in `conflict-rules.md`
- if the information describes confirmed client behavior, keep the intent in `customization-specs.md`
- if the information describes where a confirmed customization lives in code, keep it in `customization-files.md`
- if the information describes how to verify a customization manually, keep it in `upgrade-validation-checklist.md`

## Minimal checklist for any future agent

1. Confirm current branch and base branch.
2. Confirm that the upgrade source is `develop-eyeseetea`.
3. Read `conflict-rules.md`.
4. Confirm the `Baseline ready in dev` gate.
5. Merge the baseline and classify the surviving diff immediately.
6. Resolve easy conflicts first.
7. Pause for user review after the easy batch unless instructed otherwise.
8. Resolve manual conflicts by reapplying minimal client logic.
9. Ensure each surviving customization has a nearby comment:
   `// EyeSeeTea customization - [title]`
10. When possible, keep separable custom helpers grouped near the end of the file.
11. Update `customization-files.md` only with confirmed surviving customizations.
12. Run the doc consistency checks before closing.
13. Confirm the `Client upgrade done` gate.

## Customization Comment Checklist

When a customization survives the upgrade:

- use the exact title defined in `customization-specs.md`
- keep the same title in code comments, validation docs, and customization inventory
- do not invent local title variants for the same behavior
- if a customization is removed or absorbed by `develop-eyeseetea`, remove or update the obsolete title in the client docs
