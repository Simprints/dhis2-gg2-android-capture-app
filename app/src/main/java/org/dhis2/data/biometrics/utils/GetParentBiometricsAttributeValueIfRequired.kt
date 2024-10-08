package org.dhis2.data.biometrics.utils

import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.usescases.biometrics.BIOMETRICS_ENABLED
import org.dhis2.usescases.teiDashboard.TeiAttributesProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue

fun getParentBiometricsAttributeValueIfRequired(
    d2: D2,
    teiAttributesProvider: TeiAttributesProvider,
    basicPreferenceProvider: BasicPreferenceProvider,
    attributeValues: List<TrackedEntityAttributeValue>,
    programUid: String,
    teiUid: String
):TrackedEntityAttributeValue?{
    if (BIOMETRICS_ENABLED) {
        val biometricsAttribute = getBiometricsTrackedEntityAttribute(d2) ?: return null

        val parentTeiUid = getParentTeiUid(d2,basicPreferenceProvider,attributeValues,programUid,teiUid)

        if (parentTeiUid != null) {

            val relatedAttributeValues: List<TrackedEntityAttributeValue> =
                teiAttributesProvider.getValuesFromProgramTrackedEntityAttributesByProgram(
                    programUid,
                    parentTeiUid
                ).blockingGet()

            val attValue = getTrackedEntityAttributeValueByAttribute(
                biometricsAttribute,
                relatedAttributeValues
            )
            if (attValue != null) {
                return attValue
            }
        }
    }

    return null
}

