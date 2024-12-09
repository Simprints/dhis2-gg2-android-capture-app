package org.dhis2.usescases.biometrics.ui.buttons

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TealBorderButton(
    @StringRes textId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
) {
    OutlinedButton(
        modifier = modifier.defaultMinSize(minHeight = 50.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFF0281cb)
        ),
        onClick = onClick
    ) {
        Text(
            text = stringResource(textId),
            color = Color(0xFF0281cb),
            textAlign = TextAlign.Center
        )
    }
}