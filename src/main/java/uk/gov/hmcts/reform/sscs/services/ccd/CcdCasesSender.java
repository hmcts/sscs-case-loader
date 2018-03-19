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
    CcdCasesSender(CcdApiWrapper ccdApiWrapper) {
        this.ccdApiWrapper = ccdApiWrapper;
    }

    public void sendCreateCcdCases(CaseData caseData, String idamOauth2Token, String serviceAuthorization) {
        ccdApiWrapper.create(caseData, idamOauth2Token, serviceAuthorization);
    }

    public void sendUpdateCcdCases(CaseData caseData, CaseDetails existingCcdCase, String idamOauth2Token) {
        String latestEventType = caseData.getLatestEventType();
        if (latestEventType != null) {
            checkNewEvidenceReceived(caseData, existingCcdCase, idamOauth2Token);
            ifThereIsEventChangesThenUpdateCase(caseData, existingCcdCase, idamOauth2Token);
        }
    }

    private void ifThereIsEventChangesThenUpdateCase(CaseData caseData, CaseDetails existingCcdCase,
                                                     String idamOauth2Token) {
        if (thereIsAnEventChange(caseData, existingCcdCase)) {
            ccdApiWrapper.update(caseData, existingCcdCase.getId(), caseData.getLatestEventType(), idamOauth2Token);
        } else {
            log.debug("*** case-loader *** No case update needed for case reference: {}", caseData.getCaseReference());
        }
    }

    private boolean thereIsAnEventChange(CaseData caseData, CaseDetails existingCcdCase) {
        List eventObjects = (ArrayList) existingCcdCase.getData().get("events");
        return eventObjects == null || caseData.getEvents().size() != eventObjects.size();
    }

    private void checkNewEvidenceReceived(CaseData caseData, CaseDetails existingCase, String idamOauth2Token) {
        Evidence newEvidence = caseData.getEvidence();
        Evidence existingEvidence = buildExistingEvidence(existingCase);
        if (newEvidence != null && existingEvidence != null && !existingEvidence.equals(newEvidence)) {
            ccdApiWrapper.update(caseData, existingCase.getId(), "evidenceReceived", idamOauth2Token);
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
