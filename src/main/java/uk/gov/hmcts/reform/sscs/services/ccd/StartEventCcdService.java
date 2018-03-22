package uk.gov.hmcts.reform.sscs.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;

@Service
public class StartEventCcdService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CoreCaseDataProperties coreCaseDataProperties;

    @Autowired
    public StartEventCcdService(CoreCaseDataApi coreCaseDataApi, CoreCaseDataProperties coreCaseDataProperties) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.coreCaseDataProperties = coreCaseDataProperties;
    }

    public StartEventResponse startCase(String serviceAuthorization, String idamOauth2Token, String eventType) {
        return coreCaseDataApi.startForCaseworker(
            idamOauth2Token,
            serviceAuthorization,
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            eventType);
    }

    public StartEventResponse startEvent(String serviceAuthorization, String idamOauth2Token, String caseId,
                                         String eventType) {
        return coreCaseDataApi.startEventForCaseWorker(
            idamOauth2Token,
            serviceAuthorization,
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            caseId,
            eventType);
    }

}
