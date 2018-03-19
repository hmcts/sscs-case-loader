package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final CoreCaseDataProperties coreCaseDataProperties;
    private final IdamService idamService;
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    public CcdApiWrapper(CoreCaseDataProperties properties, IdamService idam, CoreCaseDataApi ccd) {
        this.coreCaseDataProperties = properties;
        this.idamService = idam;
        this.coreCaseDataApi = ccd;
    }

    public CaseDetails create(String eventType, CaseData caseData) {

        String serviceAuthorization = idamService.generateServiceAuthorization();
        String idamOauth2Token = idamService.getIdamOauth2Token();

        StartEventResponse startEventResponse = startEvent(serviceAuthorization, idamOauth2Token, eventType);

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

    public CaseDetails update(CaseData caseData, Long caseId, String eventType) {

        String serviceAuthorization = idamService.generateServiceAuthorization();
        String idamOauth2Token = idamService.getIdamOauth2Token();

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
