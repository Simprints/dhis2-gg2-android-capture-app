package org.dhis2.usescases.biometrics.ui.buttons

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.dhis2.commons.biometrics.gradientButtonColor

@Composable
fun TealGradientButton(
    @StringRes textId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { }
) {
    val modifier = modifier
        .wrapContentWidth()
        .defaultMinSize(minHeight = 50.dp)

    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(contentColor = Color.Transparent),
        contentPadding = PaddingValues(),
        onClick = { onClick() },
        shape = RoundedCornerShape(4.dp),
    ) {
        Box(
            modifier = Modifier
                .background(gradientButtonColor)
                .then(modifier),
            contentAlignment = Alignment.Center,

            ) {
            Row() {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(textId),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}