package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.UpdateType;


@Service
@Slf4j
public class CcdCasesSender {

    private static final String SSCS_APPEAL_UPDATED_EVENT = "SSCS - appeal updated event";
    private static final String UPDATED_SSCS = "Updated SSCS";
    private final UpdateCcdCaseService updateCcdCaseService;
    private final UpdateCcdCaseData updateCcdCaseData;
    private String logPrefix = "";

    @Autowired
    CcdCasesSender(UpdateCcdCaseService updateCcdCaseService,
                   UpdateCcdCaseData updateCcdCaseData) {
        this.updateCcdCaseService = updateCcdCaseService;
        this.updateCcdCaseData = updateCcdCaseData;
    }

    public void setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
    }

    public void sendUpdateCcdCases(SscsCaseData gapsCaseData, SscsCaseDetails existingCcdCase, IdamTokens idamTokens) {

        String latestEventType = gapsCaseData.getLatestEventType();

        if (latestEventType != null) {

            log.info("Found latest event type {} in gaps case for case id {}",
                latestEventType, existingCcdCase.getId());

            SscsCaseData existingCcdCaseData = existingCcdCase.getData();

            addMissingExistingHearings(gapsCaseData, existingCcdCaseData);

            checkNewEvidenceReceived(gapsCaseData, existingCcdCase, idamTokens);

            ifThereIsChangesThenUpdateCase(gapsCaseData, existingCcdCaseData, existingCcdCase.getId(), idamTokens);
        }
    }

    private void ifThereIsChangesThenUpdateCase(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData,
                                                Long existingCaseId, IdamTokens idamTokens) {

        UpdateType updateType = updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(
            gapsCaseData, existingCcdCaseData);

        log.info("Case Update Type {} for case id {}", updateType.toString(), existingCaseId);

        if (isNotValidForDigitalCase(gapsCaseData, existingCcdCaseData)) {
            updateCase(gapsCaseData, existingCcdCaseData, existingCaseId, idamTokens, CASE_UPDATED.getCcdType());
        } else if (UpdateType.EVENT_UPDATE == updateType) {
            updateCase(gapsCaseData, existingCcdCaseData, existingCaseId, idamTokens,
                gapsCaseData.getLatestEventType());
        } else if (hasCaseRefBeenAdded(gapsCaseData, existingCcdCaseData)) {
            //Override event to appealReceived if new case ref has been added
            updateCase(gapsCaseData, existingCcdCaseData, existingCaseId, idamTokens, APPEAL_RECEIVED.getCcdType());
        } else if (UpdateType.DATA_UPDATE == updateType) {
            updateCase(gapsCaseData, existingCcdCaseData, existingCaseId, idamTokens, CASE_UPDATED.getCcdType());
        } else {
            log.debug(logPrefix + " No case update needed for case reference: {}", gapsCaseData.getCaseReference());
        }
    }

    private boolean isNotValidForDigitalCase(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        return (caseData.getLatestEventType().equals(DWP_RESPOND.getCcdType())
            || caseData.getLatestEventType().equals(APPEAL_RECEIVED.getCcdType()))
            && existingCcdCaseData.getCreatedInGapsFrom() != null
            && existingCcdCaseData.getCreatedInGapsFrom().equals(READY_TO_LIST.getCcdType());
    }

    private void updateCase(SscsCaseData caseData, SscsCaseData existingCcdCaseData, Long existingCaseId,
                            IdamTokens idamTokens, String eventType) {

        existingCcdCaseData.setCaseReference(caseData.getCaseReference());

        updateCcdCaseService.updateCase(existingCcdCaseData, existingCaseId, eventType,
                SSCS_APPEAL_UPDATED_EVENT, UPDATED_SSCS, idamTokens);
    }

    private boolean hasCaseRefBeenAdded(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        return null != existingCcdCaseData
            && StringUtils.isBlank(existingCcdCaseData.getCaseReference())
            && !StringUtils.isBlank(caseData.getCaseReference());
    }

    private void addMissingExistingHearings(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        List<Hearing> gaps2Hearings = caseData.getHearings();
        List<Hearing> ccdCaseDataHearings = existingCcdCaseData.getHearings();
        ArrayList<Hearing> hearingArrayList = new ArrayList<>();

        if (null != ccdCaseDataHearings) {
            if (null != gaps2Hearings) {
                Set<String> gaps2HearingDateTime = gaps2Hearings
                    .stream()
                    .map(hearing -> getMissingHearingDateTime(hearing.getValue()))
                    .collect(toSet());

                List<Hearing> missingHearings = ccdCaseDataHearings
                    .stream()
                    .filter(hearing ->
                        !gaps2HearingDateTime.contains(getMissingHearingDateTime(hearing.getValue())))
                    .collect(toList());

                hearingArrayList.addAll(gaps2Hearings);
                hearingArrayList.addAll(missingHearings);
            } else {
                hearingArrayList.addAll(ccdCaseDataHearings);
            }

            existingCcdCaseData.setHearings(hearingArrayList);
        } else {
            existingCcdCaseData.setHearings(gaps2Hearings);
        }
    }

    private String getMissingHearingDateTime(HearingDetails details) {
        return details.getHearingDate() + details.getTime();
    }

    private void checkNewEvidenceReceived(SscsCaseData caseData, SscsCaseDetails existingCase, IdamTokens idamTokens) {
        Evidence newEvidence = caseData.getEvidence();
        SscsCaseData existingCaseData = existingCase.getData();
        Evidence existingEvidence = existingCaseData.getEvidence();
        if (newEvidence != null && !CollectionUtils.isEmpty(newEvidence.getDocuments())) {
            Evidence updatedEvidence = addNewEvidenceIfFound(newEvidence, existingEvidence);
            if (updatedEvidence != null) {
                existingCaseData.setEvidence(updatedEvidence);
                updateCcdCaseService.updateCase(existingCaseData, existingCase.getId(), "evidenceReceived",
                        SSCS_APPEAL_UPDATED_EVENT, UPDATED_SSCS, idamTokens);
            }
        }
    }

    private Evidence addNewEvidenceIfFound(Evidence newEvidence, Evidence existingEvidence) {
        Evidence updatedEvidence = null;

        if (existingEvidence == null || CollectionUtils.isEmpty(existingEvidence.getDocuments())) {
            updatedEvidence = newEvidence;
        } else {
            List<Document> newToAddDocuments = newEvidence.getDocuments().stream()
                    .filter(nd -> !existingEvidence.getDocuments().contains(nd)).collect(toList());

            if (!CollectionUtils.isEmpty(newToAddDocuments)) {
                existingEvidence.getDocuments().addAll(newToAddDocuments);
                updatedEvidence = existingEvidence;
            }
        }
        return updatedEvidence;
    }

}
