package org.dhis2.usescases.biometrics

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.dhis2.usescases.biometrics.entities.BiometricsConfig
import org.dhis2.usescases.biometrics.entities.BiometricsMode
import org.dhis2.usescases.biometrics.repositories.BiometricsConfigRepository
import org.dhis2.usescases.biometrics.usecases.SelectBiometricsConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class SelectBiometricsConfigTest {
    private val maxDuration = 30

    @Mock
    lateinit var biometricsConfigRepository: BiometricsConfigRepository

    @Test
    fun `Should select default config if there are more than one config by user org unit group`()= runBlocking<Unit> {
        val selectBiometricsConfig = givenABiometricConfigs(listOf("iKcGSF3p97c", "aAcGSF3p97c"))

        selectBiometricsConfig("dummyProgram").collect()

        verify(biometricsConfigRepository).saveSelectedConfig(defaultConfig)
    }

    @Test
    fun `Should select default config if there are not config by user org unit group or program`()= runBlocking<Unit> {
        val selectBiometricsConfig =
            givenABiometricConfigs(listOf("non_in_config_1", "non_in_config_2"))

        selectBiometricsConfig("dummyProgram").collect()

        verify(biometricsConfigRepository).saveSelectedConfig(defaultConfig)
    }

    @Test
    fun `Should select config if there are config by user org unit group and non by program`()= runBlocking<Unit> {
        val selectBiometricsConfig = givenABiometricConfigs(listOf("aAcGSF3p97c"))

        selectBiometricsConfig("dummyProgram").collect()

        val expectedConfig = configs[3]

        verify(biometricsConfigRepository).saveSelectedConfig(expectedConfig)
    }

    @Test
    fun `Should select config if there are config by program`()= runBlocking<Unit> {
        val selectBiometricsConfig = givenABiometricConfigs(listOf("aAcGSF3p97c"))

        selectBiometricsConfig("DM9n1bUw8W8").collect()

        val expectedConfig = configs[2]

        verify(biometricsConfigRepository).saveSelectedConfig(expectedConfig)
    }

    private fun givenABiometricConfigs(userOrgUnitGroups: List<String>): SelectBiometricsConfig {
        whenever(
            biometricsConfigRepository.getBiometricsConfigs()
        ).thenReturn(flowOf(configs))

        whenever(
            biometricsConfigRepository.getUserOrgUnitGroups()
        ).thenReturn(flowOf(userOrgUnitGroups))

        whenever(biometricsConfigRepository.saveSelectedConfig(any())
        ).thenReturn(flowOf(Unit))

        return SelectBiometricsConfig(biometricsConfigRepository)
    }
}

val defaultConfig = BiometricsConfig(
    orgUnitGroup = "default",
    projectId = "jVIWpXqmw6i0QqcY1lPJ",
    confidenceScoreFilter = 55,
    icon = "fingerprint",
    lastVerificationDuration = 5,
    program = null,
    lastDeclinedEnrolDuration = null,
    orgUnitLevelAsModuleId = 0,
    dateOfBirthAttribute = "S4eTdBrXPpj",
    ageThresholdMonths = 6,
    biometricsMode = BiometricsMode.full
)

val configs = listOf(
    defaultConfig,
    BiometricsConfig(
        orgUnitGroup = "iKcGSF3p97c",
        projectId = "Fx4AWbpU0zq7LQt9lPxn",
        confidenceScoreFilter = 55,
        icon = "face",
        lastVerificationDuration = 5,
        program = null,
        lastDeclinedEnrolDuration = null,
        orgUnitLevelAsModuleId = 0,
        dateOfBirthAttribute = "S4eTdBrXPpj",
        ageThresholdMonths = 6,
        biometricsMode = BiometricsMode.full
    ),
    BiometricsConfig(
        orgUnitGroup = null,
        projectId = "Fx4AWbpU0zq7LQt9lPxn",
        confidenceScoreFilter = 55,
        icon = "fingerprint",
        lastVerificationDuration = 5,
        program = "DM9n1bUw8W8",
        lastDeclinedEnrolDuration = null,
        orgUnitLevelAsModuleId = 0,
        dateOfBirthAttribute = "S4eTdBrXPpj",
        ageThresholdMonths = 6,
        biometricsMode = BiometricsMode.full
    ),
    BiometricsConfig(
        orgUnitGroup = "aAcGSF3p97c",
        projectId = "AFx4AWbpU0zq7LQt9lPxn",
        confidenceScoreFilter = 55,
        icon = "face",
        lastVerificationDuration = 5,
        program = null,
        lastDeclinedEnrolDuration = null,
        orgUnitLevelAsModuleId = 0,
        dateOfBirthAttribute = "S4eTdBrXPpj",
        ageThresholdMonths = 6,
        biometricsMode = BiometricsMode.full
    )
)
