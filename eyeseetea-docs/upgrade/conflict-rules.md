# Flavor Conflict Resolution Rules

Operational guide for resolving merge conflicts in the flavor branch after bringing changes from `develop-eyeseetea`.

This file is meant to be reusable by any agent or model in future sessions.

## Purpose

This document is **not** the canonical inventory of client customizations.

Its purpose is:
- define how conflicts should be resolved
- record which files are conflict-prone and why
- stay reusable across upgrades and clients

The canonical customization inventory must remain in:
- `eyeseetea-docs/customizations/<flavor>/customization-files.md`

## Document split

Use the documents with this responsibility split:

- `customization-files.md`
  Final-state inventory.
  Only keep confirmed `<flavor>` customizations that still exist on top of `develop-eyeseetea`.

- `conflict-rules.md`
  Working merge guide.
  Keep reusable rules here only.

- `upgrade-<version>-notes.md`
  Temporary upgrade notes.
  Keep version-specific progress, decisions, and unresolved questions there while the upgrade is active.

## Baseline

- Base branch for all client forks: `develop-eyeseetea`
Rule of record:

- `develop-eyeseetea` is the EyeSeeTea reference branch
- Oslo upgrades must be integrated into `develop-eyeseetea` first
- Client forks must be upgraded from `develop-eyeseetea`, not directly from Oslo

Never do this:

- merge Oslo directly into a client branch
- compare a client fork only against Oslo when `develop-eyeseetea` already contains the shared EyeSeeTea baseline

All conflict decisions should answer this question:

> What is the minimum flavor-specific logic that must survive on top of `develop-eyeseetea`?

## Core principles

1. Prefer `develop-eyeseetea` by default.
   If a change is not clearly flavor-specific, keep the base branch version.

2. Keep client customizations isolated when possible.
   Prefer flavor-specific code under `app/src/<flavor>/` or `app/src/<flavor>Debug/` over changes in shared `main` code.

3. Reapply minimal deltas, not whole old files.
   When a shared file conflicts, start from `develop-eyeseetea` and reinsert only the flavor-specific behavior.

4. Do not use conflict resolution as documentation.
   Temporary merge notes go to `upgrade-<version>-notes.md`; stable confirmed customizations go to `customization-files.md`.

5. If a customization is already absorbed by `develop-eyeseetea`, do not keep a duplicate client fork of it.

6. A future agent must be able to continue from the docs alone.
   Keep decisions explicit, short, and file-based.

7. When possible, isolate client custom code at the end of the file.
   If a customization can be extracted into helper functions, constants, mappers, or callbacks without hurting readability, place that custom block near the end of the file so it is easier to identify in future upgrades.

8. Do not force end-of-file extraction when the logic must stay inline.
   If the customization is naturally tied to a builder chain, Compose tree, model property, constructor parameter, or control-flow branch, keep it where it executes and add the required nearby comment there.

## What an agent should do automatically

An agent may do these steps without asking first:

- inspect git status, current branch, and diff against `develop-eyeseetea`
- classify files into:
  - direct flavor files
  - easy conflicts
  - manual conflicts
  - post-merge review files
- resolve obvious `accept_ours` files under `app/src/<flavor>/**` and `app/src/<flavor>Debug/**`
- resolve obvious `accept_theirs` files when they are clearly shared-base changes with no client logic
- update `customization-files.md` only when a customization is confirmed to survive on top of `develop-eyeseetea`
- when editing shared files, prefer moving separable flavor helpers or constants toward the end of the file

## What an agent should not do automatically

An agent must not do these things without explicit user approval:

- merge Oslo directly into client branch
- rewrite large groups of shared files just because `theirs` compiles
- remove shared custom code unless it is clearly obsolete or absorbed by `develop-eyeseetea`
- assume every conflict means a real customization
- assume every non-conflict shared diff is a real customization
- modify `eyeseetea-docs/customizations/eyeseetea/customizations-eyeseetea.md` while working on a client fork inventory
- treat temporary merge progress as final customization documentation
- move code to the end of a file if doing so makes the file harder to understand or changes the natural API structure

## Code placement convention for surviving customizations

Apply this convention when resolving shared-code conflicts:

- if a customization can live in a separate helper, mapper, callback, extension, or constant block, place it near the end of the file
- if a customization must remain inline to be readable or correct, leave it inline and add the required `// EyeSeeTea customization - [title]` comment there
- prefer small isolated custom helpers over scattering custom conditions across the file
- do not reorder large parts of the base file only to satisfy this convention

Typical good candidates for end-of-file placement:

- helper functions
- custom mappers
- extension functions
- constants tied only to flavor behavior
- callback implementations that can be isolated cleanly

Typical bad candidates for forced extraction:

- constructor parameters
- data model properties
- sealed class contracts
- inline UI wiring that is clearer at the call site
- logic whose extraction would make the base flow harder to follow

## File migration rule: Java to Kotlin

When the old flavor customization was made in a `.java` file but `develop-eyeseetea` now uses a `.kt` replacement, the agent must not assume the old Java conflict is the right place to keep working.

Required process:

- locate the current active replacement file in the new base branch
- verify whether the old Java file is still used, deprecated, duplicated, or superseded
- reimplement the surviving customization in the active Kotlin file if that is now the real implementation
- only then decide whether the old Java file should keep custom code, be resolved as `theirs`, or remain for manual review

Rules:

- if the active Kotlin replacement is clearly located and matches the same responsibility, reimplement the customization there and accept `theirs` in the old Java file
- this may be done automatically only when the Kotlin destination is clearly identified and the mapping is low-risk
- if the Kotlin replacement does not exist with a clear one-to-one mapping, or the new implementation is very different, the agent must not resolve it automatically
- in that case, classify the Java conflict as manual/supervised review and identify the likely Kotlin or replacement area where the customization must be reimplemented
- do not classify a Java-only conflict as a confirmed customization until the Kotlin replacement has been reviewed
- do not force a functional customization title onto a technical migration if no documented business behavior matches it
- if the migration is technical rather than functional, keep it in the technical inventory/rules, not in the functional spec

## Conflict minimization rule

Before editing a conflicted file, the agent should choose the smallest valid resolution strategy.

Preferred order:

1. keep `develop-eyeseetea` and reinsert only the few custom lines needed
2. resolve only the conflicting hunk and keep the rest of the file untouched
3. rewrite a local function or block if the conflict is structurally broken
4. rewrite a whole file only when the file is already corrupted by duplicated conflict content or cannot be safely merged hunk-by-hunk

Rules:

- do not rewrite a whole file just because a simpler merge is possible
- if the conflict is only a few lines, do not expand the scope to nearby unrelated code
- if the only evidence for a customization is a test expectation or a weak inference, stop and classify it as `needs_validation`
- when in doubt, prefer leaving the conflict for manual review over reconstructing behavior from assumptions

Expected delta rule:

- before editing, define the expected functional delta in concrete terms
- examples:
  - add one factory and one constructor parameter
  - replace one method call
  - change one test assertion
  - reinsert one helper call
- if the intended customization can be expressed in 1 to 5 small edits, do not perform a broad rewrite of the file

Stop and redo rule:

- after resolving a conflict, compare the clean diff with the expected functional delta
- if the clean diff is significantly larger than the intended customization, stop
- assume the merge is overchanged
- redo the file from `develop-eyeseetea`
- reapply only the minimum custom lines

Automerge verification rule:

Git automerge resolves hunks without conflicts silently. It can drop customization code that is not in a conflicting hunk — for example, a parameter added at the end of a function call when only the beginning of the file conflicts. An IDE three-way merge view shows all changes (conflicting and auto-resolved), but the CLI only shows conflict markers. Code comments (`// EyeSeeTea customization`) may also be missing from some insertion points, so they are not a reliable check.

**This rule applies to every file listed in `customization-files.md` after any merge of the baseline — not only files that git marked as conflicted.** If `develop-eyeseetea` contains commits that removed customization code (as happened in the WIDP 3.3.1 upgrade with commits like `31baf8306 Remove notifications customization`), git can apply those deletions as a clean automerge with no conflict markers, dropping entire customization wiring silently.

For every file in the customization inventory, verify the full delta — not just the conflicted hunks:

```bash
git diff develop-eyeseetea -- path/to/file
```

- the diff must contain ALL the customization lines for that file, whether or not git reported a conflict
- compare the diff against `customization-files.md` to check that every documented insertion point for that customization survived
- if the diff is smaller than expected (fewer customization lines than documented), the automerge silently dropped code — recover it before staging
- do not trust conflict markers as the complete picture of what changed; they only cover hunks where both sides touched the same lines

**Inventory completeness is load-bearing.** This rule only catches files that are listed in `customization-files.md`. If a customization's wiring is spread across files that the inventory never captured (e.g. `MainActivity`, `MainView`, `MainPresenter` for the Notifications system, missed in WIDP 3.3.1 post-merge), the rule cannot fire. Keep the inventory complete by deriving it from the feature commits:

```bash
# for each customization, list all files the original feature commit touched
git show <feat-commit-sha> --stat
```

Every file in that output must appear in `customization-files.md` under the corresponding section. `customization-files.md` should track the `Feat commits` SHAs for each customization so this check is reproducible.

This rule applies to both human and agent resolution. It is the CLI equivalent of reviewing the full three-way diff in an IDE.

Shared-file safety rule:

- for shared files such as `*Module.kt`, large tests, dependency wiring files, and files with formatting drift, resolve as `theirs` in structure
- reinsert only the exact flavor behavior
- do not accept collateral changes in imports, formatting, style, or unrelated APIs unless they are required to compile

Low-change conflict rule:

- if the conflict should result in only one to three real functional changes, prefer this sequence:
  1. restore the `develop-eyeseetea` version
  2. add only those exact lines
  3. review the final diff before staging
- do not "clean up", "modernize", or reformat the file during this kind of merge

## Recreate a conflict for manual resolution

If the merge is still open and a previously resolved file needs to be reviewed again manually, the original merge conflict for that file can be recreated with:

```bash
git checkout -m -- path/to/file
```

Example:

```bash
git checkout -m -- aggregates/src/commonMain/kotlin/org/dhis2/mobile/aggregates/ui/viewModel/DataSetTableViewModel.kt
```

Notes:
- this works while the merge is still in progress
- if the merge has already been committed, this will no longer recreate the original conflict state
- use this only for the specific file that needs manual re-resolution

## Comment convention for surviving customizations

Every surviving flavor customization in shared code should use this comment style:

```kotlin
// EyeSeeTea customization - [title]
// Base behavior: ...    // only when the customization changes base behavior
// <flavor> behavior: ...   // optional, only when useful
```

Rules:

- the first line with `EyeSeeTea customization - [title]` is mandatory
- add `Base behavior:` only when the customization replaces, restricts, or overrides behavior from `develop-eyeseetea`
- do not add `Base behavior:` when the customization only adds support and does not change the base behavior
- if present, keep `Base behavior:` short and updated on every merge
- add `<flavor> behavior:` only when the custom behavior is not obvious from the code itself
- do not keep large blocks of old base code commented out
- do not copy the original code verbatim into comments unless there is a temporary merge reason
- prefer a short behavior summary over commented-out code

Use this convention especially in:

- shared business logic branches
- helper functions extracted for flavor behavior
- places where `develop-eyeseetea` and flavor intentionally diverge
- areas that historically need review on each upgrade

It is usually not worth adding `Base behavior:` in:

- interface property declarations
- simple data model fields
- additive support code that does not change the base flow
- trivial wiring where the customization is already self-evident

## XML comment convention

For XML resources, use XML comments instead of code-style comments.

Preferred style:

```xml
<!-- EyeSeeTea customization - [title] -->
```

Optional extended style when the resource overrides base behavior:

```xml
<!-- EyeSeeTea customization - [title] -->
<!-- Base behavior: ... -->
<!-- <flavor> behavior: ... -->
```

Rules:

- use the exact same functional title list as in code comments
- for grouped strings or menu items, one comment above the whole related block is preferred over repeating the same comment on every line
- only add `Base behavior` in XML when the resource changes visible behavior from `develop-eyeseetea`
- for flavor-only branding resources, a generic group comment is enough
- do not add noisy comments to every single XML node if one block comment is clearer

## When the agent should stop and ask the user

The agent should pause and ask before continuing when:

- a shared-code conflict has two plausible business behaviors
- a conflict affects workflows the agent cannot validate from local context
- the resolution would remove a known flavor behavior from `main` code
- multiple files need the same business decision and that decision is product-specific
- the branch contains unexpected manual edits unrelated to the current merge

## Resolution categories

Each conflict should be classified into one of these categories.

### A. `accept_theirs`

Use when the file is not flavor-specific and the conflict comes from upstream/shared evolution.

Typical signals:
- no flavor business rule in the file
- no flavor hook tied to flavor
- only API migration, refactor, imports, formatting, or test adaptation
- the old flavor side only preserved older base behavior

Expected action:
- take `develop-eyeseetea`
- if needed, verify that no flavor customization is lost

### B. `accept_ours`

Use only when the file is clearly flavor-owned.

Typical signals:
- file under `app/src/<flavor>/`
- file under `app/src/<flavor>Debug/`
- file exists only to support flavor branding/resources

Expected action:
- keep flavor version
- only normalize if build or API changes require it

### C. `manual_reapply_on_theirs`

This is the most common class for shared-code conflicts.

Typical signals:
- file lives under `app/src/main/`, `commons`, `form`, `aggregates`, or `tracker`
- `develop-eyeseetea` has real new architecture/API changes
- flavor added business behavior in the same file

Expected action:
- start from `develop-eyeseetea`
- port only the flavor-specific logic
- prefer extracting to flavor hooks if feasible

### D. `defer_after_build_verification`

Use when the customization may already be obsolete or absorbed, but confidence is low.

Expected action:
- tentatively keep `develop-eyeseetea`
- verify with compilation/tests/manual path
- only reintroduce flavor code if behavior is missing

## Post-merge fork identity check

After merging `develop-eyeseetea` into a client branch (or after a revert-the-revert), the merge can silently overwrite or delete fork-specific configuration that was not in a conflicting hunk. Run these checks before proceeding to conflict classification or build verification.

### 1. Version and identity strings

Compare `gradle/libs.versions.toml` against the pre-merge fork version:

```bash
diff <(git show HEAD~1:gradle/libs.versions.toml) gradle/libs.versions.toml
```

Check that:
- `vName` still has the client fork name (e.g., `3.3.1-widp-fork-1`, not `3.3.1-eyeseetea-fork-1`)
- `vCode` is correct for the client release
- `dhis2sdk` points to the right SDK fork version if the client uses a patched SDK

### 2. Flavor source sets

Verify the client flavor source sets still exist:

```bash
ls -d app/src/<flavor>/ app/src/<flavor>Debug/ app/src/<flavor>Release/
```

`develop-eyeseetea` may have renamed source sets (e.g., `widp` → `eyeseetea`). The merge brings the new source sets but deletes the old ones. Both must coexist — restore the client source sets from the pre-merge commit if deleted:

```bash
git checkout HEAD~1 -- app/src/<flavor>/ app/src/<flavor>Debug/ app/src/<flavor>Release/
```

### 3. Dependencies

Compare the `[versions]` and `[libraries]` sections of `libs.versions.toml`:

```bash
diff <(git show HEAD~1:gradle/libs.versions.toml) gradle/libs.versions.toml
```

Check that:
- no dependency used by the client fork was removed (search for removed library aliases in `*.kts` and `*.kt` files)
- no dependency version was downgraded if the client fork patched it

Also check `build.gradle.kts` files in modules that contain client customizations:

```bash
diff <(git show HEAD~1:app/build.gradle.kts) app/build.gradle.kts
```

### 4. Build configuration

Check that `app/build.gradle.kts` still defines the client flavor:

```bash
grep -A5 '<flavor>' app/build.gradle.kts
```

If the flavor definition was removed or renamed, restore it.

### 5. Files that should not come from develop-eyeseetea

Some files are fork-specific and should never be overwritten by the baseline:
- `google-services.json` (Firebase config, per-client)
- Signing configurations
- CI/CD files specific to the client

If these were overwritten, restore from the pre-merge commit.

## Mandatory post-merge preclassification

Immediately after merging `develop-eyeseetea` into the client branch, do not start editing files blindly.

First classify every affected file into one of these buckets:

- direct flavor files
- conflicted files resolved as `accept_ours`
- conflicted files resolved as `accept_theirs`
- conflicted files resolved as `manual_reapply_on_theirs`
- shared non-conflict diffs that may still represent client behavior
- obsolete or absorbed differences

Minimum record per file:

- file path
- classification
- expected functional delta
- linked customization title if known
- status

Recommended temporary format in `upgrade-<version>-notes.md`:

```md
| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| path/to/file | manual_reapply_on_theirs | reinsert one helper call | Select UPG | pending | New base API changed constructor |
```

Why:

- it prevents easy conflicts from being mixed with surviving drift
- it exposes absorbed differences before they are documented as customizations
- it reduces broad rewrites by forcing an expected delta before editing

## Default rules by path

### Always `accept_ours`

- `app/src/<flavor>/**`
- `app/src/<flavor>Debug/**`

### Usually `accept_theirs`

- build/generated or build output
- formatting-only conflicts
- tests that only follow shared behavior and do not assert `<flavor>`-specific business logic

### Usually `manual_reapply_on_theirs`

- `app/src/main/java/org/dhis2/usescases/**`
- `app/src/main/java/org/dhis2/data/**`
- `commons/src/main/**`
- `form/src/main/**`
- `aggregates/src/**`
- `tracker/src/**`

### Usually `defer_after_build_verification`

- tests that only prove a client customization indirectly
- files where the old client delta may already be absorbed by `develop-eyeseetea`
- migrations from old Java implementation to new Kotlin implementation when the business delta is still unclear

## Known <flavor>-sensitive areas

When there is files conflict with conflict, assume manual review is needed unless proven otherwise.


## Easy conflict rules

These are the conflicts that an agent should usually resolve without asking:

### Easy `accept_ours`

- anything under `app/src/<flavor>/**`
- anything under `app/src/<flavor>Debug/**`
- flavor launcher icons, branding strings, and flavor-only resources

### Easy `accept_theirs`

- pure import reorder / formatting conflicts
- shared test updates that only follow refactors from `develop-eyeseetea`
- API migrations where the flavor side contains no business logic
- resources or translations unrelated to flavor behavior

### Easy `manual_reapply_on_theirs`

- shared files where the flavor side only adds a few lines with clearly marked custom behavior
- files with comments like `EyeSeeTea customization` where the new base structure from `develop-eyeseetea` should be kept

## Hard conflict rules

These should usually be reviewed file by file:

- org unit selection logic
- enrollment and event creation flows
- dataset/team change request logic
- form rendering and field visibility behavior
- program stage behavior
- search behavior that may affect flavor workflows

For these files, the safe default is:

1. keep `develop-eyeseetea` structure
2. port the minimum flavor delta
3. update `customization-file.md` only if the delta remains after merge

## Merge algorithm

For each conflicted file:

1. Identify whether the file is:
   - flavor-owned
   - shared but custom
   - shared and probably not custom

2. Compare both sides and decide one category:
   - `accept_theirs`
   - `accept_ours`
   - `manual_reapply_on_theirs`
   - `defer_after_build_verification`

3. If `manual_reapply_on_theirs`:
   - keep `develop-eyeseetea` structure/API
   - reinsert smallest flavor logic
   - add/update `// EyeSeeTea customization - ...` if the code remains in shared modules
   - do not stage the file until the resulting diff still matches the expected delta

4. After resolving:
   - if the customization is confirmed and still needed, add it to `customization-files.md`
   - if not needed anymore, do not record it there
   - if the file still differs but the business meaning is unclear, keep it out of the final inventory and mark it `needs_validation`

## Upgrade workflow

This is the expected process for any future upgrade of flavor or another client fork.

1. Upgrade Oslo into `develop-eyeseetea` first.
   Never upgrade a client branch directly from Oslo.

2. Validate `develop-eyeseetea`.
   It must represent the shared EyeSeeTea baseline for that Oslo version.

3. Create or update the client upgrade branch from the client fork.

4. Merge `develop-eyeseetea` into the client branch.

5. Classify differences:
   - direct flavor files
   - easy conflicts
   - manual conflicts
   - shared non-conflict diffs that may still be custom
   - obsolete or absorbed differences

6. Resolve easy conflicts using the rules in this file.

7. Stop and ask the user whether to continue after the easy batch, unless the user has explicitly asked for full autonomous resolution.

8. Resolve manual conflicts by reapplying the minimum client-specific behavior on top of `develop-eyeseetea`.

9. Update `customization-files.md` with confirmed surviving customizations only.

10. Run validation:
   - build if feasible
   - targeted tests if feasible
   - sanity review of key workflows

11. Produce a final summary:
   - resolved by `theirs`
   - resolved by `ours`
   - manually merged
   - still uncertain
   - still differing without confirmed customization title

## Temporary progress notes during an active merge

If a merge is currently in progress and you need short-lived tracking in this file, keep it minimal and delete it once the merge is stabilized.

Recommended temporary format:

```md
| File | Category | Expected delta | Customization | Status | Notes |
|------|----------|----------------|---------------|--------|-------|
| path/to/file | manual_reapply_on_theirs | Preserve one org-unit restriction | Validate or hide orgunit by Teamprofile | pending | Constructor migrated in base branch |
```

Allowed status values:
- `pending`
- `resolved_keep_theirs`
- `resolved_keep_ours`
- `resolved_manual_merge`
- `needs_validation`

Rules:
- do not keep branch-specific snapshots here once the merge is complete
- do not treat temporary tracking as the final customization inventory
- if a customization survives, move the stable result to `customizations-files.md`
- if a file reaches the end of the upgrade without a confirmed customization title, it must not be silently dropped from the notes

## Comment labeling reminder during merge

When a surviving flavor customization remains in shared code:
- add one nearby `EyeSeeTea customization - [Title]` comment
- use the exact functional title from the top-level `#` heading of the matching `openspec/specs/<capability>/spec.md`
- do not add these comments blindly before the final surviving logic is clear
