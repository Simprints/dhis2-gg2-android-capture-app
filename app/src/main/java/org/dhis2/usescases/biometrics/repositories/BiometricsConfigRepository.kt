package org.dhis2.usescases.biometrics.repositories

import kotlinx.coroutines.flow.Flow
import org.dhis2.usescases.biometrics.entities.BiometricsConfig

interface BiometricsConfigRepository {
    fun sync(): Flow<Unit>
    fun getUserOrgUnitGroups():  Flow<List<String>>
    fun getBiometricsConfigs():  Flow<List<BiometricsConfig>>
    fun saveSelectedConfig(config: BiometricsConfig): Flow<Unit>
}