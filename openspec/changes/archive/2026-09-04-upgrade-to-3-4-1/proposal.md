## Why

The Simprints fork is currently based on `develop-eyeseetea` 3.3.1
(`3.3.1-simprints-fork-1`), while the shared baseline has moved to **3.4.1**
(`develop-eyeseetea@938b819597`, PR #323). Staying behind the baseline means
missing upstream Oslo fixes and baseline-wide EyeSeeTea improvements, and lets
the gap to the next upgrade grow larger and riskier. A first merge attempt
against the intermediate 3.4 baseline was already aborted (2026-08-06,
`develop-eyeseetea` had since moved on to 3.4.1); this change targets 3.4.1
directly.

## What Changes

- Merge `develop-eyeseetea@938b819597` (3.4.1) into
  `feature-simprints/upgrade_3.4.1`, moving the implementation baseline for
  all 13 active Simprints customizations from 3.3.1 to 3.4.1.
- Classify every conflicted and non-conflicted-but-customization-touching
  file per `eyeseetea-docs/upgrade/conflict-rules.md`
  (`accept_ours` / `accept_theirs` / `manual_reapply_on_theirs` /
  `defer_after_build_verification`).
- Resolve `accept_ours` / `accept_theirs` files first, then manually reapply
  Simprints logic on top of the new baseline for `manual_reapply_on_theirs`
  files (expected concentration: biometrics, search, TEI dashboard,
  enrollment — see Impact).
- Run the automerge verification rule from `CLAUDE.md` /
  `conflict-rules.md` for every file listed in `customization-files.md`, not
  only files git marks as conflicted.
- Confirm the areas explicitly out of scope for preservation (2FA/login,
  notifications, change server URL, granular sync flavor wiring — section 3
  of `customization-files.md`) end up matching baseline behavior, not any
  prior Simprints-specific variant.
- Validate the result against
  `eyeseetea-docs/upgrade/simprints/upgrade-validation-checklist.md` (13
  functional flows + the out-of-scope negative check).
- Update `customization-files.md` with confirmed surviving customizations
  and close out `eyeseetea-docs/upgrade/simprints/upgrade-3.4-notes.md`.

No **BREAKING** changes to Simprints-facing behavior are intended — see
Capabilities below.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. This change intentionally preserves all 13 active Simprints
capabilities unchanged at the requirements level (`openspec/specs/`) — only
the implementation baseline moves from 3.3.1 to 3.4.1. If resolving a
`manual_reapply_on_theirs` conflict surfaces a genuine behavior change that
cannot be avoided (e.g. upstream removed something a customization structurally
depended on), that is out of scope for this change and must be raised with the
user and proposed separately, not folded silently into the upgrade.

This change sets `skip_specs: true` in `.openspec.yaml` accordingly.

## Impact

- **Flavor source set (`app/src/simprints/`, `app/src/simprintsDebug/`)**:
  low conflict risk — flavor code, resources, and manifests are isolated from
  upstream by construction (see `customization-files.md` section 1).
- **Shared code (`app/src/main/...`)**: this is where the real conflict
  surface lives. Per `customization-files.md` section 2, the biometrics
  integration touches ~40+ shared files across
  `data/biometrics/`, `usescases/biometrics/`, `usescases/searchTrackEntity/`,
  `usescases/teiDashboard/`, and `usescases/enrollment/`. These are the files
  most likely to need `manual_reapply_on_theirs` treatment.
- **Areas to remove, not preserve**: `login/`, `data/notifications/`,
  `usescases/notifications/`, `utils/session/ChangeServerURL*`,
  `data/service/SyncPresenterImpl.kt` (granular sync wiring only),
  `utils/granularsync/SyncStatusDialog.kt` — per section 3 of
  `customization-files.md`.
- **Build/dependency files**: `app/build.gradle.kts`, `settings.gradle.kts`,
  `gradle/libs.versions.toml` currently listed as unclassified shared drift
  (`customization-files.md` section 4) — need classification against the new
  baseline's flavor and composite-build blocks during this upgrade.
- **SDK fork**: no known SDK-side patch dependency for the 13 active
  capabilities; confirm during `manual_reapply_on_theirs` resolution whether
  3.4.1 introduces new SDK-fork coupling.
- **Tests**: `SearchTEIViewModelTest.kt` already carries Simprints-specific
  test coverage; expect updates wherever `SearchTEIViewModel.kt` needs manual
  reapply.
