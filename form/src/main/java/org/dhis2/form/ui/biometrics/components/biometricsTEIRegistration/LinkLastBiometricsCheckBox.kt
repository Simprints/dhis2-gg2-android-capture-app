package org.dhis2.form.ui.biometrics.components.biometricsTEIRegistration

import android.graphics.Color.parseColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dhis2.commons.biometrics.defaultButtonColor
import org.dhis2.form.R

@Composable
internal fun LinkLastBiometricsCheckBox(
    value: Boolean,
    enabled: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
    ) {
        Text(
            stringResource(R.string.link_last_biometrics),
            color = if (enabled) Color(parseColor(defaultButtonColor)) else Color.LightGray,
            style = MaterialTheme.typography.body1,
        )
        Checkbox(
            enabled = enabled,
            checked = value,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material.CheckboxDefaults.colors(
                checkedColor = Color(parseColor(defaultButtonColor)),
                uncheckedColor = Color(parseColor(defaultButtonColor)),
            ),
        )
    }
}
