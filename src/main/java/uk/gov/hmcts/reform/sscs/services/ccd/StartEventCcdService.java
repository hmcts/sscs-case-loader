package uk.gov.hmcts.reform.sscs.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;

@Service
public class StartEventCcdService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CoreCaseDataProperties coreCaseDataProperties;

    @Autowired
    public StartEventCcdService(CoreCaseDataApi coreCaseDataApi, CoreCaseDataProperties coreCaseDataProperties) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.coreCaseDataProperties = coreCaseDataProperties;
    }

    public StartEventResponse startCase(IdamTokens idamTokens, String eventType) {
        return coreCaseDataApi.startForCaseworker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getAuthenticationService(),
            idamTokens.getServiceUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            eventType);
    }

    public StartEventResponse startEvent(IdamTokens idamTokens, String caseId, String eventType) {
        return coreCaseDataApi.startEventForCaseWorker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getAuthenticationService(),
            idamTokens.getServiceUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            caseId,
            eventType);
    }

}
