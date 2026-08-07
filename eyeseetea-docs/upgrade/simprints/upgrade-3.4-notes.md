# Simprints upgrade 3.4 notes

Use this file as the temporary working notes for the Simprints upgrade to 3.4.

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
- Target version: `3.4.1`
- Base branch: `develop-eyeseetea`
- Base commit at onboarding time: `73a7eb8f0fcc127d37d8887144673e480c4d5b93`
- Base commit updated to: `938b819597` (`develop-eyeseetea` after upgrade 3.4.1, PR #323)
- Current merge-base with upgrade branch: `d87193d003a0acccc53914f88026719df6fe8fc3`
- Upgrade branch: `feature-simprints/upgrade_3.4.1` (renamed from `feature-simprints/upgrade_3.4`)
- Started on: `2026-05-12`
- Status: `in_progress`

Note: a first merge attempt against `develop-eyeseetea@73a7eb8f0f` (3.4) was aborted before commit, with no changes to `eyeseetea-docs/` — restarting the merge against `develop-eyeseetea@938b819597` (3.4.1) instead.

Note: onboarding Phases 4-5 completed 2026-08-07 (OpenSpec specs for the 13 active customizations, `openspec/config.yaml`, root `CLAUDE.md`, `.claude/` scaffolding). The upgrade proposal `openspec/changes/upgrade-to-3-4-1/` was created the same day (`proposal.md`, `design.md`, `tasks.md`) and is the authoritative plan for this merge — this file continues to track temporary, session-level progress and conflict decisions per its own Purpose. Merge itself is starting now.

## Progress

- baseline prepared: `yes`
- merge started: `no`
- easy conflicts resolved: `no`
- manual conflicts pending: `no`
- validation started: `no`

## What has been done so far

- `eyeseetea-docs` was brought from `develop-eyeseetea` into `feature-simprints/upgrade_3.4`.
- Simprints onboarding files were created:
  - `eyeseetea-docs/customizations/simprints/customization-files.md`
  - `eyeseetea-docs/customizations/simprints/customization-specs.md`
  - `eyeseetea-docs/upgrade/simprints/upgrade-validation-checklist.md`
- The active Simprints scope was reduced to biometrics-related behavior only.
- The following areas were explicitly classified as not to preserve during merge:
  - login / OpenID / 2FA
  - notifications
  - change server URL
  - granular sync flavor wiring
- A first brownfield extraction of Simprints biometrics behavior was completed from code and `EyeSeeTea customization` comments.

## Decisions

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|
| `eyeseetea-docs/customizations/simprints/customization-files.md` | accept_ours | onboarding inventory for Simprints | `n/a` | resolved_keep_ours | New client-specific inventory file created on this branch. |
| `eyeseetea-docs/customizations/simprints/customization-specs.md` | accept_ours | temporary brownfield narrative draft | `n/a` | resolved_keep_ours | New client-specific draft created on this branch; later content may move to OpenSpec. |
| `eyeseetea-docs/upgrade/simprints/upgrade-validation-checklist.md` | accept_ours | client validation scaffold | `n/a` | resolved_keep_ours | New client-specific validation checklist created before merge. |
| `eyeseetea-docs/upgrade/simprints/upgrade-3.4-notes.md` | accept_ours | temporary upgrade notebook | `n/a` | resolved_keep_ours | This file tracks temporary progress and merge decisions for Simprints 3.4. |
| `app/src/simprints/**` | accept_ours_then_review_on_theirs | preserve flavor resources, branding, and client-specific source set contents | flavor identity surface | pending | Direct Simprints flavor surface documented in onboarding inventory and should not be dropped during merge. |
| `app/src/simprintsDebug/**` | accept_ours_then_review_on_theirs | preserve debug flavor resources and client-specific debug source set contents | flavor identity surface | pending | Keep unless a specific file is later proven to be obsolete drift. |
| `app/src/simprints/java/org/dhis2/data/user/UserComponentFlavor.kt` | accept_ours_then_review_on_theirs | preserve Simprints DI flavor entry point | flavor identity surface | pending | Explicit direct flavor code entry point documented during onboarding. |
| `login/**` | accept_theirs | remove Simprints drift in login/OpenID/2FA areas | `removed` | pending | Simprints does not preserve login/OpenID/2FA custom behavior. |
| `app/src/main/java/org/dhis2/data/notifications/**` | accept_theirs | remove notifications drift | `removed` | pending | Notifications are explicitly out of scope for preservation. |
| `app/src/main/java/org/dhis2/usescases/notifications/**` | accept_theirs | remove notifications drift | `removed` | pending | Notifications are explicitly out of scope for preservation. |
| `app/src/main/java/org/dhis2/utils/session/ChangeServerURL*` | accept_theirs | remove change-server-url drift | `removed` | pending | Change server URL must not survive the merge. |
| `app/src/simprints/java/org/dhis2/data/user/GranularSyncModule.kt` | accept_theirs | remove granular-sync flavor drift | `removed` | pending | Granular sync flavor wiring is not a Simprints customization to preserve. |
| `app/src/main/java/org/dhis2/data/biometrics/**` | manual_reapply_on_theirs | preserve Simprints biometrics integration over 3.4 baseline | active biometrics customization set | pending | Core customization area. Must be merged carefully against the 3.4 baseline. |
| `app/src/main/java/org/dhis2/usescases/biometrics/**` | manual_reapply_on_theirs | preserve Simprints-specific biometrics behavior | active biometrics customization set | pending | Includes age, module-id, sequential search, dashboard biometrics, duplicates, and UI helpers. |
| `app/src/main/java/org/dhis2/usescases/searchTrackEntity/**` | manual_reapply_on_theirs | preserve Simprints search integration only where biometrics is involved | biometric search integration | pending | Search is customized for biometric identify, duplicate review, confirm identity, and TEI card behavior. |
| `app/src/main/java/org/dhis2/usescases/enrollment/**` | manual_reapply_on_theirs | preserve form-side biometrics registration flow | enrollment / TEI form biometrics | pending | Enrollment keeps registration, duplicates, and `registerLast`, but not verification. |
| `app/src/main/java/org/dhis2/usescases/teiDashboard/**` | manual_reapply_on_theirs | preserve dashboard biometrics registration and verification | TEI dashboard biometrics | pending | Dashboard has both register and verify behavior. |
| `app/src/main/java/org/dhis2/data/forms/**` | manual_reapply_on_theirs | preserve form integration points for biometrics | enrollment / TEI form biometrics | pending | Only the biometrics-related pieces should survive. |
| `app/build.gradle.kts` | defer_after_build_verification | classify flavor definition vs baseline drift | `n/a` | needs_validation | Likely contains valid flavor surface plus technical drift. |
| `settings.gradle.kts` | defer_after_build_verification | classify local build and dependency drift | `n/a` | needs_validation | Needs comparison against EyeSeeTea 3.4 baseline. |
| `gradle/libs.versions.toml` | defer_after_build_verification | classify release identity vs dependency drift | `n/a` | needs_validation | Needs comparison against EyeSeeTea 3.4 baseline. |

## Confirmed functional findings from onboarding

- Config sync and selection:
  - biometrics configs are downloaded and stored locally
  - one active config is selected per entered program
  - precedence is `program` -> `orgUnitGroup` -> `default`
  - `default` is mandatory
- Config-driven biometrics behavior reviewed:
  - `biometricsMode`
  - `ageThresholdMonths`
  - `dateOfBirthAttribute`
  - `confidenceScoreFilter`
  - `orgUnitLevelAsModuleId`
  - `lastVerificationDuration`
  - `lastDeclinedEnrolDuration`
  - `enableIdentificationForTET`
- `BiometricsClient` reviewed:
  - built from active preferences by `BiometricsClientFactory`
  - sends metadata including fork version, TEI id, org unit context, user org units, and subject age
  - forces JSON responses with `versionCode=20250102`
  - maps register / identify / verify / confirm identity / `registerLast` responses back to DHIS2 models
  - preserves credential-linked matches below threshold
  - can propagate scanned credential data back into DHIS2
- Search and duplicate handling reviewed:
  - Simprints GUIDs are reused as biometrics attribute values in DHIS2 search
  - duplicate review is backed by DHIS2 search, not a disconnected list
  - duplicate flow can confirm identity, open existing dashboard, or continue with `registerLast`
- UI behavior reviewed:
  - `TEICardMapper` keeps biometrics and NHIS rows visible even when other empty attributes are hidden
  - cards decorate biometrics/NHIS rows with specific markers
  - avatar initials come from name/surname attributes
- Dashboard vs form distinction confirmed:
  - `TEI dashboard`: register + verify
  - `Enrollment / TEI form`: register + duplicates + `registerLast`, no form-driven verify found

## Baseline reference note

- `73a7eb8f0fcc127d37d8887144673e480c4d5b93` is the `develop-eyeseetea` branch head that was current when onboarding started.
- `d87193d003a0acccc53914f88026719df6fe8fc3` is the current `merge-base` with `feature-simprints/upgrade_3.4`.
- Use the branch head when describing the intended 3.4 baseline reference.
- Use the merge-base when reviewing unexplained shared drift or reconstructing what diverged on the Simprints branch.

## WIDP leftovers inherited by this branch

Verified 2026-08-07, before starting the 3.4.1 merge.

This branch carries WIDP customizations it never used, inherited through the
historical merge `f81f1dc3f3` (`origin/feature-widp/bring_last_changes_3_3_0_1`
into `feature-simprints/bring_last_changes_3_3_0_1`). Files that exist here but
not in `develop-eyeseetea` include:

- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorRequiredException.kt`
- `login/src/commonMain/kotlin/org/dhis2/mobile/login/main/domain/model/TwoFactorState.kt`
- `login/src/androidInstrumentedTest/kotlin/screen/TwoFAToEnableScreenTest.kt`
- `commonskmm/.../auth/OpenIdController.kt`, `OpenIdControllerImpl.kt`
- plus 2FA-related blocks inside shared files (`CredentialsViewModel.kt`,
  `CredentialsScreen.kt`, `LoginRepositoryImpl.kt`, `LoginResult.kt`)

Context: `develop-eyeseetea` was created from `develop-widp` (the cleanest fork
at the time) and had the WIDP customizations deliberately removed. The removal
is recorded in baseline history as `87c5da0109 "Remove 2factor customization"`.

Decision: **do not delete these manually before the merge.** `87c5da0109` is an
ancestor of `develop-eyeseetea@938b819597` but not of this branch, so the 3.4.1
merge itself will bring the deletion through a normal three-way merge — the same
way baseline was cleaned. This is the correct mechanism, not an accident.

Caveat to verify after merging (tasks 3.3 and 7.1): the deletion may surface as a
real conflict rather than a silent auto-delete, because Simprints pulled upstream
and WIDP bring-forward merges that touched the same files after that point.
Confirm the files are actually gone post-merge; do not assume.

## Open Questions

- Whether to install OpenSpec for Simprints immediately after onboarding or after the first baseline merge attempt.
- Whether `app/build.gradle.kts`, `settings.gradle.kts`, and `gradle/libs.versions.toml` contain any Simprints-specific flavor identity that should be preserved beyond pure drift cleanup.
- Whether there are Simprints biometrics customizations outside the already reviewed biometrics/search/enrollment/dashboard surface.

## Automerge casualties

Pending.

Use this section only if git automerge silently drops Simprints customization code in non-conflicting hunks during the real merge.

Expected format:

- `path/to/file` — short note about what was lost and how it was recovered

## Post-merge fork identity fixes

Pending.

Use this section after the merge if Simprints-specific flavor identity needs to be restored or rechecked, for example:

- flavor source sets
- flavor definitions in Gradle
- version naming
- fork-specific dependencies
- fork-specific app id / resources / branding

## Follow-ups resolved

Pending.

Move here any issue found during merge or validation once it has been closed, with date and short resolution note.

## New rules promoted to `conflict-rules.md`

Pending.

Only add rules here when a concrete Simprints merge incident reveals a reusable resolution pattern worth keeping beyond this upgrade.

## Validation Notes

- build: not started
- targeted tests: not started
- manual flows checked: code review only, no runtime validation yet

## Finalization

- surviving customizations moved to `customization-files.md`: `yes`
- stable rules moved to `conflict-rules.md`: `no`
- temporary notes ready to archive/remove: `no`
- unexplained shared drift remaining: `yes`
