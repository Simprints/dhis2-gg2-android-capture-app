package org.dhis2.usescases.login

import android.content.Context
import android.content.ContextWrapper
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.prefs.BasicPreferenceProviderImpl
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.biometrics.BiometricsConfigApi
import org.dhis2.data.biometrics.BiometricsConfigRepositoryImpl
import org.dhis2.data.server.ServerComponent
import org.dhis2.ui.ThemeManager
import org.dhis2.usescases.biometrics.repositories.BiometricsConfigRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager

@Module
class LoginModule internal constructor(
    private val context: Context,
    private val serverComponent: ServerComponent,
) {
    @Provides
    @PerActivity
    fun provideResourceManager(themeManager: ThemeManager): ResourceManager {
        return themeManager.let {
            val colorUtils = ColorUtils()
            val contextWrapper = ContextWrapper(context)
            contextWrapper.setTheme(it.getAppTheme())
            ResourceManager(contextWrapper, colorUtils)
        }
    }

    @Provides
    @PerActivity
    fun provideBiometricsConfigRepository(
        basicPreferenceProvider: BasicPreferenceProvider,
    ): BiometricsConfigRepository {
        val d2 = serverComponent.getD2()

        val biometricsConfigApi = BiometricsConfigApi(d2.httpServiceClient())
        return BiometricsConfigRepositoryImpl(d2, basicPreferenceProvider, biometricsConfigApi)
    }

    @Provides
    @PerActivity
    fun provideSyncBiometricsConfig(
        biometricsConfigRepository: BiometricsConfigRepository,
    ): SyncBiometricsConfig {
        return SyncBiometricsConfig(biometricsConfigRepository)
    }
}

