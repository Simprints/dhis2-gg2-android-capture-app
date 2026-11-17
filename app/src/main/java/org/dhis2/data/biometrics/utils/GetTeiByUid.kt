package org.dhis2.data.biometrics.utils

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

fun getTeiByUid(
    d2: D2,
    teiUid: String,
): TrackedEntityInstance? =
    d2
        .trackedEntityModule()
        .trackedEntityInstances()
        .withTrackedEntityAttributeValues()
        .uid(
            teiUid,
        ).blockingGet()
