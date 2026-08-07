package org.dhis2.utils

// EyeSeeTea customization - Biometric Duplicate Review And Confirm Identity
// enrollmentUid is nullable, matching how the search flow treats it
// (SearchTEPresenter.onSearchTEIModelClick passes null when there is no selected enrollment).
data class LastSelection(
    val teiUid: String,
    val enrollmentUid: String?,
    val isOnline: Boolean
)