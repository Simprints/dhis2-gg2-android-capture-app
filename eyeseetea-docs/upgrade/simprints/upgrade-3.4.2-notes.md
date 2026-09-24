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
- Status: `closed` (2026-09-17)

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
- merge started: `yes` (2026-09-10)
- easy conflicts resolved: `yes` — all conflicts resolved manually by the user; merge staged, not yet committed (`MERGE_HEAD` present)
- manual conflicts pending: `no`
- validation started: `yes` — automerge verification of the 13 customizations done (2026-09-10); build/tests not run yet

## Decisions

| File | Classification | Expected delta | Customization | Status | Notes |
|------|----------------|----------------|---------------|--------|-------|

## Automerge verification (2026-09-10)

Ran the mandatory post-merge check from `AGENTS-simprints.md`: `git diff
develop-eyeseetea -- <path>` (two-dot) for every file in
`customization-files.md`'s inventory, plus cross-checking original feat
commits against the inventory for gaps. **Result: none of the 13
customizations lost code to the automerge** — no silent drops.

Three inventory corrections applied to `customization-files.md`:

- **§2.11 (Verification Persistence) — added `HomeRepositoryImpl.kt`.** Was
  missing from the inventory despite carrying an active embedded block
  (`// EyeSeeTea customization - Biometric Verification Persistence`, an
  `init{}` cleanup of corrupted biometrics GUID attribute values). Confirmed
  present and untouched by the merge — this was a documentation gap, not a
  loss.
- **§2.10 (TEI UI Surfaces) — removed `ValueStoreImpl.kt`.** Checked full
  file history: its only customization (a commented-out no-resize-images
  tweak) was already dead code, removed on 2026-03-09 (`b354b59e8`, "Remove
  dead custom code") well before this upgrade. Not a merge casualty.
- **§2.8 (Relationship Search TET Toggle) — removed `SearchTEMap.kt`.**
  Checked full file history: it never carried biometrics/Simprints code, only
  two same-day unrelated import-path edits in March. Was listed without ever
  having had customization content.

Also removed a redundant `// EyeSeeTea customization` comment sitting above
an `import` line in `AppComponent.java` — `AGENTS-simprints.md` explicitly
disallows customization comments on imports (Oslo's GitHub Action rejects
them). The correct comment (above the actual `LoginComponent plus(...)`
method) was already in place and is untouched.

## Open Questions

- ~~Whether the 3.4.2 Oslo diff (9 commits over 3.4.1, see `upstream/3.4.2`) touches any of the 13 active customization areas beyond what's already tracked above.~~ **Resolved 2026-09-10**: automerge verification above confirms no customization lost code; see section above for the 3 inventory corrections found along the way.
- ~~Whether `app/src/simprints/.../PostMetadataSyncModule.kt` needs any change now that `PostMetadataSyncAction` is a stable baseline contract rather than a freshly-landed one.~~ **Resolved 2026-09-10**: confirmed it correctly references `org.dhis2.mobile.commons.domain.PostMetadataSyncAction`, which exists in baseline. No change needed.

## Validation Notes

- build: `passed` (2026-09-10) — `./gradlew assembleSimprintsDebug` succeeds, APK `dhis2-v3.4.2-simprints-fork-1-feature-simprints-upgrade_3.4.2.apk` produced. Unresolved-class warnings in the log (`androidx.window.extensions.*`, Compose tooling) are normal desugaring noise, not errors.
- targeted tests: `testSimprintsDebugUnitTest` — `passed` (2026-09-10), 977/977. First run failed with `Unresolved reference 'DaggerAppComponent'` in `App.kt:191` — stale kapt/KSP cache (same pattern documented in the 3.4.1 notes, unrelated to the `AppComponent.java` import-comment cleanup done this session); fixed with `./gradlew --stop` + deleting `app/build/kspCaches`, `app/build/generated/source/kapt`, `app/build/tmp/kapt3`, then re-run clean. `connectedSimprintsDebugAndroidTest` — in progress on `emulator-5554`.
- manual flows checked: `done` (2026-09-11) — same flows as 3.4.1, run against `upgrade-validation-checklist.md` (stable, version-independent checklist); no regressions found
- `connectedSimprintsDebugAndroidTest`: **BLOCKED** (2026-09-10) — 0 tests run, app crashes on launch: `java.lang.IllegalStateException: Room cannot verify the data integrity. Looks like you've changed schema but forgot to update the version number. Expected identity hash: b285324f0178f09d3ae20dc204fadef9, found: f256dcb402e7c06125f3b1de237fc9c2` (`D2Manager.instantiateD2` → `UserDao_Impl`). Same failure mode as the 3.4.1 note on this file (`upgrade-3.4-notes.md` line 312).
  - **2026-09-10 attempt (RETRACTED 2026-09-11): schema migration blamed on the `dhis2sdk` `1.14.1-eyeseetea-fork-1` → `1.14.2-eyeseetea-fork-1` bump.** A manual `DataElement.url` column was added to `dhis_test.db` and the stored `room_master_table` identity hash was hand-patched to match, on the theory that this SDK bump changed the Room schema. **This theory was wrong.** Checked the actual SDK repo (`/Users/xurxodev/Workspace/EyeSeeTea/android/dhis2-android-sdk`) via `git diff 1.14.1-eyeseetea-fork-1 1.14.2-eyeseetea-fork-1`: only 7 files changed, all `PasswordAndCookieAuthenticator`-related plus `libs.versions.toml`/docs — **zero schema or `DataElement` changes**. `DataElement.url` already existed in `1.14.1`. `AppDatabase.kt` (the `@Database` declaration) is byte-identical between the two tags. The manual `.db` migration was reverted (`git checkout -- app/src/androidTest/assets/databases/dhis_test.db`, 2026-09-11) — it is byte-identical to `develop-eyeseetea` tip again.
  - **Net effect: this line item is back to unresolved.** The identity-hash mismatch is real (confirmed by the original stack trace) but its actual cause is still unknown — it is not the `1.14.1`→`1.14.2` bump. Needs re-investigation: check what SDK version the *previous* (3.4.1) `.db` regen was actually built against, and whether something else in this 3.4.2 merge (not the SDK version bump) changed the compiled Room schema.
  - Everything below this point from the 2026-09-10 session (the play.im.dhis2.org full-swap rejection reasoning, the androidTest run that stopped after `TeiFlowTest`) was produced while chasing the wrong root cause and needs to be re-verified once the real schema mismatch is found — kept here only as investigation history, not as a resolved fix:
    - Tried borrowing a newer upstream `.db` (`f56049fe3`, `user_version` 181) — failed with `A migration from 181 to 180 was required but not found`; this fork's SDK only understands version 180.
    - Tried a full data swap from a freshly-synced `https://play.im.dhis2.org/stable-2-43-1` `.db` — fixed the identity hash but replaced Oslo's deterministic fixture data with live demo data, breaking ~18 content-dependent androidTests. Rejected for that reason, independent of the root-cause error above.
    - A `connectedSimprintsDebugAndroidTest` run against the (now-reverted) patched `.db` stopped after `TeiFlowTest` (58/109 tests, 4 pre-existing unrelated failures) without finishing the suite — cause undiagnosed, and irrelevant to re-check until the `.db` itself is fixed for the right reason.

## Finalization

- surviving customizations moved to `customization-files.md`: `yes` — all 13 confirmed, plus the 3 inventory corrections above; `PostMetadataSyncModule.kt`'s customization comment title corrected to match its spec ("Biometrics Configuration Selection Per Program Or Org Unit Group").
- stable rules moved to `conflict-rules.md`: `n/a` — no new stable merge rule was discovered during this upgrade beyond what `conflict-rules.md` already documented from 3.4.1.
- temporary notes ready to archive/remove: `yes` — this file is closed; kept as historical record like `upgrade-3.4-notes.md`, not further edited except for baseline-promotion status flips.
- unexplained shared drift remaining: `none known`

Closed 2026-09-17. `openspec validate --specs --strict`: 13/13 passed. `check_upgrade_docs.py --client simprints`: clean except `build/` artifact noise and the one already-documented non-biometrics comment title (`DashboardViewModel.kt`/`TEIDataFragment.kt`, tracked as an open documentation question in `customization-files.md`, not a gap introduced by this upgrade). Unit tests: 977/977. `connectedSimprintsDebugAndroidTest` remains blocked by the pre-existing Room identity-hash mismatch (root cause not found in this upgrade — the SDK `1.14.1`→`1.14.2` bump was investigated and ruled out); carried forward as a known issue in the PR, per the 3.4.1 precedent. Archived as `openspec/changes/archive/2026-09-17-upgrade-to-3-4-2/`.
