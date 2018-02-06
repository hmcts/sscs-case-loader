package uk.gov.hmcts.reform.sscs.services.ccd;

import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.config.properties.IdamProperties;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;

@Service
public class CoreCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CoreCaseDataProperties coreCaseDataProperties;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamApiClient idamApiClient;
    private final IdamProperties idamProperties;

    @Autowired
    public CoreCaseDataService(CoreCaseDataApi coreCaseDataApi, CoreCaseDataProperties coreCaseDataProperties,
                               AuthTokenGenerator authTokenGenerator, IdamApiClient idamApiClient,
                               IdamProperties idamProperties) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.coreCaseDataProperties = coreCaseDataProperties;
        this.authTokenGenerator = authTokenGenerator;
        this.idamApiClient = idamApiClient;
        this.idamProperties = idamProperties;
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
        return EventRequestData.builder()
            .userToken(getIdamUserToken())
            .userId(coreCaseDataProperties.getUserId())
            .jurisdictionId(coreCaseDataProperties.getJurisdictionId())
            .caseTypeId(coreCaseDataProperties.getCaseTypeId())
            .eventId(coreCaseDataProperties.getEventId())
            .ignoreWarning(true)
            .build();
    }

    private String getIdamUserToken() {
        String authorisation = idamProperties.getRole().getEmail() + ":" + idamProperties.getRole().getPassword();
        String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());
        Authorize authorize = idamApiClient.authorize("Basic " + base64Authorisation);

        // FIXME: 06/02/2018 Fix issue when using IDAM OAuth2

        //        Authorize authorize = idamApiClient.authorizeCodeType(
        //            "Basic " + base64Authorisation,
        //            "code",
        //            "sscs",
        //            "http://localhost"
        //        );
        //
        //        String authorizeToken = idamApiClient.authorizeToken(
        //            authorize.getCode(),
        //            "authorization_code",
        //            "http://localhost",
        //            "sscs",
        //            "AAAAAAAAAAAAAAAC"
        //        );
        //
        //        System.out.println(authorizeToken);

        return "Bearer " + authorize.getAccessToken();
    }
}
