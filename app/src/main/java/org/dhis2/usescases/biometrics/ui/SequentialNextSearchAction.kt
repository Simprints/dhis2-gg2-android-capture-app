package org.dhis2.usescases.biometrics.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.usescases.biometrics.ui.buttons.RegisterNewPatientButton
import org.dhis2.usescases.biometrics.ui.buttons.SearchWithAttributesButton
import org.dhis2.usescases.biometrics.ui.buttons.SearchWithBiometricsButton

@Composable
internal fun SequentialNextSearchActions(
    sequentialSearchActions: List<SequentialSearchAction>,
    onClick: ((action:SequentialSearchAction) -> Unit),
) {
   Column (horizontalAlignment = Alignment.CenterHorizontally) {
        sequentialSearchActions.forEach { action ->
            SequentialNextSearchAction(
                sequentialSearchAction = action,
                onClick = onClick,
            )
            Spacer(modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
internal fun SequentialNextSearchAction(
    sequentialSearchAction: SequentialSearchAction,
    onClick: ((action:SequentialSearchAction) -> Unit),
) {
    when (sequentialSearchAction) {
        is SequentialSearchAction.SearchWithBiometrics -> {
            SearchWithBiometricsButton(
                modifier = Modifier.defaultMinSize(minHeight = 50.dp, minWidth = 270.dp),
                onClick = { onClick(sequentialSearchAction) },
            )
        }

        is SequentialSearchAction.SearchWithAttributes -> {
            SearchWithAttributesButton(
                modifier = Modifier.defaultMinSize(minHeight = 50.dp, minWidth = 270.dp),
                onClick = { onClick(sequentialSearchAction) },
            )
        }

        is SequentialSearchAction.RegisterNew -> {
            RegisterNewPatientButton(
                onClick = { onClick(sequentialSearchAction) },
            )
        }
    }
}

@Preview
@Composable
fun PreviewSequentialNextSearchWithBiometricsAction() {
    SequentialNextSearchAction(
        sequentialSearchAction = SequentialSearchAction.SearchWithBiometrics,
        onClick = { },
    )
}

@Preview
@Composable
fun PreviewSequentialNextSearchWithAttributesAction() {
    SequentialNextSearchAction(
        sequentialSearchAction = SequentialSearchAction.SearchWithAttributes,
        onClick = { },
    )
}

@Preview
@Composable
fun PreviewSequentialNextRegisterNewAction() {
    SequentialNextSearchAction(
        sequentialSearchAction = SequentialSearchAction.RegisterNew,
        onClick = { },
    )
}
