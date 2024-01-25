package uk.gov.hmcts.reform.sscs.services.ccd;

import static uk.gov.hmcts.reform.sscs.exceptions.FeignExceptionLogger.debugCaseLoaderException;

import feign.FeignException;
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
    private final UpdateCcdProcessingVenue updateCcdProcessingVenue;

    @Autowired
    UpdateCcdCaseData(UpdateCcdAppellantData updateCcdAppellantData, UpdateCcdHearingOptions updateCcdHearingOptions,
                      UpdateCcdHearingType updateCcdHearingType,
                      UpdateDwpTimeExtension updateDwpTimeExtension, UpdateEvents updateEvents,
                      UpdateCcdRpc updateCcdRpc,
                      UpdateCcdProcessingVenue updateCcdProcessingVenue) {
        this.updateCcdAppellantData = updateCcdAppellantData;
        this.updateCcdHearingOptions = updateCcdHearingOptions;
        this.updateCcdHearingType = updateCcdHearingType;
        this.updateDwpTimeExtension = updateDwpTimeExtension;
        this.updateEvents = updateEvents;
        this.updateCcdRpc = updateCcdRpc;
        this.updateCcdProcessingVenue = updateCcdProcessingVenue;
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
        boolean updateRep = false;
        boolean updateRpc = false;
        boolean updateProcessingVenue = false;

        if (null != gapsCaseData && null != gapsCaseData.getAppeal()) {
            try {
                updateParties = updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCcdCaseData);
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update Appellant Details");
            }

            try {
                updateHearingOptions = updateCcdHearingOptions.updateHearingOptions(gapsCaseData, existingCcdCaseData);
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update Hearing Options");
            }

            try {
                updateHearingType = updateCcdHearingType.updateHearingType(gapsCaseData, existingCcdCaseData);
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update Hearing Type");
            }

            try {
                updateRep = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCcdCaseData);
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update Representative Details");
            }

            try {
                updateRpc = updateCcdRpc.updateCcdRpc(gapsCaseData, existingCcdCaseData);
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update Regional Processing Center Details");
            }

            try {
                updateProcessingVenue = updateCcdProcessingVenue.updateVenue(gapsCaseData, existingCcdCaseData);
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update Processing Venue Details");
            }
        }

        boolean ccdDataChanged =  dwpTimeExtension || updateParties || updateHearingOptions || updateHearingType
            || updateRep || updateRpc || updateProcessingVenue;

        log.info("updatedCCdData is {} for case {}", ccdDataChanged,
            existingCcdCaseData == null ? "null" : existingCcdCaseData.getCcdCaseId());

        return ccdDataChanged;
    }

}
