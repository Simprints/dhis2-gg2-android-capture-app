package org.dhis2.usescases.biometrics

import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.data.biometrics.getBiometricsConfig
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import timber.log.Timber

fun isUnderAgeThreshold(
    basicPreferenceProvider: BasicPreferenceProvider,
    attributeValues: List<TrackedEntityAttributeValue>
): Boolean {
    val ageInMonths =
        getAgeInMonthsByAttributes(basicPreferenceProvider, attributeValues)
    val biometricsConfig = getBiometricsConfig(basicPreferenceProvider)

    return ageInMonths < (biometricsConfig.ageThresholdMonths)
}

fun containsAgeFilterAndIsUnderAgeThreshold(
    basicPreferenceProvider: BasicPreferenceProvider,
    queryData: Map<String, String>
): Boolean {
    val biometricsConfig = getBiometricsConfig(basicPreferenceProvider)

    val birthdateFieldKey = queryData.keys.find { it == biometricsConfig.dateOfBirthAttribute }

    val value = queryData[birthdateFieldKey]

    if (value != null) {
        val ageInMonths = calculateAgeInMonths(value, DateTime.now())
        return ageInMonths < (biometricsConfig.ageThresholdMonths)
    } else {
        return false
    }
}

fun getAgeInMonthsByFieldUiModel(
    basicPreferenceProvider: BasicPreferenceProvider,
    fields: List<FieldUiModel>
): Long {
    val biometricsConfig = getBiometricsConfig(basicPreferenceProvider)

    val birthdateFieldValue = fields.find { it.uid == biometricsConfig.dateOfBirthAttribute }

    return if (birthdateFieldValue?.value != null && birthdateFieldValue.value != "") {
        calculateAgeInMonths(birthdateFieldValue.value!!, DateTime.now())
    } else {
        0
    }
}

fun getAgeInMonthsByAttributes(
    basicPreferenceProvider: BasicPreferenceProvider,
    attributes: List<TrackedEntityAttributeValue>
): Long {
    val biometricsConfig = getBiometricsConfig(basicPreferenceProvider)

    val birthdateFieldValue =
        attributes.find { it.trackedEntityAttribute() == biometricsConfig.dateOfBirthAttribute }

    return if (birthdateFieldValue?.value() != null && birthdateFieldValue.value() != "") {
        calculateAgeInMonths(birthdateFieldValue.value()!!, DateTime.now())
    } else {
        0
    }
}

fun calculateAgeInMonths(value: String, now: DateTime): Long {
    return try {
        val formatter = DateTimeFormat.forPattern(DateUtils.DATE_FORMAT_EXPRESSION)
        val dateValue = formatter.parseDateTime(value)
        val months = Days.daysBetween(dateValue, now).days.toDouble() / 30

        val ageInMonths = months.toLong()

        Timber.d("Age in months: $ageInMonths")
        ageInMonths
    } catch (e: Exception) {
        0
    }
}