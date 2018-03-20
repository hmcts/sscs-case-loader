package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@Component
@Slf4j
public class CcdApiWrapper {

    private static final String APPEAL_CREATED = "appealCreated";

    private final CoreCaseDataProperties coreCaseDataProperties;
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamService idamService;

    @Autowired
    CcdApiWrapper(CoreCaseDataProperties properties, CoreCaseDataApi ccd, IdamService idamService) {
        this.coreCaseDataProperties = properties;
        this.coreCaseDataApi = ccd;
        this.idamService = idamService;
    }

    @Retryable
    public CaseDetails create(CaseData caseData, String idamOauth2Token, String serviceAuthorization) {
        StartEventResponse startEventResponse = startEvent(serviceAuthorization, idamOauth2Token, APPEAL_CREATED);
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("GAPS2 Case")
                .description("CaseLoader Case created")
                .build())
            .data(caseData)
            .build();
        return coreCaseDataApi.submitForCaseworker(
            idamOauth2Token,
            serviceAuthorization,
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            true,
            caseDataContent);
    }

    @Retryable
    public CaseDetails update(CaseData caseData, Long caseId, String eventType, String idamOauth2Token,
                              String serviceAuthorization) {
        StartEventResponse startEventResponse = startEvent(serviceAuthorization, idamOauth2Token, eventType);
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
            idamOauth2Token,
            serviceAuthorization,
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            caseId.toString(),
            true,
            caseDataContent);
    }

    @Recover
    public CaseDetails updateRecoveryMethodIfException(CaseData caseData, Long caseId, String eventType,
                                                       String idamOauth2Token, String serviceAuthorization) {
        StartEventResponse startEventResponse = startEvent(serviceAuthorization, idamOauth2Token, eventType);
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("GAPS2 Case")
                .description("CaseLoader Case updated")
                .build())
            .data(caseData)
            .build();
        String authorisation = idamService.generateServiceAuthorization();
        String idamOauth2Token1 = idamService.getIdamOauth2Token();
        return coreCaseDataApi.submitEventForCaseWorker(
            idamOauth2Token1,
            authorisation,
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            caseId.toString(),
            true,
            caseDataContent);

    }

    private StartEventResponse startEvent(String serviceAuthorization, String idamOauth2Token, String eventType) {

        return coreCaseDataApi.startForCaseworker(
            idamOauth2Token,
            serviceAuthorization,
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            eventType);
    }
}
