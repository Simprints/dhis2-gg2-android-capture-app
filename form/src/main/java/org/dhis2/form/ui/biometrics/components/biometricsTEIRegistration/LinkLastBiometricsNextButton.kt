package org.dhis2.form.ui.biometrics.components.biometricsTEIRegistration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dhis2.commons.biometrics.gradientButtonColor
import org.dhis2.form.R
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
internal fun LinkLastBiometricsNextButton(
    enabled: Boolean,
    onClick: (() -> Unit),
) {
    val background = getColor(enabled)
    val backgroundColor =
        if (background is BiometricsBackground.Solid) background.value else Color.Transparent

    val modifier = Modifier.fillMaxWidth().height(50.dp)

    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ), contentPadding = PaddingValues(),
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        enabled = enabled
    ) {
        val boxModifier = when (background) {
            is BiometricsBackground.Gradient -> Modifier
                .background(background.value)
                .then(modifier)

            is BiometricsBackground.Solid -> Modifier
        }

        Box(
            modifier = boxModifier,
            contentAlignment = Alignment.Center,

            ) {
            Text(text = stringResource(R.string.next), color = Color.White)
        }
    }
}

private fun getColor(enabled: Boolean): BiometricsBackground {
    return if (enabled) {
        BiometricsBackground.Gradient(gradientButtonColor)
    } else {
        BiometricsBackground.Solid(SurfaceColor.DisabledSurface)
    }
}
