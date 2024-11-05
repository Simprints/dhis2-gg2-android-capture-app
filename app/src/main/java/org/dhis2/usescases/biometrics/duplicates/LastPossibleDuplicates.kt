package org.dhis2.usescases.biometrics.duplicates

import org.dhis2.data.biometrics.SimprintsItem

data class LastPossibleDuplicates(
    val guids: List<SimprintsItem>,
    val sessionId: String,
)