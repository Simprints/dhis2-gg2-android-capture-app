package org.dhis2.usescases.biometrics.ui.teiDashboardBiometrics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

//TODO: Evaluate move this to a common module
// is a copy of BiometricsStatusFlag
@Composable
fun TeiDashboardBioStatus(
    model: BioStatus,
    modifier: Modifier = Modifier
) {
    Layout(
        modifier = modifier,
        content = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(Color(android.graphics.Color.parseColor(model.backgroundColor)))
                    .height(40.dp)
                    .padding(start = 16.dp)
            ) {
                Text(
                    model.text.uppercase(),
                    color = Color.White,
                    modifier = Modifier.padding(end = 42.dp)
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowLeft,
                contentDescription = "Arrow",
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
        }
    ) { measurables, constraints ->
        val textRowPlaceable = measurables[0].measure(constraints)
        val iconPlaceable = measurables[1].measure(constraints)

        layout(textRowPlaceable.width, textRowPlaceable.height) {
            textRowPlaceable.placeRelative(0, 0)
            val offsetPx = 46.5.dp.roundToPx()
            val iconX = textRowPlaceable.width - offsetPx
            val iconY = (textRowPlaceable.height - iconPlaceable.height) / 2
            iconPlaceable.placeRelative(iconX, iconY)
        }
    }
}

@Preview
@Composable
fun TeiDashboardBioStatusPreview() {
    TeiDashboardBioStatus(
        model = BioStatus(
            text = "Biometrics",
            backgroundColor = "#4d4d4d"
        )
    )
}

