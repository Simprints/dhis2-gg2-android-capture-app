package org.dhis2.usescases.biometrics.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.dhis2.usescases.biometrics.entities.BiometricsConfig
import org.dhis2.usescases.biometrics.repositories.BiometricsConfigRepository
import timber.log.Timber
import java.util.Locale

class SelectBiometricsConfig(private val biometricsConfigRepository: BiometricsConfigRepository) {
    operator fun invoke(program: String): Flow<Unit> = flow {
        val configOptions = biometricsConfigRepository.getBiometricsConfigs().first()
        val userOrgUnitGroups = biometricsConfigRepository.getUserOrgUnitGroups().first()
        val config = getSelectedConfig(configOptions, program, userOrgUnitGroups)

        biometricsConfigRepository.saveSelectedConfig(config).collect {
            emit(Unit)
        }
    }

    private fun getSelectedConfig(
        configOptions: List<BiometricsConfig>,
        program: String,
        userOrgUnitGroups: List<String>
    ): BiometricsConfig {
        val defaultConfig =
            getDefaultConfig(configOptions)

        val configByProgram = configOptions.find { it.program == program }

        val userOrgUnitGroupsInConfig =
            userOrgUnitGroups.filter { ouGroup -> configOptions.any { config -> config.orgUnitGroup == ouGroup } }

        val configByUserOrgUnitGroup =
            configOptions.find { userOrgUnitGroupsInConfig.isNotEmpty() && it.orgUnitGroup == userOrgUnitGroupsInConfig[0] }

        return configByProgram ?: if (userOrgUnitGroupsInConfig.size != 1) defaultConfig else
            configByUserOrgUnitGroup
                ?: defaultConfig
    }

    private fun getDefaultConfig(configOptions: List<BiometricsConfig>): BiometricsConfig {
        val defaultConfig =
            configOptions.find { it.orgUnitGroup?.lowercase(Locale.getDefault()) == "default" }

        if (defaultConfig == null) {
            val error = "There are not a default biometrics config"
            Timber.e(error)
            throw Exception(error)
        }
        return defaultConfig
    }
}