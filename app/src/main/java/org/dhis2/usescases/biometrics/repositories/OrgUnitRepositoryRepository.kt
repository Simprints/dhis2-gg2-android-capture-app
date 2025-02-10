package org.dhis2.usescases.biometrics.repositories

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

interface OrgUnitRepository {
    fun getByUid(uid: String): OrganisationUnit
    fun getUserOrgUnits(programUid:String): List<OrganisationUnit>
}