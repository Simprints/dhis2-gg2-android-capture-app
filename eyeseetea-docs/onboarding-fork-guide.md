# Onboarding an Existing Fork Without Documentation

Use this guide when a client fork already exists and has customizations but does not have `eyeseetea-docs/` documentation yet.

Use `eyeseetea-docs/README.md` to understand the model first. Use this file as the onboarding playbook.

## When to use this guide

- the fork exists and has been in use
- the fork has undocumented customizations in shared code and flavor files
- the fork does not have `eyeseetea-docs/customizations/<client>/` or `eyeseetea-docs/upgrade/<client>/`
- the fork needs to be upgraded and the documentation is a prerequisite

## When not to use this guide

- you are creating a brand new fork from scratch: use `new-fork.md` instead
- the fork already has complete docs and you are doing an upgrade: use `upgrade-plan-client-forks.md` instead

## Preconditions

- `develop-eyeseetea` is the shared EyeSeeTea baseline
- the fork branch is identified and accessible
- the merge-base with `develop-eyeseetea` is known
- the developer has domain knowledge of which customizations are intentional

## Phase 1. Bring shared docs into the fork

Bring the shared `eyeseetea-docs/` structure from `develop-eyeseetea` into the client branch.

### What to bring

- `eyeseetea-docs/README.md`
- `eyeseetea-docs/SDK_Setup.md`
- `eyeseetea-docs/new-fork.md`
- `eyeseetea-docs/onboarding-fork-guide.md`
- `eyeseetea-docs/upgrade/upgrade-plan-client-forks.md`
- `eyeseetea-docs/upgrade/conflict-rules.md`
- `eyeseetea-docs/upgrade/template/`
- `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md`
- `eyeseetea-docs/customizations/template/`
- `eyeseetea-docs/scripts/`

### How

Cherry-pick, merge, or manually copy from `develop-eyeseetea`. Do not start creating client-specific docs until the shared structure is in place.

### Done when

- the shared docs and templates are present in the fork branch
- no client-specific docs have been created yet

## Phase 2. Create client documentation from templates

Copy the templates to the client-specific paths.

### Developer checklist

1. Copy `eyeseetea-docs/customizations/template/customization-specs-template.md` to `eyeseetea-docs/customizations/<client>/customization-specs.md`.
2. Copy `eyeseetea-docs/customizations/template/customization-files-template.md` to `eyeseetea-docs/customizations/<client>/customization-files.md`.
3. Copy `eyeseetea-docs/upgrade/template/upgrade-validation-checklist-template.md` to `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md`.
4. Fill the mandatory header in `customization-files.md` (client, flavor, base branch, base commit, date).

### Done when

- the three client docs exist with the mandatory header filled
- the content is still template placeholder, not populated yet

## Phase 3. Inventory customizations

This is the most important phase. It produces the functional spec and the technical inventory.

### What the developer does

1. List all known intentional customizations with a functional title and expected behavior.
2. Assign a lifecycle status to each: `active`, `needs_validation`, `absorbed`, or `removed`.
3. Identify the flavor surface: `app/src/<flavor>/` and `app/src/<flavor>Debug/`.
4. Confirm which shared-code diffs are real business customizations vs technical drift or leftovers.
5. Identify customizations that may have been removed or absorbed by `develop-eyeseetea`.

### What the AI agent can help with

- run `git diff develop-eyeseetea..<branch> --stat` to list all differing files
- separate flavor-specific files from shared-code diffs
- search for `EyeSeeTea customization` comments in code
- draft the initial `customization-files.md` from the diff
- flag files that differ but have no matching customization title
- identify potential leftover files from previous forks or clients

### What the AI agent must not do

- invent functional customization titles without developer confirmation
- assume every diff is a real business customization
- skip the developer review step
- move shared EyeSeeTea baseline behavior into client-specific docs

### Developer checklist

1. Populate `customization-specs.md` with the functional titles, intent, expected behavior, and status.
2. Populate `customization-files.md` with the technical inventory grouped by customization.
3. Populate `upgrade-validation-checklist.md` with manual validation flows per customization.
4. List files that still differ but have no confirmed customization title in section 3 of `customization-files.md`.

### Done when

- every known customization has a title, status, and expected behavior in `customization-specs.md`
- the technical inventory is in `customization-files.md`
- validation flows exist in the checklist
- unclassified diffs are visible in section 3, not hidden

## Phase 4. Formalize with OpenSpec (recommended)

This phase converts the eyeseetea-docs specs into Structured Specification Documents (SSD) that AI tools can consume more effectively.

OpenSpec does not replace eyeseetea-docs. It formalizes the specs in a machine-friendly format while eyeseetea-docs remains the human-readable source of truth for the upgrade workflow.

### How OpenSpec maps to eyeseetea-docs

| eyeseetea-docs | OpenSpec equivalent | Notes |
|---|---|---|
| `customization-specs.md` entry | `openspec/specs/<domain>/spec.md` | Each customization becomes a spec with MUST/SHALL requirements |
| Upgrade strategy | `openspec/changes/<name>/proposal.md` | The upgrade itself is a change with proposal, design, and tasks |
| `upgrade-validation-checklist.md` entry | Given-When-Then scenarios in spec.md | Testable scenarios that can generate test code |
| `conflict-rules.md` | `config.yaml` project context | Referenced as rules in OpenSpec config, not duplicated |
| `customization-files.md` | Referenced in spec.md | Technical inventory stays in eyeseetea-docs |

### Steps

1. Install OpenSpec CLI: `npm install -g @fission-ai/openspec@latest`
2. Initialize: `cd <project-root> && openspec init`
3. Configure `openspec/config.yaml` with project context:
   - reference `eyeseetea-docs/upgrade/conflict-rules.md` as merge rules
   - reference `develop-eyeseetea` as the baseline branch
   - add per-artifact rules for spec formatting (Given-When-Then scenarios)
4. Create one spec per active customization under `openspec/specs/<customization-slug>/spec.md`.
5. Each spec should include:
   - purpose (from `customization-specs.md` functional intent)
   - requirements using MUST/SHALL/SHOULD keywords
   - concrete Given-When-Then scenarios (from `upgrade-validation-checklist.md`)
6. Model the upgrade as a change: use `/opsx:propose upgrade-to-<version>` or create manually under `openspec/changes/upgrade-<version>/`.

### Spec format example

```markdown
# Server URL Change

## Purpose

Field users can switch the DHIS2 server URL from within the app settings.

## Requirements

### Requirement: Server URL setting

The app MUST provide a setting that allows the user to change the DHIS2 server URL.

The app MUST validate the new URL before applying the change.

The app MUST re-authenticate the user after the server URL changes.

#### Scenario: User changes server URL

- GIVEN the user is logged in and opens the settings screen
- WHEN the user selects the change server URL option
- AND enters a valid DHIS2 server URL
- THEN the app applies the new server URL
- AND the user is prompted to re-authenticate

#### Scenario: User enters an invalid URL

- GIVEN the user is in the change server URL dialog
- WHEN the user enters an invalid URL
- THEN the app shows a validation error
- AND the server URL is not changed
```

### Done when

- `openspec/` directory exists with config and specs
- each active customization has a spec with requirements and scenarios
- the upgrade is modeled as a change

## Phase 5. Set up Claude Code tooling

Configure Claude Code to assist with the upgrade process.

### CLAUDE.md

Create a `CLAUDE.md` at the repository root with:

- project identity: app name, fork name, flavor, baseline branch
- branch model: `develop-eyeseetea` is baseline, never merge Oslo directly
- key documentation paths: `eyeseetea-docs/README.md`, `conflict-rules.md`, client specs
- rules: same golden rules from `eyeseetea-docs/README.md`
- reference to `openspec/` if Phase 4 was done

### Agents (optional)

Create specialized agents under `.claude/agents/` if the team wants reusable automation:

- `classify-conflicts.md` — classifies conflicted files using `conflict-rules.md` rules
- `resolve-easy-conflicts.md` — resolves `accept_ours` and `accept_theirs` files automatically
- `inventory-customizations.md` — analyzes diff and updates `customization-files.md`

### Skills (optional)

If OpenSpec was set up, it generates `.claude/skills/` automatically via `openspec update`.

If not using OpenSpec, consider manual skills for common operations:

- `/upgrade-status` — shows current upgrade progress from `upgrade-<version>-notes.md`
- `/classify-conflicts` — runs conflict classification

### Done when

- `CLAUDE.md` exists and references the right docs
- a fresh Claude session can orient itself by reading `CLAUDE.md`

## Phase 6. Execute the upgrade

Follow `eyeseetea-docs/upgrade/upgrade-plan-client-forks.md` for the full upgrade runbook.

### Prerequisites from this guide

Before starting the upgrade:
- Phase 1-3 are complete (docs exist and customizations are inventoried)
- Phase 4-5 are recommended but not blocking

### Steps

1. Create `eyeseetea-docs/upgrade/<client>/upgrade-<version>-notes.md` from the template.
2. Merge `develop-eyeseetea` into the client branch.
3. Classify conflicts using `conflict-rules.md`.
4. Resolve easy conflicts first, pause for developer review.
5. Resolve manual conflicts by reapplying minimum client-specific logic.
6. Validate against the checklist.
7. Update `customization-files.md` with confirmed surviving customizations.

### Done when

- the upgrade is complete per the criteria in `upgrade-plan-client-forks.md`
- no unexplained shared drift remains

## Phase 7. Add tests for customizations

For each active customization, create tests that validate the expected behavior.

### Approach

1. Use the Given-When-Then scenarios from OpenSpec specs (Phase 4) or from `upgrade-validation-checklist.md` as the test specification.
2. Prefer unit tests for isolated business logic (presenters, repositories, use cases, mappers).
3. Use integration or UI tests for workflow-level behavior (dialogs, navigation, form rendering).
4. Follow the project's existing testing patterns and frameworks.

### Priority

Start with:
- customizations that have the largest code surface (highest risk of regression)
- customizations that touch shared code (most likely to break during upgrades)

### What the AI agent can help with

- generate test stubs from Given-When-Then scenarios
- identify the right test class and framework for each customization
- suggest which existing test patterns to follow

### What the AI agent must not do

- generate tests that pass trivially without validating real behavior
- skip developer review of generated test logic

### Done when

- each active customization has at least one targeted test
- tests validate the specific customization behavior, not generic functionality
- tests pass on the current branch

## Phase 8. Clean up

### Developer checklist

1. Remove files that belong to absorbed or removed customizations.
2. Remove leftover files from previous forks or clients that are not part of any active customization.
3. Confirm section 3 of `customization-files.md` is empty or contains only items with an explicit reason and next action.
4. Ensure code comments use exact titles from `customization-specs.md`: `// EyeSeeTea customization - [title]`.
5. Run `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client <client>` to validate consistency.

### Done when

- no unexplained shared drift remains
- code comments, specs, checklist, and inventory are aligned
- `check_upgrade_docs.py` passes

## Developer vs AI agent responsibility matrix

| Phase | Developer | AI agent |
|-------|-----------|----------|
| 1. Bring shared docs | decides how to bring docs (cherry-pick, merge) | can identify which files to bring |
| 2. Create client docs | copies templates, fills header | can automate the copy |
| 3. Inventory customizations | lists known customizations, confirms titles and status | analyzes diff, drafts inventory, flags unclassified diffs |
| 4. Formalize with OpenSpec | reviews and approves specs | generates spec drafts with requirements and scenarios |
| 5. Set up Claude tooling | reviews CLAUDE.md content | drafts CLAUDE.md and agent/skill files |
| 6. Execute upgrade | reviews conflicts, confirms business decisions | classifies and resolves easy conflicts, drafts notes |
| 7. Add tests | reviews test logic and coverage | generates test stubs from scenarios |
| 8. Clean up | confirms what to remove | identifies candidates for removal |

## Promotion rule

- if this guide or any of its referenced templates becomes useful for all forks, keep it in `develop-eyeseetea`
- if a document becomes client-specific during the process, keep it in the client branch
