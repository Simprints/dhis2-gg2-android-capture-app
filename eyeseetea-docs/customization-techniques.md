# Customization Techniques

Reusable techniques for customizing shared (Oslo) code from a client fork.

Use this file when you need to change shared behavior and want to know **how**
other forks have already solved the same shape of problem. Each entry says what
the technique is for, how it works, which fork uses it, and what it costs at
upgrade time.

This is not an inventory of customizations — those live in
`eyeseetea-docs/customizations/<client>/customization-files.md`. This is the
catalogue of *mechanisms*.

## Why this file exists

Every line changed inside an Oslo file is a future merge conflict, and a fork
that invents its own mechanism pays that cost alone. When a technique is written
down here, the next fork can reuse it — and when Oslo removes the hook it
depends on, every fork using it breaks in the same, findable way.

Before inventing a new mechanism, check whether one of these already fits. After
inventing one, add it here.

## Placement hierarchy (the rule these techniques serve)

Always pick the first option that works:

1. **Flavor source set** (`app/src/<flavor>/`) — zero conflict risk
2. **New file in shared code** — no Oslo file touched
3. **Block appended at the end of an existing shared file** — low conflict risk
4. **Inline edit inside Oslo code** — last resort

The techniques below are mostly ways of staying at level 2 or 3 when the naive
solution would land at level 4.

---

## T1. Field hooks — replacing or filtering the UI of a form field

**Use when:** a client needs a form field to render as a custom component, or
needs certain fields hidden or reconfigured, without editing the form rendering
code itself.

**Used by:** Simprints — swaps the biometrics attribute for its own capture
component, hides it entirely when the program's `biometricsMode` is not `full`,
and sets editability and the age-threshold flag on it.

**How it works.** `FormView` exposes two optional callbacks through its builder:

```kotlin
FormView.Builder()
    // transform the field list before it is rendered: filter, replace, reconfigure
    .onFieldsLoadingListener { fields: List<FieldUiModel> -> transformedFields }
    // observe the final list, after transformation
    .onFieldsLoadedListener { fields: List<FieldUiModel> -> }
```

The fork implements the transformation wherever it owns the logic (Simprints
does it in `EnrollmentPresenterImpl.onFieldsLoading()`) and wires it up at the
call site (`FormInjector`).

Because a field is identified by its own `FieldUiModel` subtype, a fork can
introduce its own (Simprints has `BiometricsAttributeUiModelImpl`) and detect it
with an `is` check inside the transformation.

**Where the hook is applied.** Since 3.4.1 the transformation runs inside
`FormViewModel`, immediately before `formSectionMapper.mapFromFieldUiModelList()`
— it has to see the raw `FieldUiModel` list, because once fields are grouped
into sections the individual models are no longer reachable. `FormView` passes
the callbacks to the ViewModel in `onCreateView`.

**Rendering the custom component.** The other half is `FieldProvider`, which
picks the composable for each field. Simprints wraps baseline's `when` block in
an `if (fieldUiModel is BiometricsAttributeUiModelImpl)` branch — a level-4
edit, but a small one, and the only place where it is unavoidable.

**Upgrade cost.** Medium. The hook infrastructure (builder, factory,
`FormViewFragmentFactory`) is fork-owned and stable, but *where* the hook is
applied has already moved once (from `FormView` to `FormViewModel` in 3.4.1)
because Oslo relocated the mapping. Expect to re-point it, not to rewrite it.

---

## T2. Post-metadata-sync actions — running extra sync work after a metadata sync

> **Status: not implemented yet.** Documented here because two forks need it and
> the mechanism should be designed once. See B4 in
> `eyeseetea-docs/upgrade/simprints/upgrade-3.4-notes.md`.

**Use when:** a client has its own configuration or data that must be refreshed
every time the user syncs metadata — not only at login.

**Needed by:** Simprints (biometrics configuration) and WIDP (notifications).
Both previously hooked into `SyncPresenterImpl.syncMetadata()`, which Oslo
removed in 3.4.1 when metadata sync moved to the KMP `:sync` module.

**Why a mechanism is required.** There is no way to hook in from outside
`:sync`: it only depends on `:commonskmm`, so it cannot see fork code (`:app`
would be circular), and its consumer `SyncMetadataWorker` injects the concrete,
`final` `SyncMetadata` class, so a decorator registered in the fork's DI cannot
intercept it. Any solution has to add an extension point to baseline.

**Draft shape (not agreed).** A `PostMetadataSyncAction` contract in
`:commonskmm`, which `SyncMetadata` runs after a successful sync, with each fork
registering its own list in DI. Open questions — Koin's handling of an injected
`List<T>`, failure isolation, progress reporting, ordering, and whether to
generalise to other sync phases — are recorded alongside the draft in the notes
file above.

**Until it exists:** a fork in this situation has an accepted regression. Note
it explicitly in the upgrade notes rather than working around it locally, since
a local workaround would collide with every other fork's workaround.

---

## T3. Widening visibility of an Oslo type

**Use when:** the fork needs to construct or extend an Oslo class from a
different package, and Oslo declares it package-private.

**Used by:** Simprints — `BiometricsDuplicatesDialogModule` builds its own
`SearchRepositoryImpl` to resolve duplicate candidates, so the constructor is
made `public`.

**How it works.** Change the modifier and mark it:

```java
// EyeSeeTea customization - <spec title>
// Base behavior: the constructor is package-private, so only SearchTEModule can build it.
// Simprints behavior: <why the fork needs it from another package>
public SearchRepositoryImpl(...)
```

**Upgrade cost.** Low but silent. A one-word change is easy to lose when
resolving a conflict as `theirs` — it happened during the 3.4.1 upgrade and only
surfaced at compile time. The comment is what makes it findable.

**Prefer first:** placing the calling code in the same package, if that is
possible without dragging unrelated code along.

---

## T4. Extra constructor parameter on an Oslo class

**Use when:** fork logic inside a shared class needs a dependency Oslo does not
provide.

**Used by:** Simprints — `BasicPreferenceProvider` added to
`SearchRepositoryImpl` so `updateAttributeValue()` can persist the Simprints
GUID.

**How it works.** Append the parameter **last**, after all of Oslo's, and mark
it. Keeping Oslo's order untouched means the conflict on the next upgrade is
limited to the added line, and makes clear which parameter is the fork's.

Every caller must then pass it in that position — including any Dagger module
and any test that builds the class.

**Upgrade cost.** Medium. Oslo changing its own parameter list produces a
conflict here every time, but a mechanical one.

---

## T5. Copying an Oslo component instead of reusing it

**Use when:** never, if it can be avoided. Recorded here as an anti-pattern with
a real cost.

**Seen in:** Simprints — `BiometricsDuplicatesDialogHolder` was written by
copying `BaseTeiViewHolder` (118 vs 116 lines, no behavioural difference). Oslo
kept evolving the original; the copy silently rotted until it stopped compiling
three versions later.

**What it costs.** The copy does not conflict — it just drifts. Nothing points
from the original to the copy, so an upgrade that migrates the original leaves
the copy behind, and the failure surfaces far from the cause.

**Do instead:** subclass or extract the shared part, so an Oslo change either
propagates automatically or produces a real compile error at the point of
divergence.

**If a copy already exists:** when the original changes, diff the two and apply
the same change. During the 3.4.1 upgrade, baseline's own diff on
`BaseTeiViewHolder` was used as the exact translation table for the copy — that
is the cheapest way to catch up.

---

## How to add a technique here

Add an entry when you solve a shared-code customization in a way another fork
could reuse. Include:

- **Use when** — the problem it solves, not the implementation
- **Used by** — which fork(s), so the reader can look at a real example
- **How it works** — the mechanism, with the minimum code to recognise it
- **Upgrade cost** — what breaks when Oslo changes, and how loudly

If the technique required changing baseline, say so: the next fork needs to know
whether the hook already exists or has to be proposed.
