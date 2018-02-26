package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@Service
@Slf4j
public class CoreCaseDataServiceUtil {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CoreCaseDataProperties coreCaseDataProperties;
    private final IdamService idamService;


    @Autowired
    public CoreCaseDataServiceUtil(CoreCaseDataApi coreCaseDataApi,
                                   CoreCaseDataProperties coreCaseDataProperties,
                                   IdamService idamService) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.coreCaseDataProperties = coreCaseDataProperties;
        this.idamService = idamService;
    }

    protected EventRequestData getEventRequestData(String eventId) {
        log.info("getEventRequestData...");
        return EventRequestData.builder()
            .userToken(idamService.getIdamOauth2Token())
            .userId(coreCaseDataProperties.getUserId())
            .jurisdictionId(coreCaseDataProperties.getJurisdictionId())
            .caseTypeId(coreCaseDataProperties.getCaseTypeId())
            .eventId(eventId)
            .ignoreWarning(true)
            .build();
    }

    protected CoreCaseDataApi getCoreCaseDataApi() {
        return coreCaseDataApi;
    }

    protected CaseDataContent getCaseDataContent(CaseData caseData, StartEventResponse startEventResponse,
                                                 String summary, String description) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary(summary)
                .description(description)
                .build())
            .data(caseData)
            .build();
    }

    public String generateServiceAuthorization() {
        return idamService.generateServiceAuthorization();
    }
}
