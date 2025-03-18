package org.dhis2.usescases.login

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.dhis2.usescases.biometrics.BIOMETRICS_ENABLED
import org.dhis2.usescases.biometrics.repositories.BiometricsConfigRepository
import javax.inject.Inject

class SyncBiometricsConfig @Inject constructor(
    private val biometricsConfigRepository: BiometricsConfigRepository,
) {
    operator fun invoke(): Flow<Unit> = flow {
        if (BIOMETRICS_ENABLED) {
            biometricsConfigRepository.sync()
        }

        emit(Unit)
    }
}
