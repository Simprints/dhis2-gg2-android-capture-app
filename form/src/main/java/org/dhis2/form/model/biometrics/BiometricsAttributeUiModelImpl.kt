package org.dhis2.form.model.biometrics

import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.form.model.EventCategory
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.KeyboardActionType
import org.dhis2.form.model.LegendValue
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.PeriodSelector
import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.event.UiEventFactory
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.intent.FormIntent.OnFocus
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates

data class BiometricsAttributeUiModelImpl(
    override val uid: String,
    override val value: String? = null,
    override val programStageSection: String?,
    override val autocompleteList: List<String>?,
    override val orgUnitSelectorScope: OrgUnitSelectorScope?,
    override val selectableDates: SelectableDates?,
    override val eventCategories: List<EventCategory>?,
    override val periodSelector: PeriodSelector?,
    override val url: String?,
    override val editable: Boolean = true,
    val ageUnderThreshold: Boolean = false
) : FieldUiModel, BiometricsTEIRegistrationUIModel {

    private var callback: FieldUiModel.Callback? = null

    fun isSelected(): Boolean = false

    override val formattedLabel: String
        get() = label

    override fun setCallback(callback: FieldUiModel.Callback) {
        this.callback = callback
    }

    override fun equals(item: FieldUiModel): Boolean {
        item as BiometricsAttributeUiModelImpl
        return super.equals(item)
    }

    override val focused = false
    override val error: String? = null
    override val warning: String? = null
    override val mandatory = false
    override val label = ""
    override val hint: String? = null
    override val description: String = ""
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

    override fun invokeUiEvent(uiEventType: UiEventType) {
        onItemClick()
    }

    override fun invokeIntent(intent: FormIntent) {
        callback?.intent(intent)
    }

    override val isAffirmativeChecked: Boolean
        get() = false

    override val isNegativeChecked: Boolean
        get() = false

    override val valueType: ValueType
        get() = ValueType.TEXT

    override fun onClear() {}

    override fun onSave(value: String?) {
        onItemClick()
        callback?.intent(FormIntent.OnSave(uid, value, valueType))
    }

    override fun setValue(value: String?) = this.copy(value = value)
    override fun setSelectableDates(selectableDates: SelectableDates?): FieldUiModel {
        return this.copy(selectableDates = selectableDates)
    }

    override fun setIsLoadingData(isLoadingData: Boolean) = this.copy()

    override fun setDisplayName(displayName: String?) = this.copy()

    override fun setKeyBoardActionDone() = this.copy()

    override fun setFocus() = this.copy()

    override fun setError(error: String?) = this.copy()

    override fun setEditable(editable: Boolean) = this.copy(editable = editable)

    override fun setLegend(legendValue: LegendValue?) = this.copy()

    override fun setWarning(warning: String) = this.copy()

    override fun setFieldMandatory() = this.copy()

    override fun isSectionWithFields() = false

    fun setAgeUnderThreshold(value: Boolean) = this.copy(ageUnderThreshold = value)

    // We don't use the FieldUiModel onItemClick() to avoid infrastructure to listen in FormViewModel
    // because we need listen in enrollmentPresenterImpl to register biometrics

    private var biometricListener: (() -> Unit)? = null
    private var saveTEIListener: ((removeBiometrics:Boolean) -> Unit)? = null
    private var registerLastAndSave: ((String) -> Unit)? = null

    override fun onBiometricsClick() {
        biometricListener?.invoke()
    }

    override fun onSaveTEI(removeBiometrics:Boolean) {
        saveTEIListener?.invoke(removeBiometrics)
    }

    override fun registerLastAndSave(sessionId: String) {
        registerLastAndSave?.invoke(sessionId)
    }

    fun setBiometricsRegisterListener(listener: () -> Unit) {
        this.biometricListener = listener
    }

    fun setSaveTEI(listener: (removeBiometrics:Boolean) -> Unit) {
        this.saveTEIListener = listener
    }

    fun setRegisterLastAndSave(listener: (String) -> Unit) {
        this.registerLastAndSave = listener
    }
}
