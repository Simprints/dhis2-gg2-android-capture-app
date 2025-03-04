package org.dhis2.usescases.biometrics

import org.dhis2.data.biometrics.BiometricsClient
import org.hisp.dhis.android.core.D2

fun getOrgUnitAsModuleIdByList(
    selectedOrgUnitUIds: List<String>,
    d2: D2
): String {
    val basicInfoOrgUnits: List<BasicInfoOrgUnit> = selectedOrgUnitUIds.map {
        val orgUnit = d2.organisationUnitModule().organisationUnits().uid(it).blockingGet()
        BasicInfoOrgUnit(orgUnit?.uid() ?: "", orgUnit?.level() ?: 0, orgUnit?.path() ?: it)
    }

    return getOrgUnitAsModuleIdByBasicInfo(basicInfoOrgUnits)
}

data class BasicInfoOrgUnit(
    val uid: String,
    val level: Int,
    val path: String
)

fun getOrgUnitAsModuleIdByBasicInfo(orgUnits: List<BasicInfoOrgUnit>): String {
    val level4Parents = orgUnits.map {
                getLevel4DistrictParent(it.level, it.path)
            }
            .filter { it.isNotBlank() }
            .distinct()

    return if (level4Parents.size == 1) {
        return level4Parents.first()
    } else {
        return BiometricsClient.DefaultModuleId
    }
}

fun getLevel4DistrictParent(
    currentLevel: Int,
    path: String,
): String {
    val pathList = path.split("/").filter { it.isNotBlank() }

    if (currentLevel < 4 || path.isBlank() || pathList.size < 4) {
        return ""
    }

    val level4Distance = currentLevel - 4

    return pathList.getOrNull(pathList.size - level4Distance - 1) ?: ""
}