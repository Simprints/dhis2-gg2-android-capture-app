package org.dhis2.usescases.biometrics.duplicates

import org.dhis2.data.biometrics.SimprintsIdentifiedItem

data class LastPossibleDuplicates(
    val guids: List<SimprintsIdentifiedItem>,
    val sessionId: String,
)