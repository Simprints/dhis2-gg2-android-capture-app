# Biometric Search Integration

## Purpose

Field users need to identify a tracked entity instance (TEI) using biometric data
instead of, or in addition to, demographic attribute search. The stock DHIS2
Android client has no concept of an external biometric identification provider;
this fork integrates the Simprints biometric app into the TEI search workflow so
identification results returned by Simprints can be resolved into normal DHIS2
search results, keeping duplicate review and follow-up TEI search on the same
DHIS2 search infrastructure as any other search.

## Requirements

### Requirement: Launch biometric identification from search
The app SHALL allow the TEI search screen to launch the external Simprints
biometric app with the current module and organisation unit context.

#### Scenario: User starts a biometric search
- **WHEN** the user triggers biometric search from the search screen
- **THEN** the app launches the Simprints biometric app passing the resolved module id and organisation unit context

### Requirement: Search UI stays consistent while the biometric app is active
The app SHALL avoid showing a stale or ambiguous search list while control has
been handed off to the external biometric app.

#### Scenario: Simprints app is being launched
- **WHEN** the biometric app is being launched from search
- **THEN** the search list is hidden and a dedicated loader is shown instead

### Requirement: Convert Simprints identification results into a DHIS2 search
The app SHALL convert biometric identification results returned by Simprints into
a DHIS2 tracked entity search and continue the search workflow with those
results.

#### Scenario: Simprints returns identification candidates
- **WHEN** Simprints returns one or more candidate identifiers for an identification request
- **THEN** the app runs a DHIS2 search using those identifiers and shows the resulting TEIs in the normal search result list

### Requirement: Biometric identifiers are DHIS2 attribute values, not a side cache
The app SHALL treat Simprints candidate identifiers as values of the configured
biometrics tracked entity attribute rather than maintaining a separate local
cache of biometric matches.

#### Scenario: Reviewing a returned candidate
- **WHEN** a Simprints candidate identifier is resolved to a TEI
- **THEN** the match is found through the normal DHIS2 search index on the biometrics tracked entity attribute, not through a fork-only in-memory store
