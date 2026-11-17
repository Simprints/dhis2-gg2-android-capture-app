package org.dhis2.form.extensions

const val BIOMETRIC_VALUE = "biometrics"
const val NHIS_NUMBER_VALUE = "NHIS Number"
const val IS_SEARCH_OUTSIDE_PROGRAM_AVAILABLE = false // in all programs on instances with biometrics

fun String.isBiometricText(): Boolean = this.replace(":", "").equals(BIOMETRIC_VALUE, true)

fun String.isNotBiometricText(): Boolean = this.isBiometricText().not()

fun String.isNhisNumberText(): Boolean = this.replace(":", "").equals(NHIS_NUMBER_VALUE, true)

fun String.isNotNhisNumberText(): Boolean = this.isNhisNumberText().not()
