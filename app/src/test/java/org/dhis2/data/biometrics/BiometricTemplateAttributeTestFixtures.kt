package org.dhis2.data.biometrics

import org.dhis2.mobile.commons.biometrics.biometricTemplateAttributeId
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

fun givenTemplateAttributeValueType(d2: D2) {
    val attribute: TrackedEntityAttribute = mock()
    whenever(attribute.valueType()) doReturn ValueType.LONG_TEXT
    whenever(attribute.optionSet()) doReturn null

    whenever(
        d2.trackedEntityModule().trackedEntityAttributes().uid(biometricTemplateAttributeId).blockingGet(),
    ) doReturn attribute
}
