## Why

The Simprints fork is based on `develop-eyeseetea` 3.4.2
(`3.4.2-simprints-fork-1`, `develop-simprints@09cad0d3e`, merge-base
`f87bec8c3`). The shared baseline has moved to Oslo **3.5.0**
(`develop-eyeseetea@89b2f2e3b`, PR #343). Unlike 3.4.2 (9 Oslo commits), this
is a full Oslo minor version: 164 commits in `origin/upstream/3.4.2..origin/upstream/3.5.0`.
Many of them touch the search, TEI dashboard, enrollment and login surfaces
that the 14 active Simprints customizations are built on. Upgrading now keeps
the fork on a supported upstream version and on the EyeSeeTea SDK fork
`1.15.0-eyeseetea-fork-1`.

## What Changes

- Merge `develop-eyeseetea@89b2f2e3b` (Oslo 3.5.0 plus EyeSeeTea PRs #343 and
  the two docs commits `3cd19be2e`, `8e0200bcc`) into
  `feature-simprints/upgrade_3.5.0`. This moves the implementation baseline
  for all 14 active Simprints customizations from 3.4.2 to 3.5.0.
- Classify every conflicted file, and every file listed in
  `customization-files.md` even when it is not conflicted, per
  `eyeseetea-docs/upgrade/conflict-rules.md` (`accept_ours` / `accept_theirs`
  / `manual_reapply_on_theirs` / `defer_after_build_verification`).
- Resolve `accept_ours` / `accept_theirs` first, then manually reapply the
  Simprints logic on top of the new baseline for `manual_reapply_on_theirs`
  files.
- Replicate into `app/src/simprints/` the Oslo changes made to the `dhis2` /
  `dhis2PlayServices` flavor files that the Simprints flavor copies. Git does
  not raise a conflict for these: the 3.5.0 case is `GranularSyncModule.kt`,
  where `WorkManagerController` was removed. This was found during the
  baseline upgrade.
- Re-check the shared changes that baseline already absorbed in 3.5.0:
  - ANDROAPP-7666 fix removed: Oslo now ships it.
  - `UserManagerImpl.java` migrated to Kotlin by Oslo, without `logIn()`.
  - SDK `1.15.0-eyeseetea-fork-1`.
  - Oslo's `ci.yml` disabled at repo level.
- Run the automerge verification rule from `AGENTS-simprints.md` /
  `conflict-rules.md` for every file in `customization-files.md`.
- Validate against `eyeseetea-docs/upgrade/simprints/upgrade-validation-checklist.md`.
- Update `customization-files.md` and `AGENTS-simprints.md` (version and
  upgrade context), then close out
  `eyeseetea-docs/upgrade/simprints/upgrade-3.5.0-notes.md`.

No **BREAKING** changes to Simprints-facing behavior are intended.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. This change preserves all 14 active Simprints capabilities unchanged
at the requirements level (`openspec/specs/`). Only the implementation
baseline moves from 3.4.2 to 3.5.0. If a `manual_reapply_on_theirs`
conflict surfaces a behavior change that cannot be avoided, it is out of
scope here: raise it with the user and propose it separately instead of
folding it into the upgrade. This is why the change sets `skip_specs: true`
in `.openspec.yaml`.

## Impact

- **Flavor source set (`app/src/simprints/`, `app/src/simprintsDebug/`)**:
  no git conflict risk, by construction. Two kinds of risk remain:
  - Silent staleness in files copied from the `dhis2` flavor:
    `GranularSyncModule.kt` must follow Oslo's 3.5.0 change.
    `DownloadNewVersion.kt` is unchanged in `dhis2PlayServices` for this range.
  - Compile breaks in flavor code that calls SDK or shared APIs. Oslo adapted
    to SDK breaking changes in this range (`a919bf84a`, `ebd6fdad7`,
    `0d8268015`); flavor code was not adapted.
- **Shared code (`app/src/main/...`, `form/`, `tracker/`, `login/`)**: large
  conflict surface. Baseline changes overlap with these inventory sections:
  - **§2.1 Biometric search, §2.8 relationship TET toggle, §2.9 duplicate review**
    - Files: `SearchTEIViewModel`, `SearchTEActivity`, `SearchTEPresenter`,
      `SearchTeiViewModelFactory`, `SearchTEList`, `SearchTEMap`,
      `SearchJavaToCompose`, `tracker/.../SearchTrackedEntities`.
    - Oslo changes: search error messages (`cf72c54d5`) and the custom
      terminology series (`206a40711`, `09a0a8d56`, `1cadd3746`, …).
  - **§2.10 TEI UI surfaces and §2.4/§2.6 related wiring**
    - Files: `TEICardMapper`, `DashboardProgramModel`,
      `TeiDashboardMobileActivity`, `TEIDataFragment`, `TEIDataPresenter`,
      `EnrollmentPresenterImpl`, `ValueStoreImpl`, `form/.../Form.kt`,
      `FormView.kt`, `FormViewModel.kt`.
    - Oslo changes: custom labels (`4a3742685`, `998ff6aec`, `641c6a9e8`),
      form section reload (`977c1c439`), picture processing (`8d2c133f6`).
  - **§2.2 Biometrics config selection**
    - Files: `LoginActivity` and `LoginModule`, which carry the
      `SyncBiometricsConfig` wiring, and `ProgramViewModel`.
    - Oslo changes: OAuth / offline login / session renewal series
      (`721491b3d`, `07d35e94f`, `15a97849f`, `3105c0547`, `749953d06`,
      `9677ebe3e`, `f6cc71488`).
  - **§5 notes, `DashboardViewModel` transfer cache**
    - Oslo deleted `TeiDashboardMenu.kt` (`4a3742685`), which was the caller
      of the cached `checkIfTeiCanBeTransferred()`.
    - Check whether the cache still has a caller or has become dead code.
- **Areas to remove, not preserve** (section 3): unchanged. Oslo modified
  `SyncPresenterImpl.kt` and `OpenIdSession.kt` in this range, so
  `accept_theirs` applies.
- **Build/dependency files**: `app/build.gradle.kts`, `settings.gradle.kts`
  and `gradle/libs.versions.toml` get `defer_after_build_verification`.
  Baseline now pins AGP 9.3.1, SDK `1.15.0-eyeseetea-fork-1` and version
  name `3.5.0-eyeseetea-fork-1`. The fork needs `3.5.0-simprints-fork-1`.
- **OpenSpec/Claude scaffolding**: baseline `.claude/skills/openspec-*` is
  still generated by CLI `1.2.0` (B3 still pending), while this fork has
  `1.8.0`. Expect the same `AA` conflicts as before, resolved `accept_ours`.
  Baseline also adds new `.claude/` skills and agents (Sentry, Zephyr, test
  flows), which are `accept_theirs`.
- **Tests**: `SearchTEIViewModelTest`, `EnrollmentPresenterImplTest`,
  `TEIDataPresenter`-adjacent tests and the fork's biometrics tests. Expect
  updates wherever a manual-reapply file changes.
