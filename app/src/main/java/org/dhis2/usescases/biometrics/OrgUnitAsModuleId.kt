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
    val level = orgUnit?.level() ?: 0

    return getOrgUnitAsModuleIdByPath(selectedOrgUnitUid, level, path, orgUnitLevelAsModuleId)
}

fun getOrgUnitAsModuleIdByPath(
    selectedOrgUnitUid: String,
    selectedOrgUnitLevel: Int,
    path: String,
    orgUnitLevelAsModuleId: Int
): String {
    val maxOrgUnitLevelAsModuleId = 4

    val pathList = path.split("/").filter { it.isNotBlank() }

    return if (pathList.contains(selectedOrgUnitUid)) {

        val orgUnitLevelsPath = (1..selectedOrgUnitLevel).toList()

        val pathIndexToSelect = pathList.indexOf(selectedOrgUnitUid) + orgUnitLevelAsModuleId

        if (pathIndexToSelect < 0){
            pathList[0]
        } else {
            val pathLevel = orgUnitLevelsPath[pathIndexToSelect]

            if (pathLevel > maxOrgUnitLevelAsModuleId) {
                val maxIndex = orgUnitLevelsPath.indexOf(maxOrgUnitLevelAsModuleId)
                pathList[maxIndex]
            } else{
                pathList[pathIndexToSelect]
            }
        }
    } else {
        selectedOrgUnitUid
    }
}