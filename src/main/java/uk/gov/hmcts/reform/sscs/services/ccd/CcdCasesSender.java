package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.*;
import uk.gov.hmcts.reform.sscs.services.refdata.RegionalProcessingCenterService;
import uk.gov.hmcts.reform.sscs.util.CcdUtil;


@Service
@Slf4j
public class CcdCasesSender {

    public static final String REGION = "region";
    private final CreateCcdService createCcdService;
    private final UpdateCcdService updateCcdService;

    @Autowired
    CcdCasesSender(CreateCcdService createCcdService,
                   UpdateCcdService updateCcdService,
                   RegionalProcessingCenterService regionalProcessingCenterService) {
        this.createCcdService = createCcdService;
        this.updateCcdService = updateCcdService;
    }

    public void sendCreateCcdCases(CaseData caseData, IdamTokens idamTokens) {
        createCcdService.create(caseData, idamTokens);
    }

    public void sendUpdateCcdCases(CaseData caseData, CaseDetails existingCcdCase, IdamTokens idamTokens) {
        String latestEventType = caseData.getLatestEventType();
        if (latestEventType != null) {
            dontOverwriteSubscriptions(caseData);
            dontOverwriteAppealData(caseData, existingCcdCase);
            checkNewEvidenceReceived(caseData, existingCcdCase, idamTokens);
            ifThereIsEventChangesThenUpdateCase(caseData, existingCcdCase, idamTokens);
        }
    }

    private void dontOverwriteSubscriptions(CaseData caseData) {
        caseData.setSubscriptions(null);
    }

    private void dontOverwriteAppealData(CaseData caseData, CaseDetails existingCcdCase) {
        CaseData existingCaseData = CcdUtil.getCaseData(existingCcdCase.getData());
        Appeal existingAppeal = existingCaseData.getAppeal();

        if (caseData.getAppeal() != null) {
            existingAppeal.setAppellant(caseData.getAppeal().getAppellant());
            existingAppeal.setBenefitType(caseData.getAppeal().getBenefitType());
            caseData.setAppeal(existingAppeal);
        }
    }

    private void ifThereIsEventChangesThenUpdateCase(CaseData caseData, CaseDetails existingCcdCase,
                                                     IdamTokens idamTokens) {
        if (thereIsAnEventChange(caseData, existingCcdCase)) {
            addMissingExistingHearings(caseData, existingCcdCase);
            updateCcdService.update(caseData, existingCcdCase.getId(), caseData.getLatestEventType(), idamTokens);
        } else {
            log.debug("*** case-loader *** No case update needed for case reference: {}", caseData.getCaseReference());
        }
    }

    private void addMissingExistingHearings(CaseData caseData, CaseDetails existingCcdCase) {
        List<Hearing> gaps2Hearings = caseData.getHearings();
        CaseData ccdCaseData = CcdUtil.getCaseData(existingCcdCase.getData());
        List<Hearing> ccdCaseDataHearings = ccdCaseData.getHearings();
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

    private boolean thereIsAnEventChange(CaseData caseData, CaseDetails existingCcdCase) {
        List eventObjects = (ArrayList) existingCcdCase.getData().get("events");
        return eventObjects == null || caseData.getEvents().size() != eventObjects.size();
    }

    private void checkNewEvidenceReceived(CaseData caseData, CaseDetails existingCase, IdamTokens idamTokens) {
        Evidence newEvidence = caseData.getEvidence();
        Evidence existingEvidence = buildExistingEvidence(existingCase);
        if (newEvidence != null && existingEvidence != null && !existingEvidence.equals(newEvidence)) {
            updateCcdService.update(caseData, existingCase.getId(), "evidenceReceived", idamTokens);
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
