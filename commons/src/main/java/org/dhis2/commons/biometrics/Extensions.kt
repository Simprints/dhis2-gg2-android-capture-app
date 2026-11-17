package org.dhis2.commons.biometrics

import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute

fun TrackedEntityAttribute.isBiometricAttribute(): Boolean = name()?.contains(BIOMETRIC_VALUE, true) ?: false

fun ProgramTrackedEntityAttribute.isBiometricAttribute(): Boolean = name()?.contains(BIOMETRIC_VALUE, true) ?: false

fun String.isBiometricText(): Boolean = this.equals(BIOMETRIC_VALUE, true)

fun String.isNotBiometricText(): Boolean = !this.equals(BIOMETRIC_VALUE, true)
