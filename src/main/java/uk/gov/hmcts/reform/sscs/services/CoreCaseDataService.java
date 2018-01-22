package uk.gov.hmcts.reform.sscs.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.CcdCase;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CoreCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CoreCaseDataProperties coreCaseDataProperties;

    @Autowired
    public CoreCaseDataService(CoreCaseDataApi coreCaseDataApi, CoreCaseDataProperties coreCaseDataProperties) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.coreCaseDataProperties = coreCaseDataProperties;
    }

    public CaseDetails startEventAndSaveGivenCase(CcdCase ccdCase) {
        EventRequestData eventRequestData = getEventRequestData();
        String serviceAuthorization = getServiceAuthorization();
        StartEventResponse startEventResponse = startEvent(eventRequestData, serviceAuthorization);
        CaseDataContent caseDataContent = getCaseDataContent(ccdCase, startEventResponse);
        return saveCase(eventRequestData, serviceAuthorization, caseDataContent);
    }

    private CaseDetails saveCase(EventRequestData eventRequestData, String serviceAuthorization,
                                 CaseDataContent caseDataContent) {
        return coreCaseDataApi.submitForCaseworker(eventRequestData.getUserToken(), serviceAuthorization,
            eventRequestData.getUserId(), eventRequestData.getJurisdictionId(), eventRequestData.getCaseTypeId(),
            true, caseDataContent);
    }

    private CaseDataContent getCaseDataContent(CcdCase ccdCase, StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("SSCS - appeal created event")
                .description("Created SSCS case with token " + startEventResponse.getToken())
                .build())
            .data(ccdCase)
            .build();
    }

    private StartEventResponse startEvent(EventRequestData eventRequestData, String serviceAuthorization) {
        return coreCaseDataApi.startForCaseworker(eventRequestData.getUserToken(), serviceAuthorization,
            eventRequestData.getUserId(), eventRequestData.getJurisdictionId(), eventRequestData.getCaseTypeId(),
            eventRequestData.getEventId());
    }

    private String getServiceAuthorization() {
        //TODO Investigate how to generate this ??
        return "12";
    }

    private EventRequestData getEventRequestData() {
        //TODO how to get/generate a userToken?
        String userToken = "1";
        //TODO confirm this information with the CCD
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
