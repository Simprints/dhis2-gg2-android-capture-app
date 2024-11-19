package org.dhis2.data.biometrics

import com.google.gson.reflect.TypeToken
import org.dhis2.commons.biometrics.BiometricsPreference
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.usescases.biometrics.entities.BiometricsConfig
import org.dhis2.usescases.biometrics.entities.BiometricsMode

fun getBiometricsConfig(preferenceProvider: BasicPreferenceProvider): BiometricsConfig {
    val orgUnitGroup = preferenceProvider.getString(BiometricsPreference.ORG_UNIT_GROUP, "")
    val projectId = preferenceProvider.getString(BiometricsPreference.PROJECT_ID, "") ?: ""
    val confidenceScoreFilter =
        preferenceProvider.getInt(BiometricsPreference.CONFIDENCE_SCORE_FILTER, 0)
    val icon = preferenceProvider.getString(BiometricsPreference.ICON, "")
    val lastVerificationDuration =
        preferenceProvider.getInt(BiometricsPreference.LAST_VERIFICATION_DURATION, 0)
    val lastDeclinedEnrolDuration =
        preferenceProvider.getInt(BiometricsPreference.LAST_DECLINED_ENROL_DURATION, 0)
    val program = preferenceProvider.getString(BiometricsPreference.PROGRAM, "")
    val orgUnitLevelAsModuleId =
        preferenceProvider.getInt(BiometricsPreference.ORG_UNIT_LEVEL_AS_MODULE_ID, 0)
    val ageThresholdMonths =
        preferenceProvider.getInt(BiometricsPreference.AGE_THRESHOLD_MONTHS, 0)
    val dateOfBirthAttribute =
        preferenceProvider.getString(BiometricsPreference.DATE_OF_BIRTH_ATTRIBUTE, "") ?:""

    val biometricsMode =
        preferenceProvider.getString(BiometricsPreference.BIOMETRICS_MODE, BiometricsMode.full.name)
            ?: BiometricsMode.full.name

    return BiometricsConfig(
        orgUnitGroup,
        projectId,
        confidenceScoreFilter,
        icon,
        lastVerificationDuration,
        lastDeclinedEnrolDuration,
        program,
        orgUnitLevelAsModuleId,
        ageThresholdMonths,
        dateOfBirthAttribute,
        BiometricsMode.valueOf(biometricsMode)
    )
}

fun getBiometricsConfigByProgram(preferenceProvider: BasicPreferenceProvider, programUid:String): BiometricsConfig? {
    val biometricsConfigType = object : TypeToken<List<BiometricsConfig>>() {}

    val configs =  preferenceProvider.getObjectFromJson(
        BiometricsPreference.CONFIGURATIONS,
        biometricsConfigType,
        listOf()
    )

    return configs.find { it.program == programUid }
}
