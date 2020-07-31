package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.models.UpdateType;

@Slf4j
@Service
class UpdateCcdCaseData {
    private final UpdateCcdAppellantData updateCcdAppellantData;
    private final UpdateCcdHearingOptions updateCcdHearingOptions;
    private final UpdateCcdHearingType updateCcdHearingType;
    private final UpdateDwpTimeExtension updateDwpTimeExtension;
    private final UpdateEvents updateEvents;
    private final UpdateCcdRpc updateCcdRpc;

    @Autowired
    UpdateCcdCaseData(UpdateCcdAppellantData updateCcdAppellantData, UpdateCcdHearingOptions updateCcdHearingOptions,
                      UpdateCcdHearingType updateCcdHearingType,
                      UpdateDwpTimeExtension updateDwpTimeExtension, UpdateEvents updateEvents,
                      UpdateCcdRpc updateCcdRpc) {
        this.updateCcdAppellantData = updateCcdAppellantData;
        this.updateCcdHearingOptions = updateCcdHearingOptions;
        this.updateCcdHearingType = updateCcdHearingType;
        this.updateDwpTimeExtension = updateDwpTimeExtension;
        this.updateEvents = updateEvents;
        this.updateCcdRpc = updateCcdRpc;
    }

    UpdateType updateCcdRecordForChangesAndReturnUpdateType(SscsCaseData gapsCaseData,
                                                            SscsCaseData existingCcdCaseData) {

        boolean eventChange = updateEvents.update(gapsCaseData, existingCcdCaseData);
        boolean dataChange = updateCcdData(gapsCaseData, existingCcdCaseData);
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
        log.info("Checking for CCD vs Gap Case Data changes");

        boolean dwpTimeExtension = updateDwpTimeExtension.updateDwpTimeExtension(gapsCaseData, existingCcdCaseData);
        boolean updateParties = false;
        boolean updateHearingOptions = false;
        boolean updateHearingType = false;
        boolean updateRepresentative = false;
        boolean updateRpc = false;

        if (null != gapsCaseData && null != gapsCaseData.getAppeal()) {
            updateParties = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCcdCaseData);
            updateHearingOptions = updateCcdHearingOptions.updateHearingOptions(gapsCaseData, existingCcdCaseData);
            updateHearingType = updateCcdHearingType.updateHearingType(gapsCaseData, existingCcdCaseData);
            updateRepresentative = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCcdCaseData);
            updateRpc = updateCcdRpc.updateCcdRpc(gapsCaseData, existingCcdCaseData);
        }

        boolean ccdDataChanged =  dwpTimeExtension || updateParties || updateHearingOptions || updateHearingType
            || updateRepresentative || updateRpc;

        log.info("updatedCCdData is {} for case {}", ccdDataChanged,
            existingCcdCaseData == null ? "null" : existingCcdCaseData.getCcdCaseId());

        return ccdDataChanged;
    }

}
