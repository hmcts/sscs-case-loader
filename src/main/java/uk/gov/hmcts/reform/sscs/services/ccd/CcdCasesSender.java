package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsCcdConvertService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.refdata.RegionalProcessingCenterService;


@Service
@Slf4j
public class CcdCasesSender {

    @Value("${rpc.venue.id.enabled}")
    private boolean lookupRpcByVenueId;
    private final CreateCcdService createCcdService;
    private final UpdateCcdService updateCcdService;
    private final RegionalProcessingCenterService regionalProcessingCenterService;
    private final SscsCcdConvertService sscsCcdConvertService;

    @Autowired
    CcdCasesSender(CreateCcdService createCcdService,
                   UpdateCcdService updateCcdService,
                   RegionalProcessingCenterService regionalProcessingCenterService,
                   SscsCcdConvertService sscsCcdConvertService) {
        this.createCcdService = createCcdService;
        this.updateCcdService = updateCcdService;
        this.regionalProcessingCenterService = regionalProcessingCenterService;
        this.sscsCcdConvertService = sscsCcdConvertService;
    }

    public void sendCreateCcdCases(SscsCaseData caseData, IdamTokens idamTokens) {
        if (!lookupRpcByVenueId) {
            addRegionalProcessingCenter(caseData);
        }
        createCcdService.create(caseData, idamTokens);
    }

    public void sendUpdateCcdCases(SscsCaseData caseData, CaseDetails existingCcdCase, IdamTokens idamTokens) {
        String latestEventType = caseData.getLatestEventType();
        if (latestEventType != null) {
            SscsCaseData existingCcdCaseData = sscsCcdConvertService.getCaseData(existingCcdCase.getData());
            addMissingInfo(caseData, existingCcdCaseData);
            dontOverwriteSubscriptions(caseData);
            checkNewEvidenceReceived(caseData, existingCcdCase, idamTokens);
            ifThereIsChangesThenUpdateCase(caseData, existingCcdCaseData, existingCcdCase.getId(), idamTokens);
        }
    }

    private void addMissingInfo(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        if (!lookupRpcByVenueId) {
            addRegionalProcessingCenter(caseData);
        }
        addMissingExistingHearings(caseData, existingCcdCaseData);
    }

    private void dontOverwriteSubscriptions(SscsCaseData caseData) {
        caseData.setSubscriptions(null);
    }

    private void ifThereIsChangesThenUpdateCase(SscsCaseData caseData, SscsCaseData existingCcdCaseData,
                                                Long existingCaseId, IdamTokens idamTokens) {
        if (thereIsAnEventChange(caseData, existingCcdCaseData)) {
            updateCcdService.update(caseData, existingCaseId, caseData.getLatestEventType(), idamTokens);
        } else if (thereIsADataChange(caseData, existingCcdCaseData)) {
            updateCcdService.update(caseData, existingCaseId, "caseUpdated", idamTokens);
        } else {
            log.debug("*** case-loader *** No case update needed for case reference: {}", caseData.getCaseReference());
        }
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
                caseData.setHearings(hearingArrayList);

            } else {
                hearingArrayList.addAll(ccdCaseDataHearings);
                caseData.setHearings(hearingArrayList);

            }

        }
    }

    private String getMissingHearingDateTime(HearingDetails details) {
        return details.getHearingDate() + details.getTime();
    }

    private boolean thereIsAnEventChange(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        return existingCcdCaseData.getEvents() == null
            || caseData.getEvents().size() != existingCcdCaseData.getEvents().size();
    }

    private boolean thereIsADataChange(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        return !existingCcdCaseData.equals(caseData);
    }

    private void checkNewEvidenceReceived(SscsCaseData caseData, CaseDetails existingCase, IdamTokens idamTokens) {
        Evidence newEvidence = caseData.getEvidence();
        Evidence existingEvidence = buildExistingEvidence(existingCase);
        if (newEvidence != null && existingEvidence != null && !existingEvidence.equals(newEvidence)) {
            updateCcdService.update(caseData, existingCase.getId(), "evidenceReceived", idamTokens);
        }
    }

    private void addRegionalProcessingCenter(SscsCaseData caseData) {
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

        List<Document> documentList = new ArrayList<>();
        for (HashMap doc : documents) {
            Map<String, Object> docValue = (HashMap<String, Object>) doc.get("value");

            documentList.add(Document.builder().value(
                DocumentDetails.builder()
                    .dateReceived((String) docValue.get("dateReceived"))
                    .evidenceProvidedBy((String) docValue.get("evidenceProvidedBy"))
                    .evidenceType((String) docValue.get("evidenceType"))
                    .build())
                .build());
        }

        return Evidence.builder().documents(documentList).build();
    }
}
