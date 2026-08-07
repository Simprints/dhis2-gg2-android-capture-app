# Simprints Fork — dhis2-android-capture-app

EyeSeeTea fork of the DHIS2 Android Capture app for the Simprints client.
This file holds the fork-specific guidance; the shared project guidance lives in
`AGENTS.md` (maintained upstream by Oslo). Both are imported from `CLAUDE.md`.

- **Flavor:** `simprints` (app ID: `org.simprints.dhis2`)
- **Current version:** `3.4.1-simprints-fork-1`
- **Upstream:** dhis2/dhis2-android-capture-app
- **Baseline branch:** `develop-eyeseetea` (shared EyeSeeTea baseline, never client-specific)
- **SDK fork:** `com.github.EyeSeeTea:dhis2-android-sdk` (EyeSeeTea patches on top of the DHIS2 Android SDK, consumed via JitPack/composite-build substitution)

## Project structure

12 modules: `:app`, `:dhis_android_analytics`, `:form`, `:commons`, `:dhis2_android_maps`, `:compose-table`, `:stock-usecase`, `:dhis2-mobile-program-rules`, `:tracker`, `:aggregates`, `:commonskmm`, `:login`, `:sync`.

Key source sets:
- `app/src/main/` — shared code (all flavors)
- `app/src/simprints/` — Simprints flavor-specific code and resources
- `app/src/simprintsDebug/` — build-type overrides

## Build and test

```bash
./gradlew assembleSimprintsDebug          # build Simprints debug APK
./gradlew testSimprintsDebugUnitTest      # run Simprints-specific unit tests
./gradlew ktlintCheck                     # code style
```

See `AGENTS.md` for the full build/lint command set and Gradle task naming rules
per module type.

## Customizations

13 confirmed Simprints customizations. Each has an OpenSpec spec in `openspec/specs/`:

| # | Spec slug | Status | Risk |
|---|-----------|--------|------|
| 1 | `biometric-search-integration` | active | high |
| 2 | `biometrics-config-selection` | active | high |
| 3 | `biometrics-mode-controls` | active | medium |
| 4 | `biometrics-age-threshold` | active | medium |
| 5 | `biometrics-date-of-birth-attribute` | active | low |
| 6 | `biometrics-confidence-score-filtering` | active | medium |
| 7 | `biometrics-org-unit-module-id` | active | medium |
| 8 | `biometrics-relationship-search-tet-toggle` | active | low |
| 9 | `biometrics-duplicate-review-confirm-identity` | active | high |
| 10 | `biometrics-tei-ui-surfaces` | active | high |
| 11 | `biometrics-verification-persistence` | active | medium |
| 12 | `biometrics-time-based-windows` | active | low |
| 13 | `simprints-data-exchange-mapping` | active | high |

Areas explicitly **not** preserved during upgrades (see section 3 of `customization-files.md`): 2FA/login changes, notifications, change server URL, granular sync flavor wiring.

### Customization code rules

**Principle: minimize changes to upstream Oslo code.** Every line modified in an Oslo file is a future merge conflict. When implementing or fixing a customization, always look for a solution that avoids touching Oslo files first. If you must touch them, prefer the lowest-impact option in the hierarchy below. This is a trade-off — sometimes inline edits are unavoidable — but the default posture is to protect merge compatibility.

**Placement hierarchy** (prefer top options):
1. Flavor source set (`app/src/simprints/`) — best isolation, zero conflict risk
2. New file in shared code with header comment — no Oslo file touched
3. Append block at end of existing shared file — low conflict risk
4. Inline edit in shared file — last resort, highest conflict risk

This file is itself an example of rule 2: rather than rewriting Oslo's `CLAUDE.md`, the fork keeps its guidance here and adds a single `@AGENTS-simprints.md` import line to it.

**Comment convention:** Every customized file must have `// EyeSeeTea customization - [Title]` where `[Title]` matches the spec heading exactly. Not in imports (Oslo GitHub action rejects them). Place the comment **right above the customized block**, not above the containing scope.

Use `// EyeSeeTea fix - [description] (Oslo [ticket], introduced [version])` instead when patching an Oslo regression that affects all forks — those belong in `develop-eyeseetea`, never in a fork branch. See `conflict-rules.md`.

**Automerge verification:** After any merge of the baseline, run `git diff develop-eyeseetea -- path/to/file` for **every file listed in `customization-files.md`** — not only files git marked as conflicted. Git automerge can silently apply baseline commits that delete customization wiring, dropping code with no conflict markers. Compare each diff against the inventory and recover missing lines before staging. The rule is load-bearing only if the inventory is complete: for each customization, cross-check `git show <feat-commit> --stat` against `customization-files.md` so no wiring file is missing. See `eyeseetea-docs/upgrade/conflict-rules.md` for the full rule.

**Customization can live inside a baseline file.** Checking "does biometrics code reference this file?" is not enough — a file owned by Oslo can contain a Simprints block that nothing else references (e.g. `HomeRepositoryImpl`'s biometrics-GUID cleanup). Always grep the file's own contents for `biometric`/`simprints` before accepting a deletion or `theirs`.

**Post-merge check hierarchy:** marker-count < symbol-scan < diff-scan with semantic filter < manual emulator test. Each level catches what the previous one misses. Manual test in emulator is the irreplaceable last-line safety net — automated checks miss runtime rendering bugs and casualties in files without `// EyeSeeTea customization` markers.

## Key documentation

- `openspec/specs/` — functional specs (source of truth for what each customization does)
- `openspec/config.yaml` — project context and OpenSpec rules
- `eyeseetea-docs/customizations/simprints/customization-files.md` — technical file inventory
- `eyeseetea-docs/upgrade/simprints/upgrade-3.4-notes.md` — upgrade progress and status
- `eyeseetea-docs/upgrade/simprints/upgrade-validation-checklist.md` — manual validation flows
- `eyeseetea-docs/upgrade/conflict-rules.md` — merge conflict resolution rules

## Upgrade context

Upgrading from `3.3.1-simprints-fork-1` to `3.4.1`. Progress and conflict decisions are tracked in `eyeseetea-docs/upgrade/simprints/upgrade-3.4-notes.md`, and the plan lives in `openspec/changes/upgrade-to-3-4-1/`. Always use **two-dot diff** (`git diff develop-eyeseetea..HEAD`) to compare against baseline — three-dot misses deletions from the baseline side.

## Automation extraction rule

Track repetitive patterns during the conversation. If you observe the same task structure executed 3+ times with different inputs (e.g., resolving conflicts with the same strategy, writing tests with the same shape, applying the same transformation across files), proactively suggest extracting it into:
- A **CLAUDE.md rule** if it's a guideline (3-5 repetitions)
- An **agent** if it's a multi-step autonomous task (6+ repetitions)
- A **skill** if it requires a specialized protocol not derivable from context

State: what pattern you detected, how many times it occurred, and a concrete proposal for the extraction. Do not create the artifact — propose it and wait for approval.
