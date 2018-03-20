package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;

@Component
@Slf4j
public class CreateCcdService {

    private final CoreCaseDataProperties coreCaseDataProperties;
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    CreateCcdService(CoreCaseDataProperties properties, CoreCaseDataApi ccd) {
        this.coreCaseDataProperties = properties;
        this.coreCaseDataApi = ccd;
    }

    @Retryable
    public CaseDetails create(CaseData caseData, IdamTokens idamTokens) {
        StartEventResponse startEventResponse = startEvent(idamTokens.getAuthenticationService(),
            idamTokens.getIdamOauth2Token(), "appealCreated");
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
            idamTokens.getIdamOauth2Token(),
            idamTokens.getAuthenticationService(),
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
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
