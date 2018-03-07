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
    private final SearchCoreCaseDataService searchCoreCaseDataService;
    private final UpdateCoreCaseDataService updateCoreCaseDataService;

    @Autowired
    public CcdCasesSender(CreateCoreCaseDataService createCoreCaseDataService,
                          SearchCoreCaseDataService searchCoreCaseDataService,
                          UpdateCoreCaseDataService updateCoreCaseDataService) {
        this.createCoreCaseDataService = createCoreCaseDataService;
        this.searchCoreCaseDataService = searchCoreCaseDataService;
        this.updateCoreCaseDataService = updateCoreCaseDataService;
    }

    public void sendCreateCcdCases(List<CaseData> caseDataList) {
        caseDataList.forEach(caseData -> {
            log.info("*** case-loader *** About to save case into CCD: {}",
                JsonHelper.printCaseDetailsInJson(caseData));
            List<CaseDetails> cases = searchCoreCaseDataService.findCaseByCaseRef(caseData.getCaseReference());
            if (cases.isEmpty()) {
                CaseDetails caseDetails = createCoreCaseDataService.createCcdCase(caseData);
                log.info("*** case-loader *** Save case into CCD successfully: {}",
                    JsonHelper.printCaseDetailsInJson(caseDetails));
            }
        });
    }

    public void sendUpdateCcdCases(List<CaseData> caseDataList) {
        for (CaseData caseData : caseDataList) {
            log.info("*** case-loader *** Found potential case to update in Delta: {}", caseData.getCaseReference());
            List<CaseDetails> cases = searchCoreCaseDataService.findCaseByCaseRef(caseData.getCaseReference());
            log.info("*** case-loader *** Cases found with caseRef: {} in CCD: {}", caseData.getCaseReference(),
                JsonHelper.printCaseDetailsInJson(cases));
            if (!cases.isEmpty()) {
                String latestEventType = caseData.getLatestEventType();
                if (latestEventType != null) {
                    CaseDetails existingCcdCase = cases.get(0);
                    checkNewEvidenceReceived(caseData, existingCcdCase);
                    ifThereIsEventChangesThenUpdateCase(caseData, existingCcdCase);
                }
            }
        }
    }

    private void ifThereIsEventChangesThenUpdateCase(CaseData caseData, CaseDetails existingCcdCase) {
        CaseDetails caseDetails;
        log.info("*** case-loader *** About to update case in CCD: {}",
            JsonHelper.printCaseDetailsInJson(caseData));

        caseDetails = updateCoreCaseDataService.updateCase(caseData, existingCcdCase.getId(),
            caseData.getLatestEventType());
        log.info("*** case-loader *** case events updated in CCD successfully: {}",
            JsonHelper.printCaseDetailsInJson(caseDetails));
    }

    private void checkNewEvidenceReceived(CaseData caseData, CaseDetails existingCase) {
        Evidence newEvidence = caseData.getEvidence();
        Evidence existingEvidence = buildExistingEvidence(existingCase);
        if (newEvidence != null && existingEvidence != null && !existingEvidence.equals(newEvidence)) {
            CaseDetails caseDetails = updateCoreCaseDataService
                .updateCase(caseData, existingCase.getId(), "evidenceReceived");
            log.info("*** case-loader *** New evidence received event: {}",
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
