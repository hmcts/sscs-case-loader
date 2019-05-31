package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.sscs.models.GapsEvent.APPEAL_RECEIVED;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.UpdateType;
import uk.gov.hmcts.reform.sscs.service.RegionalProcessingCenterService;


@Service
@Slf4j
public class CcdCasesSender {

    private static final String SSCS_APPEAL_UPDATED_EVENT = "SSCS - appeal updated event";
    private static final String UPDATED_SSCS = "Updated SSCS";
    @Value("${rpc.venue.id.enabled}")
    private boolean lookupRpcByVenueId;
    private final CcdService ccdService;
    private final UpdateCcdCaseService updateCcdCaseService;
    private final RegionalProcessingCenterService regionalProcessingCenterService;
    private final UpdateCcdCaseData updateCcdCaseData;
    private String logPrefix = "";

    @Autowired
    CcdCasesSender(CcdService ccdService,
                   UpdateCcdCaseService updateCcdCaseService,
                   RegionalProcessingCenterService regionalProcessingCenterService,
                   UpdateCcdCaseData updateCcdCaseData) {
        this.updateCcdCaseService = updateCcdCaseService;
        this.regionalProcessingCenterService = regionalProcessingCenterService;
        this.ccdService = ccdService;
        this.updateCcdCaseData = updateCcdCaseData;
    }

    public void setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
    }

    public void sendCreateCcdCases(SscsCaseData caseData, IdamTokens idamTokens) {
        if (!lookupRpcByVenueId) {
            addRegionalProcessingCenter(caseData);
        }
        ccdService.createCase(caseData, "appealCreated", "SSCS - new case created",
            "Created SSCS case from Case Loader with event appealCreated", idamTokens);
    }

    public void sendUpdateCcdCases(SscsCaseData caseData, SscsCaseDetails existingCcdCase, IdamTokens idamTokens) {
        String latestEventType = caseData.getLatestEventType();
        if (latestEventType != null) {
            SscsCaseData existingCcdCaseData = existingCcdCase.getData();
            addMissingInfo(caseData, existingCcdCaseData);
            checkNewEvidenceReceived(caseData, existingCcdCase, idamTokens);
            ifThereIsChangesThenUpdateCase(caseData, existingCcdCaseData, existingCcdCase.getId(), idamTokens);
        }
    }

    private void addMissingInfo(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        if (!lookupRpcByVenueId) {
            addRegionalProcessingCenter(existingCcdCaseData);
        }
        addMissingExistingHearings(caseData, existingCcdCaseData);
    }

    private void ifThereIsChangesThenUpdateCase(SscsCaseData caseData, SscsCaseData existingCcdCaseData,
                                                Long existingCaseId, IdamTokens idamTokens) {

        UpdateType updateType = updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(
            caseData, existingCcdCaseData);
        if (UpdateType.EVENT_UPDATE == updateType) {
            updateCase(caseData, existingCcdCaseData, existingCaseId, idamTokens, caseData.getLatestEventType());
        } else if (hasCaseRefBeenAdded(caseData, existingCcdCaseData)) {
            //Override event to appealReceived if new case ref has been added
            updateCase(caseData, existingCcdCaseData, existingCaseId, idamTokens, APPEAL_RECEIVED.getType());
        } else if (UpdateType.DATA_UPDATE == updateType) {
            updateCase(caseData, existingCcdCaseData, existingCaseId, idamTokens, "caseUpdated");
        } else {
            log.debug(logPrefix + " No case update needed for case reference: {}", caseData.getCaseReference());
        }
    }

    private void updateCase(SscsCaseData caseData, SscsCaseData existingCcdCaseData, Long existingCaseId,
                            IdamTokens idamTokens, String eventType) {
        existingCcdCaseData.setCaseReference(caseData.getCaseReference());
        updateCcdCaseService
            .updateCase(existingCcdCaseData, existingCaseId, eventType,
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

    private void addRegionalProcessingCenter(SscsCaseData caseData) {
        RegionalProcessingCenter regionalProcessingCenter = regionalProcessingCenterService
            .getByScReferenceCode(caseData.getCaseReference());
        if (null != regionalProcessingCenter) {
            caseData.setRegion(regionalProcessingCenter.getName());
            caseData.setRegionalProcessingCenter(regionalProcessingCenter);
        }
    }

}
