package uk.gov.hmcts.reform.sscs.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;
import uk.gov.hmcts.reform.sscs.models.UpdateType;

@Service
class UpdateCcdCaseData {
    private static final Subscriptions EMPTY_SUBSCRIPTIONS = Subscriptions.builder().build();
    private final UpdateCcdAppellantData updateCcdAppellantData;
    private final UpdateCcdHearingOptions updateCcdHearingOptions;
    private final UpdateCcdHearingType updateCcdHearingType;
    private final UpdateGeneratedFields updateGeneratedFields;
    private final UpdateDwpTimeExtension updateDwpTimeExtension;
    private final UpdateEvents updateEvents;

    @Autowired
    UpdateCcdCaseData(UpdateCcdAppellantData updateCcdAppellantData, UpdateCcdHearingOptions updateCcdHearingOptions,
                      UpdateCcdHearingType updateCcdHearingType, UpdateGeneratedFields updateGeneratedFields,
                      UpdateDwpTimeExtension updateDwpTimeExtension, UpdateEvents updateEvents) {
        this.updateCcdAppellantData = updateCcdAppellantData;
        this.updateCcdHearingOptions = updateCcdHearingOptions;
        this.updateCcdHearingType = updateCcdHearingType;
        this.updateGeneratedFields = updateGeneratedFields;
        this.updateDwpTimeExtension = updateDwpTimeExtension;
        this.updateEvents = updateEvents;
    }

    UpdateType updateCcdRecordForChangesAndReturnUpdateType(SscsCaseData gapsCaseData,
                                                            SscsCaseData existingCcdCaseData) {
        boolean eventChange = updateEvents.update(gapsCaseData, existingCcdCaseData);
        boolean dataChange = updateCcdData(gapsCaseData, existingCcdCaseData);
        updateGeneratedFields.updateGeneratedFields(existingCcdCaseData);
        return workOutUpdateType(eventChange, dataChange);
    }

    private UpdateType workOutUpdateType(boolean eventChanged, boolean dataChange) {
        if (eventChanged) {
            return UpdateType.EVENT_UPDATE;
        } else if (dataChange) {
            return UpdateType.DATA_UPDATE;
        }
        return UpdateType.NO_UPDATE;
    }

    private boolean updateCcdData(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {
        boolean dwpTimeExtension = updateDwpTimeExtension.updateDwpTimeExtension(gapsCaseData, existingCcdCaseData);
        boolean updateParties = false;
        boolean updateHearingOptions = false;
        boolean updateHearingType = false;
        boolean updateRepresentative = false;

        if (null != gapsCaseData && null != gapsCaseData.getAppeal()) {
            updateParties = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCcdCaseData);
            updateHearingOptions = updateCcdHearingOptions.updateHearingOptions(gapsCaseData, existingCcdCaseData);
            updateHearingType = updateCcdHearingType.updateHearingType(gapsCaseData, existingCcdCaseData);
            updateRepresentative = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCcdCaseData);
        }
        return dwpTimeExtension || updateParties || updateHearingOptions || updateHearingType || updateRepresentative;
    }

}
