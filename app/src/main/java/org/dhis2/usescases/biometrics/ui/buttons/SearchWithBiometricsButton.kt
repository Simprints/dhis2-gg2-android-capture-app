package org.dhis2.usescases.biometrics.ui.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.commons.biometrics.gradientButtonColor

@Composable
fun SearchWithBiometricsButton(
    modifier: Modifier,
    onClick: () -> Unit = { }
) {
    val modifier = modifier
        .wrapContentWidth()
        .height(50.dp)

    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        contentPadding = PaddingValues(),
        onClick = { onClick() },
    ) {
        Box(
            modifier = Modifier
                .background(gradientButtonColor)
                .then(modifier),
            contentAlignment = Alignment.Center,

            ) {
            Row() {
                Text(text = stringResource(R.string.biometrics_search), color = Color.White)
            }
        }
    }
}