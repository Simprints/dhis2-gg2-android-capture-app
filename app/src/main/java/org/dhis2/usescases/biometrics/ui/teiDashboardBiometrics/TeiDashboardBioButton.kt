package org.dhis2.usescases.biometrics.ui.teiDashboardBiometrics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.commons.biometrics.gradientButtonColor

@Composable
fun TeiDashboardBioButton(
    model: BioButtonModel
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val modifier = Modifier.fillMaxWidth().height(40.dp)

        Button(
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(backgroundColor = if (model.backgroundColor != null) Color(android.graphics.Color.parseColor(model.backgroundColor)) else Color.Transparent),
            contentPadding = PaddingValues(),
            onClick = model.onActionClick,
        ) {
            val boxModifier = if (model.backgroundColor == null) Modifier
                .background(gradientButtonColor)
                .then(modifier) else Modifier

            Box(
                modifier = boxModifier,
                contentAlignment = Alignment.Center,

                ){
                Text(text = model.text, color = Color.White)
            }
        }
    }
}

@Preview
@Composable
fun TeiDashboardBioButtonPreview() {
    TeiDashboardBioButton(model = BioButtonModel(
        text = "Biometrics",
        backgroundColor = "#4d4d4d",
        onActionClick = {})
    )
}
