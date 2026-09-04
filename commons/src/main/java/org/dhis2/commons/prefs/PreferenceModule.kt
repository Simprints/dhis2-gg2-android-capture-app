package org.dhis2.commons.prefs

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class PreferenceModule {
    @Provides
    @Singleton
    open fun preferenceProvider(context: Context): PreferenceProvider = PreferenceProviderImpl(context)

    // EyeSeeTea customization - Biometrics Configuration Selection Per Program Or Org Unit Group
    // Base behavior: develop-eyeseetea removed BasicPreferenceProvider entirely (commit 1bb3974ca1).
    // Simprints behavior: the selected biometrics configuration is flattened into preferences and read
    // back through this provider by the biometrics surface (AgeInMonths, OrgUnitAsModuleId,
    // BiometricsClientFactory, EnrollmentPresenterImpl, TEIDataPresenter, TEICardMapper), so the
    // abstraction is restored here. Migrating that surface to PreferenceProvider is deferred to a
    // separate commit after this upgrade closes.
    @Provides
    @Singleton
    open fun basicPreferenceProvider(context: Context): BasicPreferenceProvider = BasicPreferenceProviderImpl(context)
}
