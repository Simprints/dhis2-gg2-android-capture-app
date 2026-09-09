# Simprints upgrade 3.4.2 notes

Use this file as the temporary working notes for the Simprints upgrade to 3.4.2.

## Purpose

This file is for:
- temporary upgrade progress
- conflict decisions taken during the current upgrade
- unresolved questions
- follow-up checks before closing the upgrade

This file is not for:
- stable merge rules
- final customization inventory
- long-term functional documentation

## Header

- Client: `simprints`
- Target version: `3.4.2`
- Base branch: `develop-eyeseetea`
- Base commit at previous upgrade close: `938b819597` (3.4.1, PR #323)
- `develop-eyeseetea` current head: `f87bec8c3` (3.4.2 Oslo + EyeSeeTea PRs #326, #328, #329, #330, #331)
- Upgrade branch: `feature-simprints/upgrade_3.4.2`
- Started on: `2026-09-09`
- Status: `in_progress`

Previous upgrade (3.4.1) closed and archived `2026-09-04` — see
`upgrade-3.4-notes.md` (kept as historical record, not further edited except
for baseline-promotion status flips) and
`openspec/changes/archive/2026-09-04-upgrade-to-3-4-1/`.

## Inherited context from the 3.4.1 upgrade — carry into this one

Checked against `develop-eyeseetea@f87bec8c3` before starting this merge
(see `upgrade-3.4-notes.md`'s "Improvements to promote to `develop-eyeseetea`"
section for full detail on each):

| Item | Status at 3.4.1 close | Verified now | Action for 3.4.2 |
|------|------------------------|--------------|-------------------|
| B1 — `AGENTS-<client>.md` template split | pending | **promoted** (`f87bec8c3`, PR #329/#330) | none — confirm merge doesn't reintroduce a stray `CLAUDE.md.template` reference |
| B4 — `PostMetadataSyncAction` extension point | implemented, not yet promoted | **promoted** (`4e5635da5`, PR #328) | verify Simprints' `app/src/simprints/.../PostMetadataSyncModule.kt` still matches baseline's contract signature after merge — this is the fix for the accepted regression from 3.4.1, confirm it still compiles/works, don't re-break it |
| B5 — `customization-techniques.md` | pending | **promoted** (`4e5635da5`, same commit as B4) | none |
| B2 — "Feat commits" section in `customization-files-template.md` | pending | **still pending** | not blocking; pick up only if touching that template this round |
| B3 — OpenSpec CLI scaffolding bump 1.2.0→1.8.0 | pending | **still pending** (baseline `.claude/skills/openspec-*` still `generatedBy: "1.2.0"`) | Simprints' own scaffolding is already on 1.8.0 (done during 3.4.1 onboarding) — expect `AA`-style conflicts again on these files if baseline hasn't bumped; resolve `accept_ours` as before, per the 3.4.1 precedent |
| B6 — `fieldListChannel` `DROP_OLDEST` race | pending | **still pending** (root cause untouched in baseline) | Simprints' 500ms debounce mitigation (`EnrollmentActivity`) is a local fork file — should survive the merge untouched; re-verify on device as part of `registerLast` validation regardless |

### B7. TDD / small-commit rules ported from `dhis2-android-sdk`'s OpenSpec config (new, 2026-09-09)

- **Baseline files:** `eyeseetea-docs/templates/openspec-config.yaml.template`
  (`rules.tasks` section)
- **Status:** `applied locally` in this fork's `openspec/config.yaml` and in
  the checked-out copy of the baseline template on this branch — **not yet
  promoted** to `develop-eyeseetea` itself (this branch cannot edit that repo
  directly; needs its own PR, same as B1-B6).
- **Evidence:** the sibling repo `dhis2-android-sdk` (`feature-oca/synced-data-retention-purge`)
  has an `openspec/config.yaml` with `rules.tasks` entries that this fork's
  template lacked: (1) test-first task granularity — one behavior-level test
  plus its minimal implementation is one commit (red→green); (2) commit
  boundaries stated explicitly per task group via a `Commit:` note, not left
  for an agent to infer; (3) tests must assert on observable behavior, never
  on which internal collaborator was called — no mocking the class under
  test's own collaborators, only at the true system boundary; (4) test names
  state the observable contract, not the mechanism.
- **Problem:** neither the Simprints `config.yaml` nor the shared
  `openspec-config.yaml.template` had any of this — `tasks.md` files across
  forks (including this one's own 3.4.1/3.4.2 upgrade tasks) never state
  commit boundaries, so an agent resuming `tasks.md` cold has to guess
  whether two adjacent tasks are one commit or two.
- **Fix applied:** ported 4 of the SDK repo's 5 `rules.tasks` items (skipped
  the 5th — mandatory failure/edge-case test coverage for delete/cascade
  semantics — since it's specific to the SDK's wipe-module domain, not
  general-purpose) into both
  `eyeseetea-docs/templates/openspec-config.yaml.template` and this fork's
  `openspec/config.yaml`, with wording genericized away from
  wiper/domain-module language.
- **To promote:** open a PR against `develop-eyeseetea` carrying just the
  `rules.tasks` diff to `openspec-config.yaml.template` (no code change, no
  behavior change — pure template/process). Until that lands, other forks
  onboarding fresh via `onboarding-fork-guide.md` Phase 4 won't get these
  rules unless they also copy them manually.

Open problems carried over, **not** tech-debt (that lives in
`refactors-pending.md`) but upgrade-relevant context:
- Simprints still uses the Dagger `LoginComponent`/`LoginModule` (baseline
  migrated login to KMP+Koin in 3.4.1) to inject `SyncBiometricsConfig` after
  sign-in. Check whether 3.4.2 changed anything further in `login/` that
  affects this — re-run the same restoration check done in 3.4.1
  (`AppComponent.plus(LoginModule)` declaration) if `AppComponent.java`
  conflicts again.
- DHIS2 core 2.43 `blockedSearchOperators`/`preferredSearchOperator`
  watch item (see 3.4.1 notes, bottom section) — re-run the `git grep` against
  whatever SDK tag ships with 3.4.2 if the SDK version changed, to check
  whether query-building code now reads these fields.

## Progress

- baseline prepared: `yes` — `develop-eyeseetea` confirmed as 3.4.2 + EyeSeeTea PRs
- merge started: `no`
- easy conflicts resolved: `no`
- manual conflicts pending: `no`
- validation started: `no`

## Decisions

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|

## Open Questions

- Whether the 3.4.2 Oslo diff (9 commits over 3.4.1, see `upstream/3.4.2`) touches any of the 13 active customization areas beyond what's already tracked above.
- Whether `app/src/simprints/.../PostMetadataSyncModule.kt` needs any change now that `PostMetadataSyncAction` is a stable baseline contract rather than a freshly-landed one.

## Validation Notes

- build: not started
- targeted tests: not started
- manual flows checked: not started

## Finalization

- surviving customizations moved to `customization-files.md`: `no`
- stable rules moved to `conflict-rules.md`: `no`
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `unknown`
