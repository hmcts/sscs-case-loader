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

@Service
@Slf4j
public class CcdCasesSender {

    private final CcdApiWrapper ccdApiWrapper;

    @Autowired
    public CcdCasesSender(CcdApiWrapper ccdApiWrapper) {
        this.ccdApiWrapper = ccdApiWrapper;
    }

    public void sendCreateCcdCases(CaseData caseData) {
        ccdApiWrapper.create(caseData);
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
            ccdApiWrapper.update(caseData, existingCcdCase.getId(), caseData.getLatestEventType());
        } else {
            log.debug("*** case-loader *** No case update needed for case reference: {}", caseData.getCaseReference());
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
            ccdApiWrapper.update(caseData, existingCase.getId(), "evidenceReceived");
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
