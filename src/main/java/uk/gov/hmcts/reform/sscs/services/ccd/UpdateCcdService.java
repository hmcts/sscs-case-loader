package uk.gov.hmcts.reform.sscs.services.ccd;

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
public class UpdateCcdService {

    private final CoreCaseDataProperties coreCaseDataProperties;
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamService idamService;

    @Autowired
    public UpdateCcdService(CoreCaseDataProperties coreCaseDataProperties, CoreCaseDataApi coreCaseDataApi,
                            IdamService idamService) {
        this.coreCaseDataProperties = coreCaseDataProperties;
        this.coreCaseDataApi = coreCaseDataApi;
        this.idamService = idamService;
    }

    @Retryable
    public CaseDetails update(CaseData caseData, Long caseId, String eventType, IdamTokens idamTokens) {
        System.out.println("*** update");
        StartEventResponse startEventResponse = startEvent(idamTokens.getAuthenticationService(),
            idamTokens.getIdamOauth2Token(), eventType);
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
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            caseId.toString(),
            true,
            caseDataContent);
    }

    @Recover
    public CaseDetails requestNewTokensAndTryToUpdateAgain(CaseData caseData, Long caseId, String eventType,
                                                           IdamTokens idamTokens) {
        System.out.println("*** recover");
        idamTokens.setIdamOauth2Token(idamService.getIdamOauth2Token());
        idamTokens.setAuthenticationService(idamService.generateServiceAuthorization());
        StartEventResponse startEventResponse = startEvent(idamTokens.getAuthenticationService(),
            idamTokens.getIdamOauth2Token(), eventType);
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
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            caseId.toString(),
            true,
            caseDataContent);

    }

    private StartEventResponse startEvent(String serviceAuthorization, String idamOauth2Token, String eventType) {
        System.out.println("*** start");
        return coreCaseDataApi.startForCaseworker(
            idamOauth2Token,
            serviceAuthorization,
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            eventType);
    }
}
