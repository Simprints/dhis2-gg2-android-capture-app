## Context

See proposal.md - Why. This section only covers the current implementation
state the design builds on.

Today, `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/biometrics/attributes.kt`
hardcodes two attribute UIDs: `biometricAttributeId` (the GUID) and
`nhisNumberAttributeId`. Both are written directly to the SDK — no form/
ViewModel round-trip — from `onBiometricsCompleted()`
(`EnrollmentPresenterImpl.kt:362`, `TEIDataPresenter.kt:862`), guarded by
`RegisterResult.Completed` and `item.hasCredential && item.scannedCredential
?.type != null`. `nhisNumberAttributeId` is written via
`updateNHISNumberAttributeValue.kt:7-16`, a single function shared by both
presenters, using
`d2.trackedEntityModule().trackedEntityAttributeValues().value(attrUid, teiUid).blockingSetCheck(...)`.

The `guid` and `scannedCredential` values available in
`SimprintsRegisteredItem` (`.../biometricsClient/models/SimprintsRegisteredItem.kt:3-7`)
come from the `enrolment` and `scannedCredential` extras of the intent
Simprints returns, parsed in `BiometricsClient.handleRegisterResponse`
(`BiometricsClient.kt:178-270`). That same intent also carries a
**`subjectActions`** extra — confirmed present on a real enrollment
capture — which the app does not currently read at all (verified: no
reference to `template`, `face_roc`, or `subjectActions` anywhere in
`data/biometrics/`). This is where the face templates live; see the
Decisions section for the confirmed payload shape.

`onBiometricsCompleted()` is reached by two distinct UI flows that both
resolve to `RegisterResult.Completed`:
1. Plain registration (new TEI, no prior biometric search).
2. "Register last" — biometric search → "New person"/"New patient" →
   "use last biometrics" checkbox (`BiometricsTEIRegistration.kt:36,66-81`)
   → `registerLast()`/`registerLastFromFragment()` in `BiometricsClient.kt`,
   routed through the same `handleRegisterResponse()` in both
   `EnrollmentActivity.kt:214-220` and `TEIDataFragment.kt:484-488`.

A third, separate flow exists and is **not** wired to
`onBiometricsCompleted()` today: `confirmIdentify()` /
`ConfirmIdentityResult` (`SearchTEActivity.kt:878-968`), which confirms the
identity of an **already existing** TEI selected from biometric search
results (no new TEI is created). On success it calls
`presenter.updateTEICredentials(...)`, which today does not touch the GUID
or NHIS attributes.

`confirmIdentify` is explicitly out of scope for this change (see
Non-Goals and the corresponding spec requirement) — the template attribute
is only written from the two registration flows above.

`Biometrics` (the GUID attribute) is hidden from the form today via a
hardcoded type check, not a program rule (confirmed against real Ghana
instance metadata — none of the 22 program rules on the two relevant
programs reference it). The PM originally asked for the new template
attribute to be hidden via a program rule instead, to avoid growing the
hardcoded filter — this was tried and reverted; see the "Hiding mechanism"
decision below for why.

## Goals / Non-Goals

**Goals:**
- Persist both face templates from a single enrollment into one new hidden
  TEA, using the existing NHIS-style direct-SDK write pattern.
- Hide the new attribute from the form without losing the value written
  directly to the SDK.
- Keep the change additive: zero modification to existing GUID/NHIS write
  behavior or to any existing spec's requirements.

**Non-Goals:**
- Writing the template from the `confirmIdentify` (existing-TEI) flow —
  out of scope, see the corresponding spec requirement. Only the two
  registration flows above write this attribute.
- Building the DHIS2 metadata (attribute, program rule) itself — metadata
  creation is external, owned by EyeSeeTea (Jorge) per PM decision, and
  tracked as a prerequisite task, not part of this design.
- Any analytics/reporting consumption of the stored template — out of
  scope; this design only covers capture and storage.

## Decisions

### Single attribute, JSON-encoded, for both templates
Per PM preference ("if we can save multiple templates in the same
attribute, that would be better"), both templates from one enrollment are
stored in **one** attribute rather than two. Alternatives considered:
- Two separate attributes (one per template slot): rejected — PM
  explicitly prefers one attribute, and a fixed "template 1 / template 2"
  split doesn't generalize if the capture count ever changes.
- Delimited string (e.g. comma-joined base64): rejected — harder to
  extend/parse for analytics than JSON, no real simplicity gain over JSON
  for a two-element structure.

**Resolved shape** (captured from a real enrollment intent, not assumed —
see below): the app SHALL persist the `biometricReferences` array exactly
as returned inside `subjectActions` (the intent extra Simprints already
sends today, currently unparsed by the app), unmodified:

```json
{
  "biometricReferences": [
    {
      "type": "FACE_REFERENCE",
      "format": "RANK_ONE_3_1",
      "templates": [
        { "template": "<base64>" },
        { "template": "<base64>" }
      ]
    }
  ]
}
```

This was chosen over a flattened projection (e.g. `{"format": ...,
"templates": ["...", "..."]}`) to stay faithful to the source structure
Simprints actually returns: `biometricReferences` is an array (not
guaranteed to always contain exactly one `FACE_REFERENCE` entry — other
biometric types could appear in the future), and each template is an
object `{"template": "..."}`, not a bare string. `format` is read verbatim
from the SDK response (confirmed real value: `RANK_ONE_3_1`, not the
`face_roc_v3` name used in informal client communication — do not hardcode
`face_roc_v3` anywhere). The per-reference `id` field present in the raw
`subjectActions` payload is intentionally dropped — it carries no known
business value and would only add sync/storage weight.

Confirmed real payload structure — `subjectActions` (an intent extra
already sent by Simprints, currently unparsed by
`BiometricsClient.handleRegisterResponse`) contains
`{schemaVersion, events: [{type: "EnrolmentRecordCreation", id, payload:
{subjectId, projectId, moduleId, attendantId, biometricReferences: [...],
externalCredentials: [...]}}]}`. Only `payload.biometricReferences` is
relevant to this design; `externalCredentials` overlaps with the existing
NHIS/`scannedCredential` handling and is out of scope here.

### Value type: LONG_TEXT
`TEXT` caps at 50,000 characters; a JSON payload containing two base64
face templates is likely to approach or exceed that, and the client's
stated intent (future analytics) argues against `FILE_RESOURCE` (opaque
blob, not queryable in SDK-exposed tables). `LONG_TEXT` has no length cap
and remains a normal TEA value type for SDK/analytics access.

### Write path: mirror `updateNHISNumberAttributeValue`, new function
A new `updateBiometricTemplateAttributeValue` function, shaped like
`updateNHISNumberAttributeValue.kt` (direct `blockingSetCheck` write, no
form), added as a new file per placement hierarchy option 2 — avoids
touching the existing NHIS file and keeps this customization independently
greppable/removable. Called once more from `onBiometricsCompleted()` in
both presenters, appended alongside the existing GUID/NHIS calls (inline
edit, placement hierarchy option 4 — unavoidable here since
`onBiometricsCompleted()` is the one true trigger point, same as the
existing two attributes).

**Sourcing the value**: unlike the GUID and NHIS number, the template data
is not already available on `SimprintsRegisteredItem` — it requires
parsing the previously-unread `subjectActions` extra. This means:
1. `BiometricsClient.handleRegisterResponse` (`BiometricsClient.kt:178-270`)
   needs to additionally read the `subjectActions` extra and parse
   `payload.biometricReferences` out of it (new DTOs mirroring the
   confirmed shape: `type`, `format`, `templates: [{template}]`).
2. `SimprintsRegisteredItem` needs a new field carrying the parsed
   `biometricReferences` (or `null`/empty if `subjectActions` is absent or
   unparseable — the write path must tolerate that, since `subjectActions`
   is not validated as always present today).
3. `onBiometricsCompleted()` passes that field to
   `updateBiometricTemplateAttributeValue`, which serializes it to JSON in
   the shape below and writes it, guarded by the same
   `hasCredential`/`scannedCredential` condition already used for
   GUID/NHIS — plus tolerating a missing/empty `biometricReferences`
   (skip the write rather than persist an empty value).

This is a larger touch on `BiometricsClient.kt` than the GUID/NHIS pattern
suggested at first glance (that file only reads `enrolment` and
`scannedCredential` today), but it stays within the same file/function
already responsible for parsing this exact intent — no new parsing
entry point is introduced.

### Hiding mechanism: code filter, not a program rule

**Tried first, reverted**: an unconditional `HIDEFIELD` program rule
(`condition: "true"`) on `BP90qVFNazj`, created on both `0.0 General
Registration` and `1.6 Child Health` in `simprints-dev`. This is
structurally incompatible with a directly-SDK-written attribute:

- `RulesUtilsProviderImpl.hideField()`
  (`form/src/main/java/org/dhis2/form/data/RulesUtilsProviderImpl.kt:274-284`)
  reacts to `HIDEFIELD` by setting `valuesToChange[field] = null` for the
  hidden attribute — this is the mechanism the rule engine uses to blank
  out fields the user can no longer see.
- That `null` is applied when the enrollment form is saved/completed
  (`EnrollmentPresenterImpl.finish()` → `enrollmentFormRepository
  .generateEvents()`), which runs **after** `onBiometricsCompleted()` has
  already written the real template value via
  `updateBiometricTemplateAttributeValue`.
- Verified end-to-end on a real device: `updateBiometricTemplateAttributeValue`
  logged `blockingSetCheck result=true` and the value was present in
  `TrackedEntityAttributeValue` immediately after the write, but had been
  wiped back to absent a few seconds later, once the enrollment finished
  saving — confirmed by inspecting the local SQLite DB before/after.

In short: any `HIDEFIELD` rule on this attribute — unconditional or not —
causes the rule engine to blank it out on every form save, unconditionally
overwriting the direct SDK write. This is not a configuration mistake to
fix; it's a fundamental mismatch between "field hidden by rule engine" and
"field written outside the form's data flow".

**Resolved**: hide the field by code instead, in
`EnrollmentPresenterImpl.onFieldsLoading()`
(`app/src/main/java/org/dhis2/usescases/enrollment/EnrollmentPresenterImpl.kt:577`),
which already filters `BiometricsAttributeUiModelImpl` out of the field
list depending on `biometricsMode`. `BP90qVFNazj` is now filtered out by
UID unconditionally, before the rule engine or the UI ever see it — so
there is no `RuleEffect` to blank the value, and nothing to revert. The two
`HIDEFIELD` program rules created in `simprints-dev` for this purpose were
deleted.

### Attribute UID sourcing
The UID is created by EyeSeeTea (Jorge), generated once and shared with the
client so their instance(s) create the identical UID — this mirrors how
`biometricAttributeId`/`nhisNumberAttributeId` are sourced today and is a
hard prerequisite: the UID cannot be hardcoded until it exists.

**Resolved**: attribute created as `Biometrics Template`
(`BiometricsTemplate`), UID `BP90qVFNazj`, `valueType: LONG_TEXT`, on the
`simprints-dev` instance, and already linked as
`programTrackedEntityAttribute` on both `0.0 General Registration`
(`Cej0ai1pPei`) and `1.6 Child Health` (`o5kwD20X1zR`). Still pending:
sharing this same UID with the client so it is created identically on
their instance(s) before this ships to them.

## Risks / Trade-offs

- **[Risk]** Hardcoding a UID that doesn't exist yet blocks implementation
  entirely. → **Mitigation**: track UID creation as an explicit blocking
  task (see tasks.md); do not start the code change until the UID is
  confirmed.
- **[Risk]** `subjectActions` is not validated as always present or always
  containing a `FACE_REFERENCE` entry (only confirmed from one real
  capture) — a future SDK/config change could omit it or change its
  schema. → **Mitigation**: the write path treats missing/unparseable
  `biometricReferences` as "skip the write", not a crash; add a test for
  that case (see tasks.md).
- **[Risk]** If a future requirement needs the template updated when
  confirming the identity of an existing TEI (`confirmIdentify`), this
  design's write path misses it entirely — that flow uses a different
  result type (no `RegisterResult`/`onBiometricsCompleted` involved). →
  **Mitigation**: out of scope by explicit spec requirement, not a gap;
  revisit as a separate, additive change if this ever becomes a
  requirement.
- **[Risk]** `onBiometricsCompleted()` and the write path it triggers have
  no test coverage beyond intent parsing today. → **Mitigation**: tasks.md
  includes adding coverage for the new call, per `android-testing` skill
  guidance.

