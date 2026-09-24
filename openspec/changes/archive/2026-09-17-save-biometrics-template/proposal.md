## Why

Simprints (SID) generates a face biometric template (`face_roc_v3`, base64
string) for every subject it enrolls, but the app currently discards it —
only the biometric GUID and the NHIS number are persisted to DHIS2 today.
The client wants the template stored for future analytics/reporting on
biometric matching quality. This proposal defines where and how that
template is captured and persisted, following the same hidden-attribute
pattern already used for the GUID and NHIS number.

## What Changes

- Add a new hidden Tracked Entity Attribute that stores the biometric
  face template(s) returned by SID, on the `Person` TET, following the
  same pattern as the existing `Biometrics` (GUID) and NHIS Number
  attributes.
- SID returns **two templates per enrollment** (one per captured face
  frame, 2 by default) — both must be persisted in a **single** attribute
  (not two separate attributes), most likely as a JSON value containing
  both base64 template strings. Exact JSON shape is an open decision (see
  design.md).
- Write the attribute directly via the SDK (no form/ViewModel round-trip),
  mirroring `updateNHISNumberAttributeValue.kt`, triggered from the same
  point as the existing GUID/NHIS writes (`onBiometricsCompleted()`),
  which already covers both plain registration and "register last"
  (search → New person → "use last biometrics").
- Hide the new attribute via a **program rule** rather than a hardcoded
  type check, per PM guidance — a departure from how the existing
  `Biometrics` attribute is hidden today (100% hardcoded, no program
  rules).
- **BREAKING**: none. This is purely additive — no existing attribute,
  write path, or UI behavior is modified.

## Capabilities

### New Capabilities

- `biometrics-template-storage`: capturing the biometric template(s)
  returned by Simprints during enrollment and persisting them to a new
  hidden DHIS2 Tracked Entity Attribute, hidden via program rule.

### Modified Capabilities

_None._ This does not change the requirements of any existing spec
(`biometric-search-integration`, `biometrics-tei-ui-surfaces`, etc.) — it
adds a new, independent write path that reuses the existing
`onBiometricsCompleted()` trigger without altering its current behavior
for the GUID or NHIS number.

## Impact

- **Shared code (conflict surface)**: new constant in
  `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/biometrics/attributes.kt`
  (append, low conflict risk — same file already holds
  `biometricAttributeId`/`nhisNumberAttributeId`); a new
  `updateBiometricTemplateAttributeValue` use case/function analogous to
  `updateNHISNumberAttributeValue.kt` (new file, no Oslo file touched);
  one additional call from `onBiometricsCompleted()` in
  `EnrollmentPresenterImpl.kt` and `TEIDataPresenter.kt` (inline edit,
  small — appends a third call alongside the existing GUID/NHIS calls).
- **Metadata**: new TEA to be created on the `Person` TET and linked as
  `programTrackedEntityAttribute` on `0.0 General Registration` and
  `1.6 Child Health`, plus a new program rule to hide it. Metadata
  creation and UID assignment are external (owned by EyeSeeTea/Jorge, per
  PM decision) and out of scope for the code change itself, but the UID
  is a hard dependency before implementation can be hardcoded.
- **Tests**: `onBiometricsCompleted()` has no coverage today beyond intent
  parsing in `BiometricsClientTest`; this change requires adding coverage
  for the new write call.
- **No impact** on `app/src/simprints/` flavor resources — this is a
  data-layer change, not a UI/config one.
