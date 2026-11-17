package org.dhis2.data.biometrics.utils

import org.hisp.dhis.android.core.D2

fun getBiometricsTrackedEntityAttribute(d2: D2): String? =
    d2
        .trackedEntityModule()
        .trackedEntityAttributes()
        .byDisplayFormName()
        .eq("Biometrics")
        .blockingGet()
        .firstOrNull()
        ?.uid()
