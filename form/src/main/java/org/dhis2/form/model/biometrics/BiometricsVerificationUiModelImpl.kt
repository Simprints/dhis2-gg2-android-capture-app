package org.dhis2.form.model.biometrics

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.KeyboardActionType
import org.dhis2.form.model.LegendValue
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.event.UiEventFactory
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.intent.FormIntent.OnFocus
import org.dhis2.form.ui.style.FormUiModelStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.option.Option

enum class BiometricsVerificationStatus {
    NOT_DONE,
    SUCCESS,
    FAILURE
}

data class BiometricsVerificationUiModelImpl(
    override val uid: String,
    override val layoutId: Int,
    override val value: String? = null,
    override val programStageSection: String?,
    val status: BiometricsVerificationStatus
) : FieldUiModel {


    private var callback: FieldUiModel.Callback? = null

    private var biometricRetryListener: BiometricsReTryOnClickListener? = null

    private var biometricRegisterListener: BiometricsRegisterClickListener? = null

    fun isSelected(): Boolean = false

    override val formattedLabel: String
        get() = label

    override fun setCallback(callback: FieldUiModel.Callback) {
        this.callback = callback
    }

    override fun equals(item: FieldUiModel): Boolean {
        item as BiometricsVerificationUiModelImpl
        return super.equals(item)
    }

    override val focused = false
    override val error: String? = null
    override val editable = false
    override val warning: String? = null
    override val mandatory = false
    override val label = ""
    override val style: FormUiModelStyle? = null
    override val hint: String? = null
    override val description: String = ""
    override val valueType: ValueType? = null
    override val legend: LegendValue? = null
    override val optionSet: String? = null
    override val allowFutureDates: Boolean? = null
    override val uiEventFactory: UiEventFactory? = null
    override val displayName: String? = null
    override val renderingType: UiRenderType? = null
    override var optionSetConfiguration: OptionSetConfiguration? = null
    override val keyboardActionType: KeyboardActionType? = null
    override val fieldMask: String? = null
    override val isLoadingData = false

    override fun onItemClick() {
        callback?.intent(
            OnFocus(
                uid,
                value
            )
        )
    }

    override fun onDescriptionClick() {
        callback?.recyclerViewUiEvents(
            RecyclerViewUiEvents.ShowDescriptionLabelDialog(
                label,
                description
            )
        )
    }

    override fun invokeUiEvent(uiEventType: UiEventType) {
        onItemClick()
    }

    override fun invokeIntent(intent: FormIntent) {
        callback?.intent(intent)
    }

    override val textColor: Int?
        get() = style?.textColor(error, warning)

    override val backGroundColor: Pair<Array<Int>, Int>?
        get() =
            valueType?.let {
                style?.backgroundColor(it, error, warning)
            }

    override val hasImage: Boolean
        get() = false

    override val isAffirmativeChecked: Boolean
        get() = false

    override val isNegativeChecked: Boolean
        get() = false

    override fun onNext() {}

    override fun onTextChange(value: CharSequence?) {
        val text = when {
            value?.isEmpty() == true -> null
            else -> value?.toString()
        }
        callback?.intent(FormIntent.OnTextChange(uid, text))
    }

    override fun onClear() {}

    override fun onSave(value: String?) {
        onItemClick()
        callback?.intent(FormIntent.OnSave(uid, value, valueType))
    }

    override fun onSaveBoolean(boolean: Boolean) {}

    override fun onSaveOption(option: Option) {}

    override fun setValue(value: String?) = this.copy(value = value)

    fun setStatus(status: BiometricsVerificationStatus) = this.copy(status = status)

    override fun setIsLoadingData(isLoadingData: Boolean) = this.copy()

    override fun setDisplayName(displayName: String?) = this.copy()

    override fun setKeyBoardActionDone() = this.copy()

    override fun setFocus() = this.copy()

    override fun setError(error: String?) = this.copy()

    override fun setEditable(editable: Boolean) = this.copy()

    override fun setLegend(legendValue: LegendValue?) = this.copy()

    override fun setWarning(warning: String) = this.copy()

    override fun setFieldMandatory() = this.copy()

    override fun isSectionWithFields() = false

    fun onRetryVerificationClick() {
        biometricRetryListener?.onRetryClick();
    }

    fun onBiometricClick() {
        biometricRegisterListener?.onClick();
    }

    fun setBiometricsRetryListener(listener: BiometricsReTryOnClickListener) {
        this.biometricRetryListener = listener
    }

    fun setBiometricsRegisterListener(listener: BiometricsRegisterClickListener) {
        this.biometricRegisterListener = listener
    }

    interface BiometricsReTryOnClickListener {
        fun onRetryClick()
    }

    interface BiometricsRegisterClickListener {
        fun onClick()
    }
}
