package org.dhis2.usescases.biometrics

import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit

fun isLastVerificationValid(lastVerification: Long?, maxDuration: Int = 0, trace:Boolean): Boolean {

    var lastUpdatedMinutes: Long? = null

    if (lastVerification != null) {
        lastUpdatedMinutes = TimeUnit.MILLISECONDS.toMinutes(
            Date().time - lastVerification
        )
    }

    if (trace){
        Timber.d("lastBiometricsVerificationDuration $maxDuration")
        Timber.d("Biometrics last updated date $lastVerification")
        Timber.d("Biometrics last updated minutes $lastUpdatedMinutes")
    }

    return lastUpdatedMinutes != null && lastUpdatedMinutes <= maxDuration
}