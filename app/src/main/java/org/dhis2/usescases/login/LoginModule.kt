package org.dhis2.usescases.login

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.biometrics.BiometricsConfigApi
import org.dhis2.data.biometrics.BiometricsConfigRepositoryImpl
import org.dhis2.data.fingerprint.FingerPrintController
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.biometrics.repositories.BiometricsConfigRepository
import org.dhis2.usescases.login.auth.OpenIdProviders
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager

@Module
class LoginModule(
    private val view: LoginContracts.View,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val userManager: UserManager?,
) {

    @Provides
    @PerActivity
    fun provideResourceManager(
        colorUtils: ColorUtils,
    ) = ResourceManager(view.context, colorUtils)

    @Provides
    @PerActivity
    fun provideD2(): D2 {
        return D2Manager.getD2()
    }

    @Provides
    @PerActivity
    fun provideBiometricsConfigRepository(
        d2: D2,
        basicPreferences: BasicPreferenceProvider
    ): BiometricsConfigRepository {
        val biometricsConfigApi = d2.retrofit().create(
            BiometricsConfigApi::class.java
        )
        return BiometricsConfigRepositoryImpl(d2, basicPreferences, biometricsConfigApi)
    }

    @Provides
    @PerActivity
    fun providePresenter(
        preferenceProvider: PreferenceProvider,
        resourceManager: ResourceManager,
        schedulerProvider: SchedulerProvider,
        dispatcherProvider: DispatcherProvider,
        fingerPrintController: FingerPrintController,
        analyticsHelper: AnalyticsHelper,
        crashReportController: CrashReportController,
        networkUtils: NetworkUtils,
        syncBiometricsConfig: SyncBiometricsConfig,
    ): LoginViewModel {
        return ViewModelProvider(
            viewModelStoreOwner,
            LoginViewModelFactory(
                view,
                preferenceProvider,
                resourceManager,
                schedulerProvider,
                dispatcherProvider,
                fingerPrintController,
                analyticsHelper,
                crashReportController,
                networkUtils,
                userManager,
                syncBiometricsConfig,
            ),
        )[LoginViewModel::class.java]
    }

    @Provides
    @PerActivity
    fun openIdProviders(context: Context): OpenIdProviders {
        return OpenIdProviders(context)
    }
}
