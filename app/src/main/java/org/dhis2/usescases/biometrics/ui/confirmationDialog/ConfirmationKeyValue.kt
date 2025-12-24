package org.dhis2.usescases.biometrics.ui.confirmationDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
private fun ConfirmationKeyValue(
    additionalInfoItem: AdditionalInfoItem,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val maxKeyWidth = maxWidth / 2 - Spacing.Spacing16

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            var valueColor: Color

            val keyColor: Color = AdditionalInfoItemColor.DEFAULT_KEY.color

            valueColor = additionalInfoItem.color ?: AdditionalInfoItemColor.DEFAULT_VALUE.color
            additionalInfoItem.key?.let {
                ConfirmationListCardKey(
                    text = additionalInfoItem.key!!,
                    color = keyColor,
                    Modifier.padding(end = Spacing.Spacing4).widthIn(Spacing.Spacing0, maxKeyWidth),
                )
            }

            Row(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(Radius.XS)),
            ) {
                if (additionalInfoItem.icon != null) {
                    Box(
                        Modifier.background(color = Color.Transparent).size(20.dp),
                    ) {
                        additionalInfoItem.icon!!.invoke()
                    }
                    Spacer(Modifier.size(Spacing.Spacing4))
                }

                valueColor =
                    if (additionalInfoItem.action != null) SurfaceColor.Primary else valueColor
                ConfirmationListCardValue(text = additionalInfoItem.value, color = valueColor)
            }

        }
    }
}

/**
 * DHIS2 KeyValueList,
 *  used to paint a list of AdditionalInfoItems
 */
@Composable
fun ConfirmationKeyValueList(
    itemList: List<AdditionalInfoItem>,
) {
    Column {
        itemList.forEach { item ->
            ConfirmationKeyValue(item)
            Spacer(Modifier.size(Spacing.Spacing8))
        }
    }
}

@Composable
fun ConfirmationListCardKey(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    var modifiedText by remember(text) { mutableStateOf(text) }
    Text(
        text = modifiedText,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                val lineIndex = textLayoutResult.getLineEnd(
                    lineIndex = 0,
                    visibleEnd = true,
                )
                modifiedText = modifiedText.substring(0, lineIndex).trimEnd() + "...:"
            }
        },
    )
}

@Composable
internal fun ConfirmationListCardValue(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}
