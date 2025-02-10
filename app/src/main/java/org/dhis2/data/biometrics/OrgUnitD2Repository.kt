package org.dhis2.data.biometrics

import org.dhis2.usescases.biometrics.repositories.OrgUnitRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OrgUnitD2Repository(private val d2: D2) : OrgUnitRepository {
    override fun getByUid(uid: String): OrganisationUnit {
        val orgUnit = d2.organisationUnitModule().organisationUnits().byUid().eq(uid).one().blockingGet()
            ?: throw IllegalArgumentException("Organisation unit with uid $uid not found")

        return orgUnit
    }

    override fun getUserOrgUnits(programUid:String): List<OrganisationUnit> {
        val programs = listOf(programUid)

        return d2.organisationUnitModule().organisationUnits()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .byProgramUids(programs).blockingGet()
    }
}