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

## Customization code placement rule

Every line changed in an Oslo file is a future merge conflict. To minimize upgrade pain, always place customization code as far from the original Oslo code as possible.

Preference order (highest to lowest):

1. **Flavor source set** (`app/src/<flavor>/java/...`) — zero conflict risk, maximum isolation
2. **New file in shared code** (`app/src/main/java/...` in a new file) — separate from Oslo files
3. **Same file, block at the end** — helpers, constants, extensions grouped at the bottom of the file
4. **Inline in existing Oslo code** — last resort only

### Customization comment rule

**Every piece of customization code must have a comment** `// EyeSeeTea customization - [title]` regardless of where it is placed. This applies to all placement levels:

- **Flavor source set**: comment at the top of the file (below the package declaration)
- **New file in shared code**: comment at the top of the file (below the package declaration)
- **Block at the end of an existing file**: comment before the block
- **Inline in existing Oslo code**: comment on the line immediately above the changed code

Rules:
- use the **exact functional title** from `customization-specs.md` — the comment and the spec title must match
- do NOT place comments on import lines — the Oslo GitHub action validates imports and will reject them
- the same principle applies to resources: prefer flavor resource overrides over modifying shared resources
- when reviewing existing customizations during upgrades, consider refactoring inline code toward a higher preference level if feasible
- do not refactor placement during an active merge — do it as a separate follow-up

Why this matters:
- AI agents use these comments to identify customization boundaries during conflict resolution
- developers can quickly find all code belonging to a customization by searching `// EyeSeeTea customization - [title]`
- during upgrades, the comment makes it clear which code must be preserved vs which is upstream

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
- `eyeseetea-docs/templates/` — CLAUDE.md and openspec/config.yaml templates used in Phase 4 / Phase 5
- `eyeseetea-docs/scripts/`
- `.claude/` — generic Claude Code scaffolding (`commands/opsx/*`, `skills/openspec-*/`, `settings.json`). Do **not** bring `.claude/settings.local.json` (per-developer overrides, gitignored).

### How

Cherry-pick, merge, or manually copy from `develop-eyeseetea`. Do not start creating client-specific docs until the shared structure is in place.

### Done when

- the shared docs and templates are present in the fork branch
- no client-specific docs have been created yet

## Phase 2. Create client documentation from templates

Copy the templates to the client-specific paths.

### Developer checklist

1. Copy `eyeseetea-docs/customizations/template/customization-files-template.md` to `eyeseetea-docs/customizations/<client>/customization-files.md`.
2. Copy `eyeseetea-docs/upgrade/template/upgrade-validation-checklist-template.md` to `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md`.
3. **Optional but recommended for brownfield onboarding**: copy `eyeseetea-docs/customizations/template/customization-specs-template.md` to `eyeseetea-docs/customizations/<client>/customization-specs.md`. This file is an **intermediate draft** used in Phase 3 to dump customizations in narrative form before learning OpenSpec syntax. It is **deleted at the end of Phase 4** once the functional content has been moved into `openspec/specs/`. New greenfield forks can skip this file and go straight to OpenSpec in Phase 4.
4. Fill the mandatory header in `customization-files.md` (client, flavor, base branch, base commit, date).

### Done when

- `customization-files.md` and `upgrade-validation-checklist.md` exist with the mandatory header filled
- if this is a brownfield onboarding, `customization-specs.md` also exists as a draft placeholder (to be deleted in Phase 4)
- the content is still template placeholder, not populated yet

## Phase 3. Inventory customizations (narrative draft)

This is the most important phase. It produces a **verified list of customizations** in narrative form — readable by humans, cheap to iterate, and reviewable with the team before any formalization.

Why narrative first: installing OpenSpec and learning its syntax adds friction to a phase that should be about *discovery and confirmation*, not tooling. Writing a short markdown narrative is the fastest way to dump, refine, and review the list with the developer who knows the fork. The narrative draft becomes direct input for Phase 4 and is deleted there.

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

1. Populate `customization-specs.md` with the functional titles, intent, expected behavior, and status. **This file is a temporary draft**, not a stable artifact — it will be deleted in Phase 4.
2. Populate `customization-files.md` with the technical inventory grouped by customization.
3. Populate `upgrade-validation-checklist.md` with manual validation flows per customization.
4. List files that still differ but have no confirmed customization title in section 3 of `customization-files.md`.
5. Review the narrative draft with the developer who owns the fork. Do not move to Phase 4 until the list of customizations is confirmed stable.

### Done when

- every known customization has a title, status, and expected behavior in `customization-specs.md`
- the narrative draft has been reviewed and signed off by a developer who knows the fork
- the technical inventory is in `customization-files.md`
- validation flows exist in the checklist
- unclassified diffs are visible in section 3, not hidden
- every customization file has a matching `// EyeSeeTea customization - [title]` comment

## Phase 4. Formalize with OpenSpec and retire the narrative draft

This phase converts the narrative `customization-specs.md` draft from Phase 3 into the **functional source of truth**: one OpenSpec spec per active customization. At the end of this phase, `customization-specs.md` is **deleted** — its content has been absorbed into `openspec/specs/` with stricter structure (SHALL/MUST + WHEN/THEN scenarios).

eyeseetea-docs remains the home for technical inventory, manual QA, conflict rules, and upgrade runbooks. The *functional contract* (what the app must do for the client, with normative requirements and testable scenarios) lives only in OpenSpec.

### How the fork documentation is split

| Concern | Lives in |
|---|---|
| Functional meaning, titles, SHALL/MUST requirements, WHEN/THEN scenarios | `openspec/specs/<capability>/spec.md` |
| Technical inventory of files, line anchors, per-file notes | `eyeseetea-docs/customizations/<client>/customization-files.md` |
| Manual QA flows | `eyeseetea-docs/upgrade/<client>/upgrade-validation-checklist.md` |
| Reusable merge/conflict rules | `eyeseetea-docs/upgrade/conflict-rules.md` |
| Stable upgrade strategy | `eyeseetea-docs/upgrade/<client>/upgrade-<version>-strategy.md` |
| Upgrade execution (one-off, created in Phase 6, not here) | `openspec/changes/upgrade-<version>/` + `eyeseetea-docs/upgrade/<client>/upgrade-<version>-notes.md` |

This phase only installs OpenSpec and migrates the current customization specs. The upgrade proposal itself is **not** created here — it is the first step of Phase 6 (Execute the upgrade).

### Steps

1. Install OpenSpec CLI: `npm install -g @fission-ai/openspec@latest` (requires Node 20.19.0+).
2. The Claude Code scaffolding (`.claude/commands/opsx/*`, `.claude/skills/openspec-*`, `.claude/settings.json`) is already inherited from `develop-eyeseetea` in Phase 1 — **do not run `openspec init --tools claude`** (it would overwrite the baseline-provided files). Just create empty `openspec/specs/` and `openspec/changes/` directories at the repo root.
3. Copy `eyeseetea-docs/templates/openspec-config.yaml.template` to `openspec/config.yaml` and fill in the placeholders (`{{CLIENT_NAME}}`, `{{FLAVOR}}`, `{{APPLICATION_ID}}`, etc.). The template already contains the EyeSeeTea-wide `rules:` section (proposal/specs/design/tasks conventions); only the `context:` block needs fork-specific values. Do **not** create an `openspec/project.md` file — that name is legacy in OpenSpec ≥1.2.0 and triggers a migration warning. All project context goes into `config.yaml`.
4. Convert each section of the Phase 3 `customization-specs.md` narrative draft into one `openspec/specs/<capability>/spec.md`:
   - folder name is kebab-case (e.g. `change-server-url`)
   - top-level `# heading` is the **human title** from the draft (e.g. `# Change Server URL`). This title is the string that MUST appear in code comments as `// EyeSeeTea customization - Change Server URL` and as the section heading in `customization-files.md`.
   - one `## Purpose` section describing why the customization exists for the client (reuse the "Functional intent" prose from the draft)
   - one `## Requirements` section with one `### Requirement: <name>` per rule, each containing at least one `#### Scenario:` with `- **WHEN** …` / `- **THEN** …`. Reuse the "Expected behavior" bullets from the draft as seed material.
   - use SHALL/MUST (normative) — avoid should/may
5. Run `openspec validate --specs` (and `--strict` in CI) before committing. Fix any structural errors.
6. **Delete `eyeseetea-docs/customizations/<client>/customization-specs.md`**. Its content now lives in `openspec/specs/`. Leaving both files in the repo creates two sources of truth that will drift.

The upgrade proposal (`openspec/changes/upgrade-<version>/`) is **not** created in this phase — it is the first step of Phase 6 (Execute the upgrade), once Claude tooling (Phase 5) is in place.

### Spec format example

```markdown
# Change Server URL

## Purpose

Field users need to switch between different DHIS2 server instances from within
the installed app, without reinstalling, clearing data, or reconfiguring the
device. The stock DHIS2 Android client does not expose this capability; it is
contributed here for the `<client>` flavor.

## Requirements

### Requirement: Settings entry to change the server URL
The app SHALL expose an explicit "Change server URL" action inside the settings
menu of the authenticated user.

#### Scenario: Menu entry is visible when logged in
- **WHEN** an authenticated user opens the settings menu
- **THEN** the menu shows a "Change server URL" option

### Requirement: Confirmation warning before applying the change
The app SHALL show an explicit warning dialog before applying the new server URL.

#### Scenario: User confirms the change
- **WHEN** the user clicks "Accept" on the warning dialog
- **THEN** the app proceeds to switch the server URL
```

### Done when

- `openspec/config.yaml` exists with `schema`, `context` and per-artifact `rules`
- every active customization from Phase 3 has a spec under `openspec/specs/<capability>/spec.md`
- `openspec validate --specs` passes with 0 failures
- **`customization-specs.md` has been deleted** (it was only a narrative draft for Phase 3)
- code comments in shared files use the exact `# heading` of the matching spec

## Phase 5. Set up Claude Code tooling

Configure Claude Code to assist with the upgrade process.

### What is already in the baseline

When you bring `develop-eyeseetea` into the fork (Phase 1), you inherit:

- `.claude/settings.json` — generic permissions for `./gradlew *`, `openspec *`, common `git *` and reads under `eyeseetea-docs/**` and `openspec/**`
- `.claude/commands/opsx/*.md` — 4 OpenSpec commands (`/opsx:explore`, `/opsx:propose`, `/opsx:apply`, `/opsx:archive`)
- `.claude/skills/openspec-*/SKILL.md` — 4 OpenSpec skills covering the same workflow

These are generic and ready to use. **No action needed unless you want to extend them for your fork.**

### CLAUDE.md (required)

Copy `eyeseetea-docs/templates/CLAUDE.md.template` to `CLAUDE.md` at the repository root and fill in the placeholders (`{{CLIENT_NAME}}`, `{{FLAVOR}}`, `{{APPLICATION_ID}}`, `{{CURRENT_VERSION}}`, etc.). The template already includes the EyeSeeTea-wide rules (placement hierarchy, comment convention, automerge verification, post-merge check hierarchy, automation extraction rule); your customizations table and identity are the only fork-specific parts to fill.

### Agents (optional, on demand)

Do not create agents speculatively. Wait until a repetitive pattern emerges (3+ identical task structures during the upgrade) and then extract per the **Automation extraction rule** in `CLAUDE.md`. Examples that may surface:

- `classify-conflicts.md` — classifies conflicted files using `conflict-rules.md`
- `resolve-easy-conflicts.md` — resolves `accept_ours` / `accept_theirs` files automatically
- `inventory-customizations.md` — analyzes diff and updates `customization-files.md`

### Additional skills (optional)

Beyond the 4 OpenSpec skills already in the baseline, you may add fork-specific skills under `.claude/skills/` if a need emerges (e.g., `/upgrade-status` reading `upgrade-<version>-notes.md`).

### Done when

- `CLAUDE.md` exists and references the right docs
- a fresh Claude session can orient itself by reading `CLAUDE.md`

## Phase 6. Execute the upgrade

Follow `eyeseetea-docs/upgrade/upgrade-plan-client-forks.md` for the full upgrade runbook.

### Prerequisites from this guide

Before starting the upgrade:
- Phases 1-5 are complete (docs exist, customizations are inventoried, OpenSpec is installed with all current specs migrated, Claude Code tooling is configured)

### Steps

1. **Create the upgrade proposal as an OpenSpec change** under `openspec/changes/upgrade-<version>/`. Use `/opsx:propose upgrade-to-<version>` (preferred, interactive) or create the directory manually with `proposal.md`, `design.md`, and `tasks.md`. The proposal lists the upstream target version, the expected conflict surface, and the ordered task breakdown that mirrors the phases of `eyeseetea-docs/upgrade/<client>/upgrade-<version>-strategy.md`. This is the **first action of the upgrade**, before any merge.
2. Create `eyeseetea-docs/upgrade/<client>/upgrade-<version>-notes.md` from the template (for temporary progress and conflict notes).
3. Merge `develop-eyeseetea` into the client branch.
4. Classify conflicts using `conflict-rules.md`.
5. Resolve easy conflicts first, pause for developer review.
6. Resolve manual conflicts by reapplying minimum client-specific logic.
7. Validate against the checklist.
8. Update `customization-files.md` with confirmed surviving customizations.

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
4. Ensure code comments use exact titles from `openspec/specs/<capability>/spec.md` top-level `#` heading: `// EyeSeeTea customization - [Title]`.
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
| 6. Execute upgrade | reviews the upgrade proposal, reviews conflicts, confirms business decisions | drafts the upgrade proposal (`/opsx:propose`), classifies and resolves easy conflicts, drafts notes |
| 7. Add tests | reviews test logic and coverage | generates test stubs from scenarios |
| 8. Clean up | confirms what to remove | identifies candidates for removal |

## Promotion rule

- if this guide or any of its referenced templates becomes useful for all forks, keep it in `develop-eyeseetea`
- if a document becomes client-specific during the process, keep it in the client branch
