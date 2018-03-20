package uk.gov.hmcts.reform.sscs.services.ccd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Doc;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Documents;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Evidence;

@Service
@Slf4j
public class CcdCasesSender {

    private final CcdApiWrapper ccdApiWrapper;
    private final UpdateCcdService updateCcdService;

    @Autowired
    CcdCasesSender(CcdApiWrapper ccdApiWrapper, UpdateCcdService updateCcdService) {
        this.ccdApiWrapper = ccdApiWrapper;
        this.updateCcdService = updateCcdService;
    }

    public void sendCreateCcdCases(CaseData caseData, IdamTokens idamTokens) {
        ccdApiWrapper.create(caseData, idamTokens);
    }

    public void sendUpdateCcdCases(CaseData caseData, CaseDetails existingCcdCase, IdamTokens idamTokens) {
        String latestEventType = caseData.getLatestEventType();
        if (latestEventType != null) {
            checkNewEvidenceReceived(caseData, existingCcdCase, idamTokens);
            ifThereIsEventChangesThenUpdateCase(caseData, existingCcdCase, idamTokens);
        }
    }

    private void ifThereIsEventChangesThenUpdateCase(CaseData caseData, CaseDetails existingCcdCase,
                                                     IdamTokens idamTokens) {
        if (thereIsAnEventChange(caseData, existingCcdCase)) {
            updateCcdService.update(caseData, existingCcdCase.getId(), caseData.getLatestEventType(), idamTokens);
        } else {
            log.debug("*** case-loader *** No case update needed for case reference: {}", caseData.getCaseReference());
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
        List<HashMap<String, Object>> documents = (List<HashMap<String, Object>>) (
            (HashMap) existingCase.getData().get("evidence")).get("documents");

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
