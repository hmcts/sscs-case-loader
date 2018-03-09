package uk.gov.hmcts.reform.sscs.services.ccd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Doc;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Documents;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Evidence;
import uk.gov.hmcts.reform.sscs.services.date.DateHelper;
import uk.gov.hmcts.reform.sscs.services.json.JsonHelper;

@Service
@Slf4j
public class CcdCasesSender {

    private final CreateCoreCaseDataService createCoreCaseDataService;
    private final SearchCoreCaseDataService searchCoreCaseDataService;
    private final UpdateCoreCaseDataService updateCoreCaseDataService;

    @Value("${sscs.case.loader.ignoreCasesBeforeDate}")
    private String ignoreCasesBeforeDateProperty;

    @Autowired
    public CcdCasesSender(CreateCoreCaseDataService createCoreCaseDataService,
                          SearchCoreCaseDataService searchCoreCaseDataService,
                          UpdateCoreCaseDataService updateCoreCaseDataService) {
        this.createCoreCaseDataService = createCoreCaseDataService;
        this.searchCoreCaseDataService = searchCoreCaseDataService;
        this.updateCoreCaseDataService = updateCoreCaseDataService;
    }

    public void sendCreateCcdCases(List<CaseData> caseDataList) {

        LocalDate ignoreCasesBeforeDate = LocalDate.parse(ignoreCasesBeforeDateProperty);
        caseDataList.forEach(caseData -> {

            LocalDate eventDate = DateHelper.convertEventDateToUkLocalDateTime(caseData.getLatestEvent().getDate());

            if (eventDate.isAfter(ignoreCasesBeforeDate) || eventDate.isEqual(ignoreCasesBeforeDate)) {
                log.info("*** case-loader *** About to save case into CCD for case reference: {}",
                    caseData.getCaseReference());
                List<CaseDetails> cases = searchCoreCaseDataService.findCaseByCaseRef(caseData.getCaseReference());
                if (cases.isEmpty()) {
                    CaseDetails caseDetails = createCoreCaseDataService.createCcdCase(caseData);
                    log.info("*** case-loader *** Saved case into CCD successfully: {}",
                        JsonHelper.printCaseDetailsInJson(caseDetails));
                }
            }
        });
    }

    public void sendUpdateCcdCases(List<CaseData> caseDataList) {
        for (CaseData caseData : caseDataList) {
            log.info("*** case-loader *** Found potential case to update in Delta for case reference: {}",
                caseData.getCaseReference());
            List<CaseDetails> cases = searchCoreCaseDataService.findCaseByCaseRef(caseData.getCaseReference());
            if (!cases.isEmpty()) {
                log.info("*** case-loader *** {} Cases found with caseRef: {} in CCD",
                    cases.size(), caseData.getCaseReference());

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
