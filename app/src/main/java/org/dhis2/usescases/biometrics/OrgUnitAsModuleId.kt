package org.dhis2.usescases.biometrics

import org.dhis2.commons.biometrics.BiometricsPreference
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.hisp.dhis.android.core.D2

fun getOrgUnitAsModuleId(
    selectedOrgUnitUid: String,
    d2: D2,
    basicPreferenceProvider: BasicPreferenceProvider
): String {
    val orgUnit = d2.organisationUnitModule().organisationUnits().uid(selectedOrgUnitUid).blockingGet()

    val orgUnitLevelAsModuleId =
        basicPreferenceProvider.getInt(BiometricsPreference.ORG_UNIT_LEVEL_AS_MODULE_ID, 0)

    val path = orgUnit?.path() ?: selectedOrgUnitUid

    return getOrgUnitAsModuleIdByPath(selectedOrgUnitUid, path, orgUnitLevelAsModuleId)
}

fun getOrgUnitAsModuleIdByPath(
    selectedOrgUnitUid: String,
    path: String,
    orgUnitLevelAsModuleId: Int
): String {
    val pathList = path.split("/")

    return if (pathList.contains(selectedOrgUnitUid)) {
        val pathIndexToSelect = pathList.indexOf(selectedOrgUnitUid) + orgUnitLevelAsModuleId

        if (pathIndexToSelect < 0) {
            pathList[0]
        } else{
            pathList[pathIndexToSelect]
        }
    } else {
        selectedOrgUnitUid
    }
}