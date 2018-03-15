package uk.gov.hmcts.reform.sscs.services.ccd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Doc;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Documents;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Evidence;
import uk.gov.hmcts.reform.sscs.services.json.JsonHelper;

@Service
@Slf4j
public class CcdCasesSender {

    private final CreateCoreCaseDataService createCoreCaseDataService;
    private final UpdateCoreCaseDataService updateCoreCaseDataService;

    @Autowired
    public CcdCasesSender(CreateCoreCaseDataService createCoreCaseDataService,
                          UpdateCoreCaseDataService updateCoreCaseDataService) {
        this.createCoreCaseDataService = createCoreCaseDataService;
        this.updateCoreCaseDataService = updateCoreCaseDataService;
    }

    public void sendCreateCcdCases(CaseData caseData) {
        createCoreCaseDataService.createCcdCase(caseData);
    }

    public void sendUpdateCcdCases(CaseData caseData, CaseDetails existingCcdCase) {
        String latestEventType = caseData.getLatestEventType();
        if (latestEventType != null) {
            checkNewEvidenceReceived(caseData, existingCcdCase);
            ifThereIsEventChangesThenUpdateCase(caseData, existingCcdCase);
        }
    }

    private void ifThereIsEventChangesThenUpdateCase(CaseData caseData, CaseDetails existingCcdCase) {
        if (isThereAnEventChange(caseData, existingCcdCase)) {
            log.info("*** case-loader *** About to update case with new event in CCD for case reference: {}",
                caseData.getCaseReference());
            CaseDetails caseDetails = updateCoreCaseDataService.updateCase(caseData, existingCcdCase.getId(),
                caseData.getLatestEventType());
            log.info("*** case-loader *** case events updated in CCD successfully: {}",
                JsonHelper.printCaseDetailsInJson(caseDetails));
        } else {
            log.info("*** case-loader *** No case update needed for case reference: {}", caseData.getCaseReference());
        }
    }

    private boolean isThereAnEventChange(CaseData caseData, CaseDetails existingCcdCase) {
        List eventObjects = (ArrayList) existingCcdCase.getData().get("events");
        return eventObjects == null || caseData.getEvents().size() != eventObjects.size();
    }

    private void checkNewEvidenceReceived(CaseData caseData, CaseDetails existingCase) {
        Evidence newEvidence = caseData.getEvidence();
        Evidence existingEvidence = buildExistingEvidence(existingCase);
        if (newEvidence != null && existingEvidence != null && !existingEvidence.equals(newEvidence)) {
            log.info("*** case-loader *** About to update case with evidence received in CCD for case reference: {}",
                caseData.getCaseReference());
            CaseDetails caseDetails = updateCoreCaseDataService.updateCase(caseData, existingCase.getId(),
                "evidenceReceived");
            log.info("*** case-loader *** New evidence received event updated in CCD for case: {}",
                JsonHelper.printCaseDetailsInJson(caseDetails));
        }
    }

    @SuppressWarnings("unchecked")
    private Evidence buildExistingEvidence(CaseDetails existingCase) {
        List<HashMap<String, Object>> documents = (List<HashMap<String, Object>>) (
            (HashMap) existingCase.getData().get("evidence")).get("documents");

        List<Documents> documentList = new ArrayList<>();
        for (HashMap doc : documents) {
            Map<String, Object> docValue = (HashMap<String, Object>) doc.get("value");

            documentList.add(Documents.builder().value(
                Doc.builder()
                    .dateReceived((String) docValue.get("dateReceived"))
                    .description((String) docValue.get("description"))
                    .build())
                .build());
        }

        return Evidence.builder().documents(documentList).build();
    }
}
