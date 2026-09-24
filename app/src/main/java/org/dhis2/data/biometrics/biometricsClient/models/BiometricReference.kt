package org.dhis2.data.biometrics.biometricsClient.models

data class BiometricReference(
    val type: String,
    val format: String,
    val templates: List<BiometricTemplate>,
)

data class BiometricTemplate(
    val template: String,
)
