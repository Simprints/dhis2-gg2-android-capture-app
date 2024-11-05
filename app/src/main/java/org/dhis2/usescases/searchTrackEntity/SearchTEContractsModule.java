package org.dhis2.usescases.searchTrackEntity;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.commons.data.SearchTeiModel;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.Filters;
import org.dhis2.data.biometrics.SimprintsItem;
import org.dhis2.maps.model.StageStyle;
import org.dhis2.usescases.biometrics.ui.SequentialSearch;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import kotlin.Pair;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchTEContractsModule {

    public interface View extends AbstractActivityContracts.View {

        void setPrograms(List<ProgramSpinnerModel> programModels);

        void clearList(String uid);

        String fromRelationshipTEI();

        void showHideFilterGeneral();

        void updateFilters(int totalFilters);

        void openOrgUnitTreeSelector();

        void showPeriodRequest(Pair<FilterManager.PeriodRequest, Filters> periodRequest);

        void clearFilters();

        Consumer<D2Progress> downloadProgress();

        void openDashboard(String teiUid, String programUid, String enrollmentUid, String sessionId);

        void showBreakTheGlass(String teiUid, String enrollmentUid);

        void goToEnrollment(String enrollmentUid, String programUid);

        void onBackClicked();

        void couldNotDownload(String typeName);

        void setInitialFilters(List<FilterItem> filtersToDisplay);

        void hideFilter();

        void showSyncDialog(String teiUid);

        void sendBiometricsConfirmIdentity(String sessionId, String guid, String teiUid,
                String enrollmentUid, boolean isOnline);

        void showBiometricsSearchConfirmation(SearchTeiModel item);
        void sendBiometricsNoneSelected(String sessionId);
        void launchBiometricsIdentify(String moduleId);
    }

    public interface Presenter {

        void init();

        void onDestroy();

        void setProgram(Program programSelected);

        void onClearClick();

        void onBackClick();

        void onEnrollClick(HashMap<String, String> queryData, SequentialSearch sequentialSearch);

        void enroll(String programUid, String teiUid, HashMap<String, String> queryData);

        void onTEIClick(String teiUid, String enrollmentUid, boolean isOnline);
        void onSearchTEIModelClick(SearchTeiModel item, SequentialSearch sequentialSearch);

        TrackedEntityType getTrackedEntityName();

        TrackedEntityType getTrackedEntityType(String trackedEntityTypeUid);

        Program getProgram();

        void addRelationship(@NonNull String teiUid, @Nullable String relationshipTypeUid, boolean online);

        void downloadTei(String teiUid, String enrollmentUid);

        void downloadTeiForRelationship(String TEIuid, String relationshipTypeUid);

        Observable<List<OrganisationUnit>> getOrgUnits();

        String getProgramColor(String uid);

        void onSyncIconClick(String teiUid);

        void showFilterGeneral();

        void clearFilterClick();

        void getMapData();

        Drawable getSymbolIcon();

        Drawable getEnrollmentSymbolIcon();

        HashMap<String, StageStyle> getProgramStageStyle();

        int getTEIColor();

        int getEnrollmentColor();

        void deleteRelationship(String relationshipUid);

        void setProgramForTesting(Program program);

        void clearOtherFiltersIfWebAppIsConfig();

        void setOpeningFilterToNone();

        void setOrgUnitFilters(List<OrganisationUnit> selectedOrgUnits);

        void trackSearchAnalytics();

        void trackSearchMapVisualization();

        void searchOnBiometrics(List <SimprintsItem> simprintsItems, String sessionId, Boolean ageNotSupported);

        boolean getBiometricsSearchStatus();

        void onBiometricsNoneOfTheAboveClick();

        void setBiometricListener(SearchTEPresenter.BiometricsSearchListener biometricsSearchListener);

        void sendBiometricsConfirmIdentity(String teiUid, String enrollmentUid, boolean isOnline);

        String getLastBiometricsSessionId();

        void resetLastBiometricsSessionId();

        void onBiometricsClick();
    }
}
