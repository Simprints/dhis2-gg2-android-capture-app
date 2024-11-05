package org.dhis2.usescases.biometrics.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.commons.biometrics.gradientButtonColor

@Composable
internal fun SequentialNextSearchActions(
    sequentialSearchActions: List<SequentialSearchAction>,
    onClick: ((action:SequentialSearchAction) -> Unit),
) {
   Column {
        sequentialSearchActions.forEach { action ->
            SequentialNextSearchAction(
                sequentialSearchAction = action,
                onClick = onClick,
            )
            Spacer(modifier = Modifier.size(8.dp))
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
            OutlinedButton(
                modifier = Modifier.defaultMinSize(minHeight = 50.dp, minWidth = 270.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF0281cb)
                ),
                onClick = { onClick(sequentialSearchAction) }
            ) {
                Text(stringResource(R.string.biometrics_search), color = Color(0xFF0281cb))
            }
        }

        is SequentialSearchAction.SearchWithAttributes -> {
            OutlinedButton(
                modifier = Modifier.defaultMinSize(minHeight = 50.dp, minWidth = 270.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF0281cb)
                ),
                onClick = { onClick(sequentialSearchAction) }
            ) {
                Text(stringResource(R.string.search_with_attributes), color = Color(0xFF0281cb))
            }
        }

        is SequentialSearchAction.RegisterNew -> {
            val modifier = Modifier
                .defaultMinSize(minHeight = 50.dp, minWidth = 270.dp)

            Button(
                modifier = modifier,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                contentPadding = PaddingValues(),
                onClick = { onClick(sequentialSearchAction) },
            ) {
                Box(
                    modifier = Modifier
                        .background(gradientButtonColor)
                        .then(modifier),
                    contentAlignment = Alignment.Center,

                    ) {
                    Row (verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.register_new_patient),
                            tint = Color.White,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.register_new_patient),
                            color = Color.White,
                        )
                    }
                }
            }
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
