package org.dhis2.usescases.biometrics

import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test

class AgeInMonthsTest {
    @Test
    fun `Should return age in months to 6`() {
        val ageInMonths = calculateAgeInMonths("2021-01-01", DateTime.parse("2021-06-30"))

        Assert.assertTrue("Age in months should be 6 but is $ageInMonths", ageInMonths == 6.toLong())
    }
}
