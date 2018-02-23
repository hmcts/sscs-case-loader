package uk.gov.hmcts.reform.sscs.services.ccd;

import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("startEventAndSaveGivenCase...");
        EventRequestData eventRequestData = getEventRequestData();
        String serviceAuthorization = generateServiceAuthorization();
        log.info("serviceAuthorization: " + serviceAuthorization);
        StartEventResponse startEventResponse = startEvent(eventRequestData, serviceAuthorization);
        return saveCase(eventRequestData, serviceAuthorization, getCaseDataContent(caseData, startEventResponse));
    }

    private CaseDetails saveCase(EventRequestData eventRequestData, String serviceAuthorization,
                                 CaseDataContent caseDataContent) {
        log.info("saveCase...");
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
                .description("Created SSCS")
                .build())
            .data(caseData)
            .build();
    }

    private StartEventResponse startEvent(EventRequestData eventRequestData, String serviceAuthorization) {
        log.info("startEvent...");
        return coreCaseDataApi.startForCaseworker(eventRequestData.getUserToken(), serviceAuthorization,
            eventRequestData.getUserId(), eventRequestData.getJurisdictionId(), eventRequestData.getCaseTypeId(),
            eventRequestData.getEventId());
    }

    private String generateServiceAuthorization() {
        return authTokenGenerator.generate();
    }

    private EventRequestData getEventRequestData() {
        log.info("getEventRequestData...");
        return EventRequestData.builder()
            .userToken(getIdamOauth2Token())
            .userId(coreCaseDataProperties.getUserId())
            .jurisdictionId(coreCaseDataProperties.getJurisdictionId())
            .caseTypeId(coreCaseDataProperties.getCaseTypeId())
            .eventId(coreCaseDataProperties.getEventId())
            .ignoreWarning(true)
            .build();
    }

    private String getIdamOauth2Token() {
        log.info("getIdamOauth2Token...");
        String authorisation = idamProperties.getOauth2().getUser().getEmail()
            + ":" + idamProperties.getOauth2().getUser().getPassword();
        String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());

        Authorize authorize = idamApiClient.authorizeCodeType(
            "Basic " + base64Authorisation,
            "code",
            idamProperties.getOauth2().getClient().getId(),
            idamProperties.getOauth2().getRedirectUrl()
        );

        Authorize authorizeToken = idamApiClient.authorizeToken(
            authorize.getCode(),
            "authorization_code",
            idamProperties.getOauth2().getRedirectUrl(),
            idamProperties.getOauth2().getClient().getId(),
            idamProperties.getOauth2().getClient().getSecret()
        );

        String oauth2Token = "Bearer " + authorizeToken.getAccessToken();
        log.info("oauth2Token: " + oauth2Token);
        return oauth2Token;
    }
}
