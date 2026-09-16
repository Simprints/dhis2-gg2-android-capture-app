package org.dhis2.data.biometrics.utils

import com.google.gson.Gson
import org.dhis2.data.biometrics.biometricsClient.models.BiometricReference
import org.dhis2.data.biometrics.biometricsClient.models.BiometricTemplate
import org.dhis2.data.biometrics.givenTemplateAttributeValueType
import org.dhis2.mobile.commons.biometrics.biometricTemplateAttributeId
import org.hisp.dhis.android.core.D2
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class UpdateBiometricTemplateAttributeValueTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Test
    fun `Should persist biometric references as json under the template attribute`() {
        givenTemplateAttributeValueType(d2)

        val biometricReferences = listOf(
            BiometricReference(
                type = "FACE_REFERENCE",
                format = "RANK_ONE_3_1",
                templates = listOf(
                    BiometricTemplate("template1"),
                    BiometricTemplate("template2"),
                ),
            ),
        )

        updateBiometricTemplateAttributeValue(d2, "teiUid", biometricReferences)

        val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(biometricTemplateAttributeId, "teiUid")

        val expectedJson = Gson().toJson(BiometricReferencesValue(biometricReferences))
        verify(valueRepository).blockingSet(expectedJson)
    }

    @Test
    fun `Should not write anything when there are no biometric references`() {
        givenTemplateAttributeValueType(d2)

        updateBiometricTemplateAttributeValue(d2, "teiUid", emptyList())

        val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(biometricTemplateAttributeId, "teiUid")

        verify(valueRepository, never()).blockingSet(any())
    }
}
