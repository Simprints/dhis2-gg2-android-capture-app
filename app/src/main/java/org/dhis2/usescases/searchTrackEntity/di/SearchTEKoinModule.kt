package org.dhis2.usescases.searchTrackEntity.di

import org.koin.dsl.module

/**
 * Koin module for Search Tracked Entity feature.
 *
 * Note: This module coexists with the legacy Dagger SearchTEModule during the migration.
 *
 * EyeSeeTea customization - Biometric Search Integration
 *
 * Base behavior: baseline defines `SearchTeiViewModelFactory` and `SearchTEIViewModel`
 * here as part of the in-progress Dagger -> Koin migration.
 *
 * Simprints behavior: both definitions are removed while the migration is unfinished.
 * `SearchTEIViewModel` takes three parameters baseline's definitions do not pass —
 * `presenter` (kept from the pre-migration code, pending a future refactor),
 * `basicPreferenceProvider` and `fromRelationships` (biometrics customizations) — and
 * none of the three is resolvable from Koin: `presenter` and `BasicPreferenceProvider`
 * exist only in Dagger, and `fromRelationships` depends on how the screen was opened.
 *
 * Nothing consumes these definitions today: `SearchTEActivity` injects
 * `SearchTeiViewModelFactory` through Dagger (`@Inject lateinit var viewModelFactory`),
 * and no caller resolves the ViewModel or its factory through Koin. Writing wiring that
 * never executes would only look functional without being so.
 *
 * Restore both definitions — passing the three parameters above — when baseline finishes
 * the migration and removes the Dagger `SearchTEModule`.
 */
val searchTEKoinModule =
    module {
        // Intentionally empty — see the note above.
    }
