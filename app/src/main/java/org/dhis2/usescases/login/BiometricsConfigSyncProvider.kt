package org.dhis2.usescases.login

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.biometrics.BIOMETRICS_ENABLED
import org.dhis2.usescases.biometrics.repositories.BiometricsConfigRepository
import javax.inject.Inject

class BiometricsConfigSyncProvider @Inject constructor(
    private val biometricsConfigRepository: BiometricsConfigRepository,
) {
    fun syncBiometricsConfig(dispatcher: DispatcherProvider) {
        if (BIOMETRICS_ENABLED) {
            CoroutineScope(dispatcher.io()).launch {
                biometricsConfigRepository.sync()
            }
        }
    }

}
