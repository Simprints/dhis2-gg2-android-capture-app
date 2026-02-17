package org.dhis2.commons.biometrics

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

const val defaultButtonColor = "#007acc"
const val successButtonColor = "#FF35835D"
const val failedButtonColor = "#C57704"
const val declinedButtonColor = "#a6a5a4"

val colorStops = arrayOf(
    0.0f to Color(0xFF009cb6),
    0.4f to Color(0xFF00b4d1),
    0.8f to Color(0xFF009cb6)
)

val gradientButtonColor = Brush.horizontalGradient(colorStops = colorStops)
