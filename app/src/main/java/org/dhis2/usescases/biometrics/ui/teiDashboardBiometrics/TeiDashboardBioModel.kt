package org.dhis2.usescases.biometrics.ui.teiDashboardBiometrics

data class TeiDashboardBioModel(
    val statusModel: BioStatus?,
    val buttonModel: BioButtonModel?,
)

data class BioButtonModel(
    val text: String,
    val backgroundColor: String?,
    val onActionClick: () -> Unit,
)

data class BioStatus(
    val text: String,
    val backgroundColor: String
)



