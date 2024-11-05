package org.dhis2.usescases.biometrics.ui

import org.dhis2.data.biometrics.SimprintsItem

sealed class SequentialSearch(
    open val previousSearch: SequentialSearch?,
    open val nextActions: List<SequentialSearchAction>
) {
    val sequentialSessionId: String?
        get() = when (this) {
            is BiometricsSearch -> {
                this.sessionId
            }

            is AttributeSearch -> {
                (previousSearch as? BiometricsSearch)?.sessionId
            }
        }

    data class BiometricsSearch(
        override val previousSearch: SequentialSearch?,
        override val nextActions: List<SequentialSearchAction>,
        val simprintsItems: List<SimprintsItem>,
        val sessionId: String?,
        val isAgeNotSupported: Boolean,
    ) : SequentialSearch(previousSearch, nextActions)

    data class AttributeSearch(
        override val previousSearch: SequentialSearch?,
        override val nextActions: List<SequentialSearchAction>,
    ) : SequentialSearch(previousSearch, nextActions)
}

sealed class SequentialSearchAction {
    data object SearchWithBiometrics : SequentialSearchAction()
    data object SearchWithAttributes : SequentialSearchAction()
    data object RegisterNew : SequentialSearchAction()
}

