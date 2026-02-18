package org.dhis2.usescases.biometrics.ui.buttons

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = stringResource(textId),
            color = Color(0xFF0281cb),
            textAlign = TextAlign.Center
        )
    }
}