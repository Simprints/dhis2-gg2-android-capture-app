## Context

See `proposal.md` - Why / What Changes for motivation and scope. This design
covers only the merge/conflict-resolution approach for moving the Simprints
fork's implementation baseline from `develop-eyeseetea` 3.4.1 to 3.4.2
(`f87bec8c3`).

Current state:
- Branch `feature-simprints/upgrade_3.4.2` is checked out at the same commit
  as `feature-simprints/upgrade_3.4.1` (HEAD) — the merge has not started.
- The 3.4.1 upgrade is closed and archived
  (`openspec/changes/archive/2026-09-04-upgrade-to-3-4-1/`, PR #332). All 13
  active customizations are currently validated against 3.4.1.
- `develop-eyeseetea` advanced from the 3.4.1 merge-base to 3.4.2 via 9 Oslo
  commits (`origin/upstream/3.4.1..origin/upstream/3.4.2`) plus 5 EyeSeeTea
  PRs (#326 Oslo 3.4.2 merge, #328 `PostMetadataSyncAction` + cleanup, #329/
  #330 `AGENTS-<client>.md` split and rename, #331 Play Store update flow for
  `eyeseetea`). Three of these PRs are the baseline promotion of findings
  from the *previous* Simprints upgrade — see `eyeseetea-docs/upgrade/simprints/upgrade-3.4.2-notes.md`'s
  "Inherited context" table for the full B1-B6 status.
- `eyeseetea-docs/upgrade/conflict-rules.md` is the canonical, reusable
  merge-resolution guide this design defers to for classification mechanics.

## Goals / Non-Goals

**Goals:**
- Merge `develop-eyeseetea` 3.4.2 into the upgrade branch with all 13 active
  Simprints capabilities intact and behaviorally unchanged.
- Classify every file that conflicts, or that appears in
  `customization-files.md`, into one of the four `conflict-rules.md`
  categories before editing it.
- Specifically re-verify the two 3.4.1-era findings that landed in baseline
  since: `PostMetadataSyncAction` (fixes the accepted metadata-sync
  regression) and the `AGENTS-<client>.md` import convention.
- Catch silent automerge drops via the Automerge verification rule, same as
  every prior upgrade.
- Leave `customization-files.md`, the validation checklist, and
  `upgrade-3.4.2-notes.md` in a state a future agent can resume from without
  re-deriving context.

**Non-Goals:**
- Changing what any of the 13 capabilities do (see proposal.md - Capabilities;
  this change sets `skip_specs: true`).
- Picking up the still-pending baseline-promotion items (B2 "Feat commits"
  template section, B3 OpenSpec CLI scaffolding bump, B6 `fieldListChannel`
  race root-cause fix) — these remain tracked in `upgrade-3.4.2-notes.md` as
  inherited context, not as work this change commits to doing.
- Picking up any item from `eyeseetea-docs/customizations/simprints/refactors-pending.md`
  — that file has its own review cadence, independent of this upgrade.
- Reconciling shared-drift build files beyond what is required to compile.

## Decisions

**Classification order.** Same order as the 3.4.1 upgrade, per
`conflict-rules.md` "Mandatory post-merge preclassification":
1. Direct flavor files (`app/src/simprints/**`, `app/src/simprintsDebug/**`)
   → `accept_ours` by default, near-zero risk. One flavor file needs a
   content check regardless of conflict status:
   `app/src/simprints/.../PostMetadataSyncModule.kt` — confirm it still
   compiles against baseline's now-stable `PostMetadataSyncAction` contract.
2. Files explicitly listed in `customization-files.md` section 2 (13 active
   customizations) → expect mostly `manual_reapply_on_theirs`, but a much
   smaller diff than 3.4.1 since the Oslo gap is 9 commits, not a minor
   version. Priority check: `dd88b0b51` ("use aggregated syncState for sync
   button in search list", `ANDROAPP-7682`) touches the search/list area —
   verify it doesn't collide with the `SearchTEIViewModel`/`SearchTeiModel`
   customization surface.
3. Files in section 3 ("out of scope for preservation") → `accept_theirs`,
   confirm no Simprints-specific variant survives — unchanged from 3.4.1.
4. Files in section 4 (shared drift: `app/build.gradle.kts`,
   `settings.gradle.kts`, `gradle/libs.versions.toml`) →
   `defer_after_build_verification`. Note the SDK bump to `1.14.2-SNAPSHOT`
   in this Oslo range (`643e66591`) — check it doesn't conflict with the
   EyeSeeTea SDK-fork composite-build substitution.
5. `.claude/skills/openspec-*` and `.claude/commands/opsx/*` — expect the
   same `AA` add/add conflicts as 3.4.1 if baseline still hasn't bumped past
   CLI `1.2.0` (confirmed still the case as of `f87bec8c3`); resolve
   `accept_ours`, same precedent.
6. Any remaining conflicted file not in the inventory → classify fresh per
   `conflict-rules.md` "Default rules by path"; update `customization-files.md`
   if a real customization surfaces here.

**Pause points.** Resolve `accept_ours`/`accept_theirs` first and pause for
user confirmation before `manual_reapply_on_theirs`, same as every prior
upgrade.

**No new SDK fork patch dependency expected.** The 3.4.1 upgrade found none
for the 13 capabilities; the only SDK-relevant change in this range is the
version bump itself, not a new patch surface. Revisit if conflict resolution
says otherwise.

## Risks / Trade-offs

- [Automerge silently drops customization wiring outside conflicted hunks] →
  Mitigated by running the Automerge verification rule against every file in
  `customization-files.md`, not only conflicted ones — same mitigation that
  caught 4 silent casualties in the 3.4.1 upgrade.
- [`PostMetadataSyncAction` contract changed shape since it was promoted] →
  Mitigated by explicitly re-checking Simprints' registration module against
  baseline's current signature before assuming it still compiles.
- [The `dd88b0b51` search-list sync-state change interacts badly with the
  biometric search/duplicate-review surface] → Mitigated by treating it as a
  named priority check in classification step 2, not leaving it to be found
  incidentally during conflict resolution.
- [Manual reapply reconstructs the wrong behavior from a weak inference] →
  Mitigated by the same "expected delta" rule from 3.4.1: define the expected
  delta before editing, stop and redo if the resulting diff is significantly
  larger.

## Migration Plan

1. Merge `develop-eyeseetea@f87bec8c3` into `feature-simprints/upgrade_3.4.2`.
2. Preclassify all affected files per the order in Decisions; record in
   `eyeseetea-docs/upgrade/simprints/upgrade-3.4.2-notes.md`.
3. Resolve `accept_ours` / `accept_theirs` batches; pause for review.
4. Resolve `manual_reapply_on_theirs` batch, with the `dd88b0b51` priority
   check done first; pause for review.
5. Resolve `defer_after_build_verification` batch once the project builds.
6. Run the Automerge verification rule against every file in
   `customization-files.md`.
7. Validate against `upgrade-validation-checklist.md`.
8. Update `customization-files.md` with confirmed surviving customizations
   and close `upgrade-3.4.2-notes.md`.

Rollback: the merge is performed on a dedicated branch
(`feature-simprints/upgrade_3.4.2`); if it needs to be abandoned, `git merge
--abort` (pre-commit) or resetting the branch to its pre-merge commit
(post-commit, with explicit user confirmation per the destructive-action
policy) recovers the prior state. No production system is touched until the
branch is merged to a release branch, which is out of scope for this change.
