## 1. Pre-merge setup

- [ ] 1.1 Confirm `feature-simprints/upgrade_3.4.2` is checked out and `develop-eyeseetea` is at `f87bec8c3` (3.4.2 Oslo + EyeSeeTea PRs #326, #328, #329, #330, #331).
- [ ] 1.2 Confirm `eyeseetea-docs/upgrade/simprints/upgrade-3.4.2-notes.md` header reflects the 3.4.2 target and note the merge is starting.
- [ ] 1.3 Check whether a `develop-simprints` branch exists with commits ahead of this branch (mirrors the last step of the 3.4.1 upgrade) — if so, note its scope in `upgrade-3.4.2-notes.md` and plan to merge it only after this upgrade is validated, not before or during.

**Commit:** docs-only (notes header update); no merge has happened yet, so this is one commit, e.g. `docs(simprints): start 3.4.2 upgrade notes`.

## 2. Merge and preclassification

- [ ] 2.1 Merge `develop-eyeseetea` into `feature-simprints/upgrade_3.4.2`.
- [ ] 2.2 Classify direct flavor files (`app/src/simprints/**`, `app/src/simprintsDebug/**`) as `accept_ours`.
- [ ] 2.3 Classify every file listed in `customization-files.md` section 2 (13 active customizations).
- [ ] 2.4 Classify files listed in `customization-files.md` section 3 (out-of-scope areas) as `accept_theirs`.
- [ ] 2.5 Classify files listed in `customization-files.md` section 4 (shared drift: `app/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`) as `defer_after_build_verification`.
- [ ] 2.6 Classify any remaining conflicted file not covered above per `conflict-rules.md` default rules by path; if a real customization surfaces, flag the inventory gap before continuing.
- [ ] 2.7 Record the preclassification table in `upgrade-3.4.2-notes.md` per the `conflict-rules.md` temporary format.
- [ ] 2.8 Pause and confirm with the user before starting the `manual_reapply_on_theirs` batch.

**Commit:** 2.1 starts the merge; with conflicts present, git refuses any `git commit` at all — not even an unrelated docs file — until every conflicted path is staged. So 2.2-2.7 (classification, note-taking in `upgrade-3.4.2-notes.md`) happen in the working tree while the merge is still open, but cannot be committed yet; they get committed together with the conflict resolutions from sections 3/5/6 as part of the single merge commit that closes in task 6.1. 2.8 is a pause point, not a commit.

## 3. Resolve easy conflicts

- [ ] 3.1 Resolve all `accept_ours` flavor files.
- [ ] 3.2 Resolve all `accept_theirs` files (out-of-scope areas + clear shared-base changes).
- [ ] 3.3 Resolve the expected `.claude/skills/openspec-*` / `.claude/commands/opsx/*` `AA` conflicts as `accept_ours` (this fork is on CLI 1.8.0, baseline is still on 1.2.0 as of `f87bec8c3` — see design.md step 5).
- [ ] 3.4 Run the post-merge fork identity check (`conflict-rules.md`): version/identity strings in `gradle/libs.versions.toml`, flavor source sets present, no dependency silently removed, `app/build.gradle.kts` still defines the `simprints` flavor.

**Commit:** 3.1-3.3 stage resolved paths into the still-open merge (`git add`) — same "cannot commit yet" constraint as section 2, so these land inside the eventual single merge commit, not separately. 3.4 is a verification step with no code change of its own, run once every conflict is staged but still before `git commit` closes the merge; if it finds a real identity regression, fix it now (it's still pre-commit) rather than as a separate follow-up.

## 4. Targeted checks for known baseline changes

- [ ] 4.1 `PostMetadataSyncAction` re-check — confirm `app/src/simprints/.../PostMetadataSyncModule.kt` still compiles against baseline's current contract in `:commonskmm`/`:sync`, and that it still fixes the metadata-sync biometrics-config regression from 3.4.1 (manual or automated re-check, see `upgrade-validation-checklist.md`).
- [ ] 4.2 `dd88b0b51` priority check — inspect whether "use aggregated syncState for sync button in search list" (`ANDROAPP-7682`) conflicts with or silently changes behavior in `SearchTEIViewModel.kt` / `SearchTeiModel`-adjacent files used by `biometric-search-integration` or `biometrics-tei-ui-surfaces`.
- [ ] 4.3 `AGENTS-<client>.md` check — confirm the merge does not reintroduce a fork-owned `CLAUDE.md` or drop the `@AGENTS-simprints.md` import line.
- [ ] 4.4 SDK bump check — confirm the Oslo SDK version bump (`643e66591`, `1.14.2-SNAPSHOT`) does not conflict with the EyeSeeTea SDK-fork composite-build substitution in `settings.gradle.kts`.

**Commit:** 4.1 and 4.4 need the project to actually compile to be meaningful, so run them after 6.2's build is green, not before (4.2 and 4.3 are pure diff/text reading and can happen any time once the relevant files are staged). The merge is still open at this point (git only allows `git commit` in task 6.1 once the build-verification gate in 6.2 passes), so any fix these checks need is just more staged content in the same open merge — no separate commit. Only a regression found **after** the merge commit has already closed (e.g. caught later during validation in section 8) gets its own follow-up commit, scoped to the one file/behavior it repairs.

## 5. Resolve manual conflicts — active capabilities

- [ ] 5.1 Resolve any conflicts in the 13 active-customization files found in task 2.3, reapplying only the minimum Simprints-specific delta per `conflict-rules.md`.
- [ ] 5.2 Pause and confirm with the user if any manual-reapply file's resulting diff is significantly larger than expected (per design.md's "expected delta" rule).

**Commit:** git blocks committing anything while a merge has unresolved conflicts — there is no per-capability commit option here. All of 5.1 (every conflicted file's resolution, including the reapplied Simprints logic) goes into the single merge commit that `git commit` produces once every conflict from 2.1/3.x/5.x/6.1 is staged — same as the 3.4.1 upgrade's single `21ac09bad` merge commit. 5.2 is a pause point, not a commit. Anything that needs *new* behavior beyond what conflict resolution requires (not just reapplying what was already there) is out of scope for this merge commit — raise it with the user per the design.md "expected delta" rule instead of folding it in.

## 6. Deferred and shared-drift resolution

- [ ] 6.1 Reconcile `app/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml` against the new baseline — minimum needed to compile.
- [ ] 6.2 Confirm the project builds (`./gradlew assembleSimprintsDebug`).

**Commit:** 6.1 concludes the merge commit (these are the last unresolved paths) — `git commit` to close the merge happens here, once 6.2's build passes. 6.2 is a verification gate, not a commit; if the build fails and needs a fix outside the merge's conflicted files, that fix is a separate follow-up commit after the merge is closed.

## 7. Automerge verification

- [ ] 7.1 For every file listed in `customization-files.md` (not only conflicted ones), run `git diff develop-eyeseetea -- <file>` and confirm all documented customization lines survived.
- [ ] 7.2 Recover any silently dropped customization code found in 7.1 before staging.

**Commit:** 7.1 is read-only, no commit. Each casualty recovered in 7.2 is its own commit, scoped to the one file/customization it restores (e.g. `fix(biometrics): restore <X> dropped by automerge` — mirrors the 3.4.1 upgrade's pattern of one commit per casualty, never a batch).

## 8. Validation

- [ ] 8.1 Run `./gradlew testSimprintsDebugUnitTest` and fix regressions in Simprints-specific tests.
- [ ] 8.2 Manually validate all flows from `upgrade-validation-checklist.md` that are reachable given available time — prioritize the high-risk capabilities (#1, #2, #9, #10, #13) and the two targeted checks from section 4 above.
- [ ] 8.3 If any flow is deliberately left unvalidated due to time constraints, document it explicitly in the PR's "Known Issues" and in `upgrade-validation-checklist.md`'s per-flow status — do not silently skip, per the 3.4.1 precedent.

**Commit:** 8.1 is test-first in the TDD sense only when a regression needs a code fix — in that case, the test (if not already failing-and-covering) plus its minimal fix is one commit; a test added for coverage with no behavior change is its own separate commit (per the B7 commit-granularity rule in `openspec/config.yaml`). 8.2 is manual QA, no commit. 8.3 is a docs-only commit (`docs(simprints): record 3.4.2 validation scope`), separate from any code fix commits above.

## 9. Close out

- [ ] 9.1 Update `customization-files.md` with confirmed surviving customizations and technical notes from the manual-reapply tranche.
- [ ] 9.2 Update `upgrade-3.4.2-notes.md` progress section and close it out once the upgrade is validated.
- [ ] 9.3 Run `python3 eyeseetea-docs/scripts/check_upgrade_docs.py --client simprints` and confirm only expected out-of-scope noise remains.
- [ ] 9.4 Run `openspec validate --specs --strict` and confirm all 13 specs still pass unchanged.
- [ ] 9.5 If this change surfaced a genuine capability-behavior change (not just an implementation move), stop and raise it with the user as a separate change instead of folding it in here.
- [ ] 9.6 Archive this OpenSpec change once closed (`openspec/changes/upgrade-to-3-4-2/` → `openspec/changes/archive/<date>-upgrade-to-3-4-2/`).

**Commit:** 9.1-9.2 are one docs commit (`docs(simprints): close out 3.4.2 upgrade notes`). 9.3-9.4 are verification only, no commit unless they find something to fix (own commit if so). 9.5 is a decision point, not a commit. 9.6 is its own commit (`chore(openspec): archive upgrade-to-3-4-2 change`), mirroring the 3.4.1 close-out commit `89837351f`.
