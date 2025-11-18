package org.dhis2.data.biometrics.utils

import org.dhis2.commons.bindings.blockingSetCheck
import org.dhis2.usescases.biometrics.nhisNumberAttributeId
import org.hisp.dhis.android.core.D2

fun updateNHISNumberAttributeValue(
    d2: D2,
    teiUid: String,
    value: String
) {
    val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
        .value(nhisNumberAttributeId, teiUid)

    valueRepository.blockingSetCheck(d2, nhisNumberAttributeId, value)
}