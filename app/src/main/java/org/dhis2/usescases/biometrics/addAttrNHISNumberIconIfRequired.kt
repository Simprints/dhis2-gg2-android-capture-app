package org.dhis2.usescases.biometrics

import org.dhis2.form.extensions.isNhisNumberText
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem

fun addAttrNHISNumberEmojiIfRequired(additionalInfo: List<AdditionalInfoItem>): List<AdditionalInfoItem> {
    return additionalInfo.map {
        val key = it.key ?: ""

        if (key.isNhisNumberText()) {
            if (it.value.isNotBlank() && it.value != "-") {
                it.copy(value = "✔\uFE0F", isConstantItem = true)
            } else {
                it.copy(value = "❌", isConstantItem = true)
            }
        } else {
            it
        }

    }
}