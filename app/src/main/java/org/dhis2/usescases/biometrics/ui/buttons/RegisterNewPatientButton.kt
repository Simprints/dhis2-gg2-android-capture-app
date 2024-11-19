package org.dhis2.usescases.biometrics.ui.buttons

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun RegisterNewPatientButton(
    onClick: () -> Unit = { },
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("New patient?")
        TextButton(onClick =onClick) {
            Text(
                "Register",
                color = Color(0xFF0281cb),
                style = TextStyle(
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}