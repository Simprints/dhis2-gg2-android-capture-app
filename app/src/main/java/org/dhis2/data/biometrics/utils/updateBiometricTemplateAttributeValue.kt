package org.dhis2.data.biometrics.utils

import com.google.gson.Gson
import org.dhis2.bindings.blockingSetCheck
import org.dhis2.data.biometrics.biometricsClient.models.BiometricReference
import org.dhis2.mobile.commons.biometrics.biometricTemplateAttributeId
import org.hisp.dhis.android.core.D2

internal data class BiometricReferencesValue(
    val biometricReferences: List<BiometricReference>,
)

fun updateBiometricTemplateAttributeValue(
    d2: D2,
    teiUid: String,
    biometricReferences: List<BiometricReference>,
) {
    if (biometricReferences.isEmpty()) return

    val value = Gson().toJson(BiometricReferencesValue(biometricReferences))

    val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
        .value(biometricTemplateAttributeId, teiUid)

    valueRepository.blockingSetCheck(d2, biometricTemplateAttributeId, value)
}
