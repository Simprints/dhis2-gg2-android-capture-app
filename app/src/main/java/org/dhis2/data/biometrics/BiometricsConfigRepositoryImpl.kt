package org.dhis2.data.biometrics

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.dhis2.commons.biometrics.BiometricsIcon
import org.dhis2.commons.biometrics.BiometricsPreference
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.usescases.biometrics.entities.BiometricsConfig
import org.dhis2.usescases.biometrics.repositories.BiometricsConfigRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import timber.log.Timber

class BiometricsConfigRepositoryImpl(
    private val d2: D2,
    private val preferenceProvider: BasicPreferenceProvider,
    private val biometricsConfigApi: BiometricsConfigApi
) : BiometricsConfigRepository {

    override fun sync(): Flow<Unit> = flow {
        try {
            val configOptions = biometricsConfigApi.getData()


            preferenceProvider.saveAsJson(BiometricsPreference.CONFIGURATIONS, configOptions)
            Timber.d("BiometricsConfig synced!")
            Timber.d("BiometricsConfig: $configOptions")

            val userOrgUnitGroups =
                d2.organisationUnitModule().organisationUnits()
                    .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                    .withOrganisationUnitGroups()
                    .blockingGet().flatMap { ou ->
                        if (ou.organisationUnitGroups() != null) ou.organisationUnitGroups()!!
                            .map { ouGroup -> ouGroup.uid() }
                        else listOf()
                    }.distinct()

            preferenceProvider.saveAsJson(
                BiometricsPreference.USER_ORG_UNIT_GROUPS,
                userOrgUnitGroups
            )

            emit(Unit)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun getUserOrgUnitGroups(): Flow<List<String>> = flow {
        val listStringType = object : TypeToken<List<String>>() {}

        emit(
            preferenceProvider.getObjectFromJson(
                BiometricsPreference.USER_ORG_UNIT_GROUPS,
                listStringType,
                listOf()
            )
        )
    }

    override fun getBiometricsConfigs(): Flow<List<BiometricsConfig>> = flow {
        val biometricsConfigType = object : TypeToken<List<BiometricsConfig>>() {}

        emit(
            preferenceProvider.getObjectFromJson(
                BiometricsPreference.CONFIGURATIONS,
                biometricsConfigType,
                listOf()
            )
        )
    }

    override fun saveSelectedConfig(config: BiometricsConfig) : Flow<Unit> = flow {
        preferenceProvider.setValue(BiometricsPreference.ORG_UNIT_GROUP, config.orgUnitGroup)

        preferenceProvider.setValue(BiometricsPreference.PROGRAM, config.program)

        preferenceProvider.setValue(BiometricsPreference.PROJECT_ID, config.projectId)

        preferenceProvider.setValue(
            BiometricsPreference.CONFIDENCE_SCORE_FILTER,
            config.confidenceScoreFilter ?: 0
        )

        val icon =
            BiometricsIcon.values()
                .firstOrNull { it.name == config.icon?.toUpperCase() }?.name
                ?: BiometricsIcon.FINGERPRINT.name

        preferenceProvider.setValue(BiometricsPreference.ICON, icon)
        preferenceProvider.setValue(
            BiometricsPreference.LAST_VERIFICATION_DURATION,
            config.lastVerificationDuration ?: 0
        )
        preferenceProvider.setValue(
            BiometricsPreference.LAST_DECLINED_ENROL_DURATION,
            config.lastDeclinedEnrolDuration ?: 0
        )

        val orgUnitLevelAsModuleId = getOrgUnitLevelAsModuleId(config)

        preferenceProvider.setValue(
            BiometricsPreference.ORG_UNIT_LEVEL_AS_MODULE_ID,
            orgUnitLevelAsModuleId
        )

        preferenceProvider.setValue(
            BiometricsPreference.AGE_THRESHOLD_MONTHS,
            config.ageThresholdMonths
        )

        preferenceProvider.setValue(
            BiometricsPreference.DATE_OF_BIRTH_ATTRIBUTE,
            config.dateOfBirthAttribute
        )

        preferenceProvider.setValue(
            BiometricsPreference.BIOMETRICS_MODE,
            config.biometricsMode.name
        )

        Timber.d("downloadBiometricsConfig!")
        Timber.d("orgUnitGroup: ${config.orgUnitGroup}")
        Timber.d("program: ${config.program}")
        Timber.d("projectId: ${config.projectId}")
        Timber.d("confidenceScoreFilter: ${config.confidenceScoreFilter}")
        Timber.d("icon: $icon")
        Timber.d("lastVerificationDuration: ${config.lastVerificationDuration}")
        Timber.d("lastDeclinedEnrolDuration: ${config.lastDeclinedEnrolDuration}")
        Timber.d("orgUnitLevelAsModuleId: $orgUnitLevelAsModuleId")
        Timber.d("ageThresholdMonths: ${config.ageThresholdMonths}")
        Timber.d("dateOfBirthAttribute: ${config.dateOfBirthAttribute}")
        Timber.d("biometricsMode: ${config.biometricsMode.name}")

        emit(Unit)
    }

    private fun getOrgUnitLevelAsModuleId(config: BiometricsConfig): Int {
        return if (config.orgUnitLevelAsModuleId == null || config.orgUnitLevelAsModuleId > 0) {
            Timber.d("Invalid orgUnitLevelAsModuleId assigned, using default value 0")
            0
        } else {
            config.orgUnitLevelAsModuleId
        }
    }
}