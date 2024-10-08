package org.dhis2.data.biometrics.utils

import org.dhis2.commons.bindings.blockingSetCheck
import org.hisp.dhis.android.core.D2

fun updateBiometricsAttributeValue(d2: D2, teiUid:String, value: String, parentTeiUid:String?) {
    val teiUidToUpdate = parentTeiUid?:teiUid
    val tei = d2.trackedEntityModule().trackedEntityInstances().uid(teiUidToUpdate).blockingGet()
        ?: return

    val attributeUid = getBiometricsTrackedEntityAttribute(d2)

    if (attributeUid != null) {
        val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(attributeUid, tei.uid())
        valueRepository.blockingSetCheck(d2, attributeUid, value)
    }
}