package uk.gov.hmcts.reform.sscs.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.*;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;

@Service
public class CoreCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CoreCaseDataProperties coreCaseDataProperties;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamApiClient idamApiClient;

    @Autowired
    public CoreCaseDataService(CoreCaseDataApi coreCaseDataApi, CoreCaseDataProperties coreCaseDataProperties,
                               AuthTokenGenerator authTokenGenerator, IdamApiClient idamApiClient) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.coreCaseDataProperties = coreCaseDataProperties;
        this.authTokenGenerator = authTokenGenerator;
        this.idamApiClient = idamApiClient;
    }

    public CaseDetails startEventAndSaveGivenCase(CaseData caseData) {
        EventRequestData eventRequestData = getEventRequestData();
        String serviceAuthorization = generateServiceAuthorization();
        StartEventResponse startEventResponse = startEvent(eventRequestData, serviceAuthorization);
        return saveCase(eventRequestData, serviceAuthorization, getCaseDataContent(caseData, startEventResponse));
    }

    private CaseDetails saveCase(EventRequestData eventRequestData, String serviceAuthorization,
                                 CaseDataContent caseDataContent) {
        return coreCaseDataApi.submitForCaseworker(eventRequestData.getUserToken(), serviceAuthorization,
            eventRequestData.getUserId(), eventRequestData.getJurisdictionId(), eventRequestData.getCaseTypeId(),
            true, caseDataContent);
    }

    private CaseDataContent getCaseDataContent(CaseData caseData, StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("SSCS - appeal created event")
                .description("Created SSCS case with token " + startEventResponse.getToken())
                .build())
            .data(caseData)
            .build();
    }

    private StartEventResponse startEvent(EventRequestData eventRequestData, String serviceAuthorization) {
        return coreCaseDataApi.startForCaseworker(eventRequestData.getUserToken(), serviceAuthorization,
            eventRequestData.getUserId(), eventRequestData.getJurisdictionId(), eventRequestData.getCaseTypeId(),
            eventRequestData.getEventId());
    }

    private String generateServiceAuthorization() {
        return authTokenGenerator.generate();
    }

    private EventRequestData getEventRequestData() {
        String authorisation = "emal:password";
        String userToken = idamApiClient.authorize(authorisation);
        return EventRequestData.builder()
            .userToken(userToken)
            .userId(coreCaseDataProperties.getUserId())
            .jurisdictionId(coreCaseDataProperties.getJurisdictionId())
            .caseTypeId(coreCaseDataProperties.getCaseTypeId())
            .eventId(coreCaseDataProperties.getEventId())
            .ignoreWarning(true)
            .build();
    }
}
