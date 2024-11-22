package org.dhis2.usescases.biometrics.ui

import org.dhis2.commons.biometrics.BIOMETRICS_SEARCH_PATTERN
import org.dhis2.usescases.biometrics.biometricAttributeId

const val failedBiometricsSessionId = "NA"

sealed class SequentialSearch(
    open val previousSearch: SequentialSearch?,
    open val nextActions: List<SequentialSearchAction>
) {
    private val sequentialSessionId: String?
        get() = when (this) {
            is BiometricsSearch -> {
                this.sessionId
            }

            is AttributeSearch -> {
                (previousSearch as? BiometricsSearch)?.sessionId
            }
        }

    val finalQueryData: HashMap<String, String>
        get() = when (this) {
            is BiometricsSearch -> {
                val biometricsQueryData = sequentialSessionId?.let {
                    hashMapOf(biometricAttributeId to BIOMETRICS_SEARCH_PATTERN + sequentialSessionId + "_" + biometricUid)
                }
                    ?: emptyMap()

                val attributesQueryData =
                    (previousSearch as? AttributeSearch)?.queryData ?: emptyMap()

                HashMap(biometricsQueryData + attributesQueryData)
            }

            is AttributeSearch -> {
                val attributesQueryData = queryData

                val biometricsQueryData =
                    (previousSearch as? BiometricsSearch)?.let {
                        sequentialSessionId?.let { sessionId ->
                            hashMapOf(biometricAttributeId to BIOMETRICS_SEARCH_PATTERN + sessionId + "_" + it.biometricUid)
                        } ?: emptyMap()
                    } ?: emptyMap()

                HashMap(biometricsQueryData + attributesQueryData)
            }
        }

    data class BiometricsSearch(
        override val previousSearch: SequentialSearch?,
        override val nextActions: List<SequentialSearchAction>,
        val biometricUid: String,
        val sessionId: String?,
        val isAgeNotSupported: Boolean,
    ) : SequentialSearch(previousSearch, nextActions)

    data class AttributeSearch(
        override val previousSearch: SequentialSearch?,
        override val nextActions: List<SequentialSearchAction>,
        val queryData: Map<String, String>
    ) : SequentialSearch(previousSearch, nextActions)
}

sealed class SequentialSearchAction {
    data object SearchWithBiometrics : SequentialSearchAction()
    data object SearchWithAttributes : SequentialSearchAction()
    data object RegisterNew : SequentialSearchAction()
}

