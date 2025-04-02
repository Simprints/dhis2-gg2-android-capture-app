package org.dhis2.data.biometrics.utils

import com.google.gson.reflect.TypeToken
import org.dhis2.commons.bindings.blockingSetCheck
import org.dhis2.commons.biometrics.BiometricsPreference
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.data.biometrics.getBiometricsConfig
import org.dhis2.usescases.biometrics.isLastVerificationValid
import org.hisp.dhis.android.core.D2

fun updateBiometricsAttributeValue(
    d2: D2,
    basicPreferenceProvider: BasicPreferenceProvider,
    teiUid: String,
    value: String
) {
    val tei = d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()
        ?: return

    val attributeUid = getBiometricsTrackedEntityAttribute(d2)

    if (attributeUid != null) {
        val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(attributeUid, tei.uid())

        updateVerification(basicPreferenceProvider, teiUid)

        valueRepository.blockingSetCheck(d2, attributeUid, value)
    }
}

fun updateVerification(basicPreferenceProvider: BasicPreferenceProvider, teiUid: String) {
    val biometricsConfig = getBiometricsConfig(basicPreferenceProvider)
    val verifications = getVerifications(basicPreferenceProvider).toMutableList()

    val newVerifications = calculateNewVerifications(
        teiUid,
        verifications,
        biometricsConfig.lastVerificationDuration
    )

    basicPreferenceProvider.saveAsJson(
        BiometricsPreference.BIOMETRICS_VERIFICATIONS,
        newVerifications
    )
}

fun calculateNewVerifications(
    teiUid: String,
    oldVerifications: List<BiometricsVerification>,
    lastVerificationDuration: Int?
): List<BiometricsVerification> {

    val activeVerifications = filterActiveVerifications(lastVerificationDuration, oldVerifications)

    val newVerification = BiometricsVerification(teiUid, System.currentTimeMillis())
    val newVerifications = activeVerifications.filter { it.teiUid != teiUid } + newVerification

    return newVerifications
}

private fun filterActiveVerifications(
    lastVerificationDuration: Int?,
    verifications: List<BiometricsVerification>
): List<BiometricsVerification> {
    return verifications.filter { verification ->
        isLastVerificationValid(
            verification.date,
            lastVerificationDuration ?: 0,
            false
        )
    }
}

private fun getVerifications(basicPreferenceProvider: BasicPreferenceProvider): List<BiometricsVerification> {
    val listStringType = object : TypeToken<List<BiometricsVerification>>() {}

    return basicPreferenceProvider.getObjectFromJson(
        BiometricsPreference.BIOMETRICS_VERIFICATIONS,
        listStringType,
        listOf()
    )
}

fun getVerification(
    basicPreferenceProvider: BasicPreferenceProvider,
    teiUId: String
): BiometricsVerification? {
    val verifications = getVerifications(basicPreferenceProvider)

    return verifications.find { it.teiUid == teiUId }
}

data class BiometricsVerification(val teiUid: String, val date: Long)