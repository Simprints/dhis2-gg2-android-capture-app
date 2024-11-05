package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment;

import static org.dhis2.commons.biometrics.BiometricConstantsKt.BIOMETRICS_ENROLL_REQUEST;
import static org.dhis2.commons.biometrics.BiometricConstantsKt.BIOMETRICS_TRACKED_ENTITY_INSTANCE_ID;
import static org.dhis2.commons.Constants.EVENT_MODE;
import static org.dhis2.commons.extensions.ViewExtensionsKt.closeKeyboard;
import static org.dhis2.usescases.eventsWithoutRegistration.eventCapture.ui.NonEditableReasonBlockKt.showNonEditableReasonMessage;
import static org.dhis2.utils.granularsync.SyncStatusDialogNavigatorKt.OPEN_ERROR_LOCATION;

import static android.app.Activity.RESULT_OK;

import static org.dhis2.commons.biometrics.BiometricConstantsKt.BIOMETRICS_GUID;
import static org.dhis2.commons.biometrics.BiometricConstantsKt.BIOMETRICS_TEI_ORGANISATION_UNIT;
import static org.dhis2.commons.biometrics.BiometricConstantsKt.BIOMETRICS_VERIFICATION_STATUS;
import static org.dhis2.commons.biometrics.BiometricConstantsKt.BIOMETRICS_VERIFY_REQUEST;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;

import org.dhis2.R;
import org.dhis2.commons.Constants;
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository;
import org.dhis2.commons.featureconfig.model.Feature;
import org.dhis2.data.biometrics.BiometricsClient;
import org.dhis2.data.biometrics.BiometricsClientFactory;
import org.dhis2.data.biometrics.RegisterResult;
import org.dhis2.data.biometrics.VerifyResult;
import org.dhis2.databinding.SectionSelectorFragmentBinding;
import org.dhis2.form.model.EventMode;
import org.dhis2.form.model.EventRecords;
import org.dhis2.form.ui.FormView;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureAction;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import javax.inject.Inject;

import kotlin.Unit;

public class EventCaptureFormFragment extends FragmentGlobalAbstract implements EventCaptureFormView,
        OnEditionListener {

    @Inject
    EventCaptureFormPresenter presenter;

    @Inject
    FeatureConfigRepository featureConfig;

    private EventCaptureActivity activity;
    private SectionSelectorFragmentBinding binding;
    private FormView formView;


    private String biometricsGuid;
    private int biometricsVerificationStatus;
    private String teiOrgUnit;

    private String trackedEntityInstanceId;

    public static EventCaptureFormFragment newInstance(
            String eventUid,
            Boolean openErrorSection,
            EventMode eventMode) {
        EventCaptureFormFragment fragment = new EventCaptureFormFragment();
        Bundle args = new Bundle();
        args.putString(Constants.EVENT_UID, eventUid);
        args.putBoolean(OPEN_ERROR_LOCATION, openErrorSection);
        args.putString(EVENT_MODE, eventMode.name());
        fragment.setArguments(args);
        return fragment;
    }

    public static EventCaptureFormFragment newInstance(
            String eventUid,
            Boolean openErrorSection,
            EventMode eventMode,
            String guid, int status,
            String teiOrgUnit,
            String trackedEntityInstanceId) {
        EventCaptureFormFragment fragment = new EventCaptureFormFragment();
        Bundle args = new Bundle();
        args.putString(Constants.EVENT_UID, eventUid);
        args.putBoolean(OPEN_ERROR_LOCATION, openErrorSection);
        args.putString(EVENT_MODE, eventMode.name());

        args.putString(BIOMETRICS_GUID, guid);
        args.putInt(BIOMETRICS_VERIFICATION_STATUS, status);
        args.putString(BIOMETRICS_TEI_ORGANISATION_UNIT, teiOrgUnit);
        args.putString(BIOMETRICS_TRACKED_ENTITY_INSTANCE_ID, trackedEntityInstanceId);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.activity = (EventCaptureActivity) context;

        biometricsGuid = getArguments().getString(BIOMETRICS_GUID);
        biometricsVerificationStatus = getArguments().getInt(BIOMETRICS_VERIFICATION_STATUS);
        teiOrgUnit = getArguments().getString(BIOMETRICS_TEI_ORGANISATION_UNIT);
        trackedEntityInstanceId =  getArguments().getString(BIOMETRICS_TRACKED_ENTITY_INSTANCE_ID);

        activity.eventCaptureComponent.plus(
                new EventCaptureFormModule(
                        this,
                        getArguments().getString(Constants.EVENT_UID))
        ).inject(this);
        setRetainInstance(true);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        String eventUid = getArguments().getString(Constants.EVENT_UID, "");
        EventMode eventMode = EventMode.valueOf(getArguments().getString(EVENT_MODE));
        formView = new FormView.Builder()
                .locationProvider(locationProvider)
                .onLoadingListener(loading -> {
                    if (loading) {
                        activity.showProgress();
                    } else {
                        activity.hideProgress();
                    }
                    return Unit.INSTANCE;
                })
                .onFocused(() -> {
                    activity.hideNavigationBar();
                    return Unit.INSTANCE;
                })
                .onPercentageUpdate(percentage -> {
                    activity.updatePercentage(percentage);
                    return Unit.INSTANCE;
                }).onItemChangeListener(rowAction ->{
                    activity.refreshProgramStageName();
                    return Unit.INSTANCE;
                })
                .onDataIntegrityResult(result -> {
                    presenter.handleDataIntegrityResult(result, eventMode);
                    return Unit.INSTANCE;
                })
                .factory(activity.getSupportFragmentManager())
                .setRecords(new EventRecords(eventUid, eventMode))
                .openErrorLocation(getArguments().getBoolean(OPEN_ERROR_LOCATION, false))
                .useComposeForm(
                        featureConfig.isFeatureEnable(Feature.COMPOSE_FORMS)
                )
                .onFieldsLoadingListener ( fields -> presenter.onFieldsLoading(fields))
                .build();
        activity.setFormEditionListener(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.section_selector_fragment, container, false);
        EventCaptureContract.Presenter activityPresenter = activity.getPresenter();
        binding.setPresenter(activityPresenter);

        activityPresenter.observeActions().observe(getViewLifecycleOwner(), action ->
        {
            if (action == EventCaptureAction.ON_BACK) {
                formView.onSaveClick();
                activityPresenter.emitAction(EventCaptureAction.NONE);
            }
        });

        binding.actionButton.setOnClickListener(view -> {
            closeKeyboard(view);
            performSaveClick();
        });

        presenter.initBiometricsValues(biometricsGuid, biometricsVerificationStatus, teiOrgUnit, trackedEntityInstanceId);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.formViewContainer, formView).commit();
        formView.setScrollCallback(isSectionVisible -> {
            animateFabButton(isSectionVisible);
            return Unit.INSTANCE;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.showOrHideSaveButton();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case BIOMETRICS_VERIFY_REQUEST: {
                    VerifyResult result = BiometricsClientFactory.INSTANCE.get(
                            this.getContext()).handleVerifyResponse(requestCode, data);

                    if (result instanceof VerifyResult.Match) {
                        presenter.refreshBiometricsStatus(1, true, null);
                    } else if (result instanceof VerifyResult.NoMatch) {
                        presenter.refreshBiometricsStatus(0, true,null);
                    } else if (result instanceof VerifyResult.Failure) {
                        presenter.refreshBiometricsStatus(0, true,null);
                    } else if (result instanceof VerifyResult.AgeGroupNotSupported) {
                        Toast.makeText(getContext(), R.string.age_group_not_supported,
                                Toast.LENGTH_SHORT).show();
                    }

                break;
            }
            case BIOMETRICS_ENROLL_REQUEST: {
                if (data != null) {
                    RegisterResult result = BiometricsClientFactory.INSTANCE.get(
                            this.getContext()).handleRegisterResponse(requestCode, data);
                    if (result instanceof RegisterResult.Completed) {
                        presenter.refreshBiometricsStatus(1, true,((RegisterResult.Completed) result).getGuid());
                    } else if (result instanceof RegisterResult.Failure || result instanceof RegisterResult.RegisterLastFailure) {
                        presenter.refreshBiometricsStatus(0, true, null);
                    } else if (result instanceof RegisterResult.PossibleDuplicates) {
                   /*     presenter.onBiometricsPossibleDuplicates(
                                result.guids,
                                result.sessionId
                        )*/
                    } else if (result instanceof RegisterResult.AgeGroupNotSupported) {
                        Toast.makeText(getContext(), R.string.age_group_not_supported,
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void animateFabButton(boolean sectionIsVisible) {
        int translationX = 1000;
        if (sectionIsVisible) translationX = 0;

        binding.actionButton.animate().translationX(translationX).setDuration(500).start();
    }

    @Override
    public void performSaveClick() {
        formView.onSaveClick();
    }

    @Override
    public void onEditionListener() {
        formView.onEditionFinish();
    }

    @Override
    public void hideSaveButton() {
        binding.actionButton.setVisibility(View.GONE);
    }

    @Override
    public void showSaveButton() {
        binding.actionButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReopen() {
        formView.reload();
    }

    @Override
    public void showNonEditableMessage(@NonNull String reason, boolean canBeReOpened) {
        showNonEditableReasonMessage(
                binding.editableReasonContainer,
                reason,
                canBeReOpened,
                () -> {
                    presenter.reOpenEvent();
                    return Unit.INSTANCE;
                }
        );
    }

    @Override
    public void hideNonEditableMessage() {
        binding.editableReasonContainer.removeAllViews();
    }

    @Override
    public void verifyBiometrics(@Nullable String biometricsGuid, @Nullable String teiOrgUnit, @Nullable String trackedEntityInstanceId, long ageInMonths) {
        HashMap extras = new HashMap<>();
        extras.put(BiometricsClient.SIMPRINTS_TRACKED_ENTITY_INSTANCE_ID, trackedEntityInstanceId);

        BiometricsClientFactory.INSTANCE.get(this.getContext()).verify(this, biometricsGuid, teiOrgUnit, extras, ageInMonths);
    }

    @Override
    public void registerBiometrics(@Nullable String teiOrgUnit, @Nullable String trackedEntityInstanceId, long ageInMonths) {
        HashMap extras = new HashMap<>();
        extras.put(BiometricsClient.SIMPRINTS_TRACKED_ENTITY_INSTANCE_ID, trackedEntityInstanceId);

        BiometricsClientFactory.INSTANCE.get(this.getContext()).registerFromFragment(this, teiOrgUnit, extras , ageInMonths);
    }
}