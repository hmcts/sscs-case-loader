package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.refdata.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.*;
import uk.gov.hmcts.reform.sscs.services.refdata.RegionalProcessingCenterService;
import uk.gov.hmcts.reform.sscs.util.CcdUtil;


@Service
@Slf4j
public class CcdCasesSender {

    @Value("${rpc.venue.id.enabled}")
    private boolean lookupRpcByVenueId;
    private final CreateCcdService createCcdService;
    private final UpdateCcdService updateCcdService;
    private final RegionalProcessingCenterService regionalProcessingCenterService;

    @Autowired
    CcdCasesSender(CreateCcdService createCcdService,
                   UpdateCcdService updateCcdService,
                   RegionalProcessingCenterService regionalProcessingCenterService) {
        this.createCcdService = createCcdService;
        this.updateCcdService = updateCcdService;
        this.regionalProcessingCenterService = regionalProcessingCenterService;
    }

    public void sendCreateCcdCases(CaseData caseData, IdamTokens idamTokens) {
        if (!lookupRpcByVenueId) {
            addRegionalProcessingCenter(caseData);
        }
        createCcdService.create(caseData, idamTokens);
    }

    public void sendUpdateCcdCases(CaseData caseData, CaseDetails existingCcdCase, IdamTokens idamTokens) {
        String latestEventType = caseData.getLatestEventType();
        if (latestEventType != null) {
            CaseData existingCcdCaseData = CcdUtil.getCaseData(existingCcdCase.getData());
            addMissingInfo(caseData, existingCcdCaseData);
            dontOverwriteSubscriptions(caseData, existingCcdCaseData);
            checkNewEvidenceReceived(caseData, existingCcdCase, idamTokens);
            ifThereIsChangesThenUpdateCase(caseData, existingCcdCaseData, existingCcdCase.getId(), idamTokens);
        }
    }

    private void addMissingInfo(CaseData caseData, CaseData existingCcdCaseData) {
        if (!lookupRpcByVenueId) {
            addRegionalProcessingCenter(caseData);
        }
        addMissingExistingHearings(caseData, existingCcdCaseData);
    }

    private void dontOverwriteSubscriptions(CaseData caseData, CaseData existingCcdCaseData) {
        caseData.setSubscriptions(existingCcdCaseData.getSubscriptions());
    }

    private void ifThereIsChangesThenUpdateCase(CaseData caseData, CaseData existingCcdCaseData, Long existingCaseId,
                                                IdamTokens idamTokens) {
        if (thereIsAnEventChange(caseData, existingCcdCaseData)) {
            updateCcdService.update(caseData, existingCaseId, caseData.getLatestEventType(), idamTokens);
        } else if (thereIsADataChange(caseData, existingCcdCaseData)) {
            updateCcdService.update(caseData, existingCaseId, "caseUpdated", idamTokens);
        } else {
            log.debug("*** case-loader *** No case update needed for case reference: {}", caseData.getCaseReference());
        }
    }

    private void addMissingExistingHearings(CaseData caseData, CaseData existingCcdCaseData) {
        List<Hearing> gaps2Hearings = caseData.getHearings();
        List<Hearing> ccdCaseDataHearings = existingCcdCaseData.getHearings();
        ArrayList<Hearing> hearingArrayList = new ArrayList<>();

        if (null != ccdCaseDataHearings) {
            if (null != gaps2Hearings) {
                Set<String> gaps2HearingDateTime = gaps2Hearings
                    .stream()
                    .map(hearing -> hearing.getValue().getHearingDateTime())
                    .collect(toSet());

                List<Hearing> missingHearings = ccdCaseDataHearings
                    .stream()
                    .filter(hearing ->
                    !gaps2HearingDateTime.contains(hearing.getValue().getHearingDateTime()))
                    .collect(toList());

                hearingArrayList.addAll(gaps2Hearings);
                hearingArrayList.addAll(missingHearings);
                caseData.setHearings(hearingArrayList);

            } else {
                hearingArrayList.addAll(ccdCaseDataHearings);
                caseData.setHearings(hearingArrayList);

            }

        }
    }

    private boolean thereIsAnEventChange(CaseData caseData, CaseData existingCcdCaseData) {
        return existingCcdCaseData.getEvents() == null
            || caseData.getEvents().size() != existingCcdCaseData.getEvents().size();
    }

    private boolean thereIsADataChange(CaseData caseData, CaseData existingCcdCaseData) {
        return !existingCcdCaseData.equals(caseData);
    }

    private void checkNewEvidenceReceived(CaseData caseData, CaseDetails existingCase, IdamTokens idamTokens) {
        Evidence newEvidence = caseData.getEvidence();
        Evidence existingEvidence = buildExistingEvidence(existingCase);
        if (newEvidence != null && existingEvidence != null && !existingEvidence.equals(newEvidence)) {
            updateCcdService.update(caseData, existingCase.getId(), "evidenceReceived", idamTokens);
        }
    }

    private void addRegionalProcessingCenter(CaseData caseData) {
        RegionalProcessingCenter regionalProcessingCenter = regionalProcessingCenterService
            .getByScReferenceCode(caseData.getCaseReference());
        if (null != regionalProcessingCenter) {
            caseData.setRegion(regionalProcessingCenter.getName());
            caseData.setRegionalProcessingCenter(regionalProcessingCenter);
        }
    }

    @SuppressWarnings("unchecked")
    private Evidence buildExistingEvidence(CaseDetails existingCase) {
        HashMap evidence = (HashMap) existingCase.getData().get("evidence");
        List<HashMap<String, Object>> documents = evidence != null
            ? (List<HashMap<String, Object>>)evidence.get("documents") : Collections.emptyList();

        List<Documents> documentList = new ArrayList<>();
        for (HashMap doc : documents) {
            Map<String, Object> docValue = (HashMap<String, Object>) doc.get("value");

            documentList.add(Documents.builder().value(
                Doc.builder()
                    .dateReceived((String) docValue.get("dateReceived"))
                    .evidenceProvidedBy((String) docValue.get("evidenceProvidedBy"))
                    .evidenceType((String) docValue.get("evidenceType"))
                    .build())
                .build());
        }

        return Evidence.builder().documents(documentList).build();
    }
}
