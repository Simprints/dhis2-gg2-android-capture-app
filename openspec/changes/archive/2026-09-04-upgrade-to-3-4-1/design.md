## Context

See `proposal.md` - Why / What Changes for motivation and scope. This design
covers only the merge/conflict-resolution approach for moving the Simprints
fork's implementation baseline from `develop-eyeseetea` 3.3.1 to 3.4.1
(`938b819597`, PR #323).

Current state:
- Branch `feature-simprints/upgrade_3.4.1` (renamed from `upgrade_3.4`) is
  already checked out at the new baseline (`938b819597`), but the merge
  itself has not been started yet.
- A prior merge attempt against the intermediate 3.4 baseline
  (`73a7eb8f0f`) was aborted before commit, with no data loss — see
  `eyeseetea-docs/upgrade/simprints/upgrade-3.4-notes.md`.
- Onboarding Phases 1-5 are complete: 13 active customizations are formalized
  as `openspec/specs/<capability>/spec.md`, `customization-files.md` and
  `upgrade-validation-checklist.md` use the same canonical titles, and
  `CLAUDE.md` + the `.claude/` OpenSpec scaffolding are in place.
- `eyeseetea-docs/upgrade/conflict-rules.md` is the canonical, reusable
  merge-resolution guide this design defers to for classification mechanics.

## Goals / Non-Goals

**Goals:**
- Merge `develop-eyeseetea` 3.4.1 into the upgrade branch with all 13 active
  Simprints capabilities intact and behaviorally unchanged.
- Classify every file that conflicts, or that appears in
  `customization-files.md`, into one of the four `conflict-rules.md`
  categories before editing it.
- Catch silent automerge drops (baseline commits that delete customization
  wiring without a conflict marker) via the Automerge verification rule.
- Leave `customization-files.md`, the validation checklist, and
  `upgrade-3.4-notes.md` in a state a future agent can resume from without
  re-deriving context.

**Non-Goals:**
- Changing what any of the 13 capabilities do (see proposal.md - Capabilities;
  this change sets `skip_specs: true`).
- Reconciling the shared-drift build files (`app/build.gradle.kts`,
  `settings.gradle.kts`, `gradle/libs.versions.toml`) beyond what is required
  to compile against the new baseline — deeper cleanup of that drift, if
  needed, is a separate follow-up.
- Adding tests for the 13 capabilities (that is Phase 7 of
  `onboarding-fork-guide.md`, out of scope for this change).
- Updating the shared `develop-eyeseetea` baseline itself (e.g. the pending
  OpenSpec scaffolding version bump — tracked separately, only replayed
  against baseline after this upgrade proves the approach works).

## Decisions

**Classification order.** Process files in this order, per
`conflict-rules.md` "Mandatory post-merge preclassification":
1. Direct flavor files (`app/src/simprints/**`, `app/src/simprintsDebug/**`)
   → `accept_ours` by default rule, essentially zero risk.
2. Files explicitly listed in `customization-files.md` section 2 (13 active
   customizations, ~40+ shared files under `data/biometrics/`,
   `usescases/biometrics/`, `usescases/searchTrackEntity/`,
   `usescases/teiDashboard/`, `usescases/enrollment/`) → expect
   `manual_reapply_on_theirs` for most; run the Automerge verification rule
   (`git diff develop-eyeseetea -- <file>`) on every one of them regardless
   of conflict-marker status.
3. Files listed in `customization-files.md` section 3 ("Areas explicitly out
   of scope for preservation": `login/`, notifications, change server URL,
   granular sync wiring) → `accept_theirs`, confirm no Simprints-specific
   variant survives.
4. Files in `customization-files.md` section 4 (shared drift:
   `app/build.gradle.kts`, `settings.gradle.kts`,
   `gradle/libs.versions.toml`) → `defer_after_build_verification`,
   reconcile only as far as needed to compile against 3.4.1.
5. Any remaining conflicted file not in the inventory → classify fresh per
   `conflict-rules.md` "Default rules by path"; if a real customization
   surfaces here, it means `customization-files.md` was incomplete and must
   be updated (this is exactly the completeness gap `conflict-rules.md`
   itself warns about).

**Pause points.** Per `conflict-rules.md` step 7 and CLAUDE.md, resolve
`accept_ours`/`accept_theirs` files first and pause for user confirmation
before starting `manual_reapply_on_theirs` files — those concentrate in
biometrics/search/dashboard/enrollment, the highest-risk area of this fork.

**Model choice for the manual-reapply tranche.** Evaluate switching to a
higher-effort model (Opus 5) specifically for `manual_reapply_on_theirs`
resolution in the biometrics/search/sync/program areas, given the higher cost
of a wrong inference there versus the easy tranches — decided per-session,
not fixed here.

**No SDK fork patch dependency identified.** None of the 13 active
capabilities currently reference an EyeSeeTea SDK-fork-specific class or
error code in `customization-files.md`. If `manual_reapply_on_theirs`
resolution surfaces one, record the patch surface (classes / error codes)
here before proceeding, per the `design` rule in `openspec/config.yaml`.

**Residual baseline-drift risk stays open.** A gap identified in the prior
session (2026-08-06) is not addressed by this change: code that lives in
`develop-eyeseetea` and that Simprints depends on, without being documented
as a Simprints customization (because it looks generic), can be silently
removed by a future baseline change with no conflict. No detection mechanism
exists yet for this. Out of scope here; tracked as a follow-up decision.

## Risks / Trade-offs

- [Automerge silently drops customization wiring outside conflicted hunks] →
  Mitigated by running the Automerge verification rule against every file in
  `customization-files.md`, not only conflicted ones.
- [`customization-files.md` inventory is incomplete for some customization]
  → Mitigated by cross-checking `git show <feat-commit> --stat` against the
  inventory per capability before trusting it as complete (per
  `conflict-rules.md` "Inventory completeness is load-bearing").
- [Manual reapply on `manual_reapply_on_theirs` files reconstructs the wrong
  behavior from a weak inference] → Mitigated by the "expected delta" rule:
  define the expected 1-5-line delta before editing, stop and redo if the
  resulting diff is significantly larger.
- [Shared drift in build files masks a real flavor requirement as generic
  noise, or vice versa] → Mitigated by treating section 4 of
  `customization-files.md` as `defer_after_build_verification`, not
  `accept_theirs` — verify via successful build, not assumption.

## Migration Plan

1. Merge `develop-eyeseetea@938b819597` into
   `feature-simprints/upgrade_3.4.1`.
2. Preclassify all affected files per the order in Decisions; record in
   `eyeseetea-docs/upgrade/simprints/upgrade-3.4-notes.md` using the
   temporary table format from `conflict-rules.md`.
3. Resolve `accept_ours` / `accept_theirs` batches; pause for review.
4. Resolve `manual_reapply_on_theirs` batch; pause for review before each
   high-risk area (biometrics config/mode, search+duplicate flow, TEI
   dashboard/enrollment UI, data exchange/mapping).
5. Resolve `defer_after_build_verification` batch once the project builds.
6. Run the Automerge verification rule against every file in
   `customization-files.md`.
7. Validate against `upgrade-validation-checklist.md` (13 functional flows +
   the out-of-scope negative check).
8. Update `customization-files.md` with confirmed surviving customizations
   and close `upgrade-3.4-notes.md`.

Rollback: the merge is performed on a dedicated branch
(`feature-simprints/upgrade_3.4.1`); if it needs to be abandoned, `git merge
--abort` (pre-commit) or resetting the branch to its pre-merge commit
(post-commit, with explicit user confirmation per the destructive-action
policy) recovers the prior state. No production system is touched until the
branch is merged to a release branch, which is out of scope for this change.

## Open Questions

- Should the shared-drift build files (`app/build.gradle.kts`,
  `settings.gradle.kts`, `gradle/libs.versions.toml`) be reconciled fully
  during this upgrade, or only as far as needed to compile? Current default:
  minimum needed to compile (Non-Goals); revisit if section 4 turns out to
  hide a real customization once classified.
