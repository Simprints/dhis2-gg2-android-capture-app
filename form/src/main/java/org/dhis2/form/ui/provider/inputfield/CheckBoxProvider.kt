package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.orientation
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.InputCheckBox
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesOnlyCheckBox

@Composable
internal fun ProvideCheckBoxInput(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    val dataMap = buildMap {
        fieldUiModel.optionSetConfiguration?.optionFlow?.collectAsLazyPagingItems()?.let { paging ->
            repeat(paging.itemCount) { index ->
                val optionData = paging[index]
                put(
                    optionData?.option?.code() ?: "",
                    CheckBoxData(
                        uid = optionData?.option?.uid() ?: "",
                        checked = fieldUiModel.displayName == optionData?.option?.displayName(),
                        enabled = true,
                        textInput = optionData?.option?.displayName() ?: "",
                    ),
                )
            }
        }
    }

    val (codeList, data) = dataMap.toList().unzip()

    InputCheckBox(
        modifier = modifier,
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        checkBoxData = data,
        orientation = fieldUiModel.orientation(),
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        onItemChange = { item ->
            val selectedIndex = data.indexOf(item)
            intentHandler(
                FormIntent.OnSave(
                    fieldUiModel.uid,
                    codeList[selectedIndex],
                    fieldUiModel.valueType,
                ),
            )
        },
        onClearSelection = {
            intentHandler(
                FormIntent.ClearValue(fieldUiModel.uid),
            )
        },
    )
}

@Composable
internal fun ProvideYesNoCheckBoxInput(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    resources: ResourceManager,
) {
    val data = listOf(
        CheckBoxData(
            uid = "true",
            checked = fieldUiModel.isAffirmativeChecked,
            enabled = true,
            textInput = resources.getString(R.string.yes),
        ),
        CheckBoxData(
            uid = "false",
            checked = fieldUiModel.isNegativeChecked,
            enabled = true,
            textInput = resources.getString(R.string.no),
        ),
    )

    InputCheckBox(
        modifier = modifier,
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        checkBoxData = data,
        orientation = fieldUiModel.orientation(),
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        onItemChange = { item ->
            when (item.uid) {
                "true" -> {
                    intentHandler(
                        FormIntent.OnSave(
                            fieldUiModel.uid,
                            true.toString(),
                            fieldUiModel.valueType,
                        ),
                    )
                }

                "false" -> {
                    intentHandler(
                        FormIntent.OnSave(
                            fieldUiModel.uid,
                            false.toString(),
                            fieldUiModel.valueType,
                        ),
                    )
                }

                else -> fieldUiModel.onClear()
            }
        },
        onClearSelection = {
            intentHandler(
                FormIntent.ClearValue(fieldUiModel.uid),
            )
        },
    )
}

@Composable
internal fun ProvideYesOnlyCheckBoxInput(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    val cbData = CheckBoxData(
        uid = "",
        checked = fieldUiModel.isAffirmativeChecked,
        enabled = true,
        textInput = fieldUiModel.label,
    )

    InputYesOnlyCheckBox(
        modifier = modifier,
        inputStyle = inputStyle,
        checkBoxData = cbData,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        onClick = {
            if (!fieldUiModel.isAffirmativeChecked) {
                intentHandler(
                    FormIntent.OnSave(
                        fieldUiModel.uid,
                        true.toString(),
                        fieldUiModel.valueType,
                    ),
                )
            } else {
                intentHandler(
                    FormIntent.ClearValue(fieldUiModel.uid),
                )
            }
        },
    )
}
