package org.dhis2.data.biometrics.utils

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue

fun getTrackedEntityAttributeValueByAttribute(
    attribute: String,
    attributeValues: List<TrackedEntityAttributeValue>,
): TrackedEntityAttributeValue? =
    attributeValues
        .find { attributeValue -> attributeValue.trackedEntityAttribute() == attribute }
