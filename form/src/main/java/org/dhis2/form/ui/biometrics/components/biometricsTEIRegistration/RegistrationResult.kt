package org.dhis2.form.ui.biometrics.components.biometricsTEIRegistration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dhis2.commons.biometrics.gradientButtonColor
import org.dhis2.form.R
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
internal fun RegistrationResult(
    onBiometricsClick: (() -> Unit),
    enabled: Boolean,
    resultText: Int,
    resultColor: String?,
    showRetake: Boolean,
) {

    val background = getColor(enabled, resultColor)

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
    ) {
        val modifier = Modifier.weight(1f).height(50.dp)

        Button(
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (background is BiometricsBackground.Solid) background.value else Color.Transparent
            ),
            contentPadding = PaddingValues(),
            onClick = {
                if (!showRetake) {
                    onBiometricsClick()
                }
            },
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(resultText), color = Color.White)
                }
            }
        }

        if (showRetake) {
            Spacer(modifier = Modifier.width(16.dp))

            Button(
                modifier = Modifier.width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(
                        android.graphics.Color.parseColor(
                            "#4d4d4d"
                        )
                    )
                ),
                contentPadding = PaddingValues(),
                onClick = onBiometricsClick,
                enabled = enabled,

                ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bio_retry),
                        contentDescription = stringResource(R.string.biometrics_retake),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.biometrics_retake), color = Color.White)
                }

            }
        }
    }
}

private fun getColor(enabled: Boolean, resultColor: String?): BiometricsBackground {
    return if (enabled) {
        if (resultColor.isNullOrEmpty()) {
            BiometricsBackground.Gradient(gradientButtonColor)
        } else {
            BiometricsBackground.Solid(Color(
                android.graphics.Color.parseColor(
                    resultColor
                )
            ))
        }
    } else {
        BiometricsBackground.Solid(SurfaceColor.DisabledSurface)
    }
}

sealed class BiometricsBackground{
    data class Gradient (val value: Brush): BiometricsBackground()
    data class Solid(val value: Color): BiometricsBackground()
}