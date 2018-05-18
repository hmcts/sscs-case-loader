package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@Service
@Slf4j
public class UpdateCcdService {

    private final CoreCaseDataProperties coreCaseDataProperties;
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamService idamService;
    private final StartEventCcdService startEventCcdService;

    @Autowired
    public UpdateCcdService(CoreCaseDataProperties coreCaseDataProperties, CoreCaseDataApi coreCaseDataApi,
                            IdamService idamService, StartEventCcdService startEventCcdService) {
        this.coreCaseDataProperties = coreCaseDataProperties;
        this.coreCaseDataApi = coreCaseDataApi;
        this.idamService = idamService;
        this.startEventCcdService = startEventCcdService;
    }

    @Retryable
    public CaseDetails update(CaseData caseData, Long caseId, String eventType, IdamTokens idamTokens) {
        StartEventResponse startEventResponse = startEventCcdService.startEvent(
            idamTokens, caseId.toString(), eventType);
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("GAPS2 Case")
                .description("CaseLoader Case updated")
                .build())
            .data(caseData)
            .build();
        return coreCaseDataApi.submitEventForCaseWorker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getAuthenticationService(),
            idamTokens.getServiceUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            caseId.toString(),
            true,
            caseDataContent);
    }

    @Recover
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private CaseDetails requestNewTokensAndTryToUpdateAgain(CaseData caseData, Long caseId, String eventType,
                                                            IdamTokens idamTokens) {
        log.info("*** case-loader *** Requesting new idam and s2s tokens");
        idamTokens.setIdamOauth2Token(idamService.getIdamOauth2Token());
        idamTokens.setAuthenticationService(idamService.generateServiceAuthorization());
        StartEventResponse startEventResponse = startEventCcdService.startEvent(
            idamTokens, caseId.toString(), eventType);
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("GAPS2 Case")
                .description("CaseLoader Case updated")
                .build())
            .data(caseData)
            .build();
        return coreCaseDataApi.submitEventForCaseWorker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getAuthenticationService(),
            idamTokens.getServiceUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            caseId.toString(),
            true,
            caseDataContent);

    }

}
