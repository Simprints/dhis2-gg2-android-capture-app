package org.dhis2.usescases.biometrics

import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.data.biometrics.getBiometricsConfig
import org.dhis2.form.model.FieldUiModel
import org.dhis2.tracker.search.model.TrackedEntitySearchItemAttributeDomain
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
    queryData: Map<String, List<String>?>
): Boolean {
    val biometricsConfig = getBiometricsConfig(basicPreferenceProvider)

    val birthdateFieldKey = queryData.keys.find { it == biometricsConfig.dateOfBirthAttribute }

    val value = queryData[birthdateFieldKey]?.firstOrNull()

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

// EyeSeeTea customization - Age Threshold Controls For Biometrics
// Overload for the search flow. Since 3.4.1 the search result carries attributes as
// TrackedEntitySearchItemAttributeDomain, while the TEI dashboard and enrollment flows
// still use the SDK's TrackedEntityAttributeValue, so both types have to be supported.
// @JvmName is required: both overloads erase to List on the JVM.
//
// TODO: merge the duplicated age helpers in this file. getAgeInMonthsByAttributes (SDK type),
// getAgeInMonthsBySearchItemAttributes (domain type), getAgeInMonthsByFieldUiModel (form fields)
// and containsAgeFilterAndIsUnderAgeThreshold (query map) all do the same thing: find the
// configured date-of-birth attribute and compute the age. They differ only in how they read the
// (uid, value) pair. Kept separate during the 3.4.1 upgrade to keep the merge delta minimal —
// see "Open problem" in eyeseetea-docs/upgrade/simprints/upgrade-3.4-notes.md. Solution not
// decided yet.
@JvmName("isUnderAgeThresholdForSearchItems")
fun isUnderAgeThreshold(
    basicPreferenceProvider: BasicPreferenceProvider,
    attributeValues: List<TrackedEntitySearchItemAttributeDomain>
): Boolean {
    val ageInMonths = getAgeInMonthsBySearchItemAttributes(basicPreferenceProvider, attributeValues)
    val biometricsConfig = getBiometricsConfig(basicPreferenceProvider)

    return ageInMonths < (biometricsConfig.ageThresholdMonths)
}

// EyeSeeTea customization - Age Threshold Controls For Biometrics
fun getAgeInMonthsBySearchItemAttributes(
    basicPreferenceProvider: BasicPreferenceProvider,
    attributes: List<TrackedEntitySearchItemAttributeDomain>
): Long {
    val biometricsConfig = getBiometricsConfig(basicPreferenceProvider)

    val birthdateFieldValue =
        attributes.find { it.attribute == biometricsConfig.dateOfBirthAttribute }

    return if (!birthdateFieldValue?.value.isNullOrEmpty()) {
        calculateAgeInMonths(birthdateFieldValue!!.value!!, DateTime.now())
    } else {
        0
    }
}

fun calculateAgeInMonths(value: String, now: DateTime): Long {
    return try {

        val formatter1 = DateTimeFormat.forPattern(DateUtils.SIMPLE_DATE_FORMAT)
        val formatter2 = DateTimeFormat.forPattern(DateUtils.DATE_FORMAT_EXPRESSION)

        val dateValue = try {
            formatter1.parseDateTime(value)
        } catch (e: Exception) {
            formatter2.parseDateTime(value)
        }

        val months = Days.daysBetween(dateValue, now).days.toDouble() / 30

        val ageInMonths = months.toLong()

        Timber.d("Age in months: $ageInMonths")
        ageInMonths
    } catch (e: Exception) {
        0
    }
}