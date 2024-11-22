package org.dhis2.usescases.biometrics.ui

import org.dhis2.commons.biometrics.BIOMETRICS_SEARCH_PATTERN
import org.dhis2.usescases.biometrics.biometricAttributeId
import org.dhis2.usescases.teiDashboard.ui.mapper.firstNameAttrUid
import org.dhis2.usescases.teiDashboard.ui.mapper.lastNameAttrUid
import org.junit.Assert
import org.junit.Test

class SequentialSearchTest {

    private val sessionId = "1111-1111"
    private val biometricUid = "1234-5678"
    private val firstName = "John"
    private val lastName = "Doe"

    @Test
    fun `Should return query data with biometrics if it's biometrics search and not exist previous search`() {
        val biometricsSearch = givenABiometricsSearch(null)

        val queryData = biometricsSearch.finalQueryData

        Assert.assertEquals(1, queryData.keys.size)

        verifyBiometricsAttribute(queryData)
        verifyAttributesNotPresent(queryData)
    }

    @Test
    fun `Should return empty query data with biometrics if it's failed biometrics search and not exist previous search`() {
        val biometricsSearch = givenAFailedBiometricsSearch(null)

        val queryData = biometricsSearch.finalQueryData

        Assert.assertEquals(0, queryData.keys.size)
    }

    @Test
    fun `Should return query data with attributes if it's attributes search and not exist previous search`() {
        val attributeSearch = givenAnAttributeSearch(null)

        val queryData = attributeSearch.finalQueryData

        Assert.assertEquals(2, queryData.keys.size)

        verifyNormalAttributes(queryData)
        verifyBiometricsNotPresent(queryData)
    }

    @Test
    fun `Should return query data with biometrics and normal attributes if it's attributes search and exist previous biometrics search`() {
        val attributeSearch = givenAnAttributeSearch(givenABiometricsSearch(null))

        val queryData = attributeSearch.finalQueryData

        Assert.assertEquals(3, queryData.keys.size)

        verifyBiometricsAttribute(queryData)
        verifyNormalAttributes(queryData)
    }

    @Test
    fun `Should return query data with normal attributes if it's attributes search and exist previous failed biometrics search`() {
        val attributeSearch = givenAnAttributeSearch(givenAFailedBiometricsSearch(null))

        val queryData = attributeSearch.finalQueryData

        Assert.assertEquals(2, queryData.keys.size)

        verifyBiometricsNotPresent(queryData)
        verifyNormalAttributes(queryData)
    }

    @Test
    fun `Should return query data with biometrics and normal attributes if it's biometrics search and exist previous attributes search`() {
        val biometricsSearch =givenABiometricsSearch( givenAnAttributeSearch(null))

        val queryData = biometricsSearch.finalQueryData

        Assert.assertEquals(3, queryData.keys.size)

        verifyBiometricsAttribute(queryData)
        verifyNormalAttributes(queryData)
    }

    @Test
    fun `Should return query data with normal attributes if it's failed biometrics search and exist previous attributes search`() {
        val biometricsSearch =givenAFailedBiometricsSearch( givenAnAttributeSearch(null))

        val queryData = biometricsSearch.finalQueryData

        Assert.assertEquals(2, queryData.keys.size)

        verifyBiometricsNotPresent(queryData)
        verifyNormalAttributes(queryData)
    }

    private fun givenABiometricsSearch(previousSearch: SequentialSearch.AttributeSearch?): SequentialSearch.BiometricsSearch {
        val biometricsSearch = SequentialSearch.BiometricsSearch(
            previousSearch = previousSearch,
            nextActions = emptyList(),
            sessionId = sessionId,
            biometricUid = biometricUid,
            isAgeNotSupported = false
        )
        return biometricsSearch
    }

    private fun givenAFailedBiometricsSearch(previousSearch: SequentialSearch.AttributeSearch?): SequentialSearch.BiometricsSearch {
        val biometricsSearch = SequentialSearch.BiometricsSearch(
            previousSearch = previousSearch,
            nextActions = emptyList(),
            sessionId = null,
            biometricUid = biometricUid,
            isAgeNotSupported = false
        )
        return biometricsSearch
    }

    private fun givenAnAttributeSearch(previousSearch: SequentialSearch.BiometricsSearch?): SequentialSearch.AttributeSearch {
        val biometricsSearch = SequentialSearch.AttributeSearch(
            previousSearch = previousSearch,
            nextActions = emptyList(),
            queryData = mutableMapOf(firstNameAttrUid to firstName, lastNameAttrUid to lastName)
        )
        return biometricsSearch
    }

    private fun verifyBiometricsAttribute(queryData: HashMap<String, String>) {
        val biometricValue = queryData[biometricAttributeId]

        Assert.assertEquals(
            BIOMETRICS_SEARCH_PATTERN + sessionId + "_" + biometricUid,
            biometricValue
        )
    }

    private fun verifyNormalAttributes(queryData: HashMap<String, String>) {
        val firstNameValue = queryData[firstNameAttrUid]

        Assert.assertEquals(firstName, firstNameValue)

        val lastNameValue = queryData[lastNameAttrUid]

        Assert.assertEquals(lastName, lastNameValue)
    }

    private fun verifyAttributesNotPresent(queryData: HashMap<String, String>) {
        Assert.assertNull(queryData[firstNameAttrUid])
        Assert.assertNull(queryData[lastNameAttrUid])
    }

    private fun verifyBiometricsNotPresent(queryData: HashMap<String, String>) {
        Assert.assertNull(queryData[biometricAttributeId])
    }
}