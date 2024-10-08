package org.dhis2.data.biometrics

import org.dhis2.commons.biometrics.BiometricsPreference
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.usescases.biometrics.entities.BiometricsParentChildConfig
import org.dhis2.usescases.biometrics.repositories.BiometricsParentChildConfigRepository
import timber.log.Timber

class BiometricsParentChildConfigRepositoryImpl(
    private val preferenceProvider: BasicPreferenceProvider,
    private val biometricsParentChildConfigApi: BiometricsParentChildConfigApi
) : BiometricsParentChildConfigRepository {
    override fun sync() {
        try {
            val response = biometricsParentChildConfigApi.getData().execute()

            val parentChildConfig = response.body()

            if (response.isSuccessful && parentChildConfig != null) {
                preferenceProvider.setValue(
                    BiometricsPreference.PARENT_CHILD_RELATIONSHIP,
                    parentChildConfig.parentChildRelationship
                )
                preferenceProvider.setValue(
                    BiometricsPreference.AGE_THRESHOLD,
                    parentChildConfig.ageThresholdMonths
                )
                preferenceProvider.saveAsJson(
                    BiometricsPreference.DATE_OF_BIRTH_ATT_BY_PROGRAM,
                    parentChildConfig.dateOfBirthAttributeByProgram
                )

                Timber.d("downloadBiometricsParentChildConfig!")
                Timber.d("parentChildRelationship: ${parentChildConfig.parentChildRelationship}")
                Timber.d("ageThresholdMonths: ${parentChildConfig.ageThresholdMonths}")
                Timber.d("dateOfBirthAttributeByProgram: ${parentChildConfig.dateOfBirthAttributeByProgram}")
            } else {
                Timber.e(response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun get(): BiometricsParentChildConfig {
        return getBiometricsParentChildConfig(preferenceProvider);
    }
}