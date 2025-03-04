package org.dhis2.usescases.login

import org.dhis2.usescases.biometrics.BIOMETRICS_ENABLED
import org.dhis2.usescases.biometrics.repositories.BiometricsConfigRepository
import javax.inject.Inject

class SyncBiometricsConfig @Inject constructor(
    private val biometricsConfigRepository: BiometricsConfigRepository,
) {
    operator fun invoke() {
        if (BIOMETRICS_ENABLED) {
            biometricsConfigRepository.sync()
        }
    }

}
