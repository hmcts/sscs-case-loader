package uk.gov.hmcts.reform.sscs.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

@Service
public class StartEventCcdService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CcdRequestDetails ccdRequestDetails;

    @Autowired
    public StartEventCcdService(CoreCaseDataApi coreCaseDataApi, CcdRequestDetails ccdRequestDetails) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.ccdRequestDetails = ccdRequestDetails;
    }

    public StartEventResponse startCase(IdamTokens idamTokens, String eventType) {
        return coreCaseDataApi.startForCaseworker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(),
            idamTokens.getUserId(),
            ccdRequestDetails.getJurisdictionId(),
            ccdRequestDetails.getCaseTypeId(),
            eventType);
    }

    public StartEventResponse startEvent(IdamTokens idamTokens, String caseId, String eventType) {
        return coreCaseDataApi.startEventForCaseWorker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(),
            idamTokens.getUserId(),
            ccdRequestDetails.getJurisdictionId(),
            ccdRequestDetails.getCaseTypeId(),
            caseId,
            eventType);
    }

}
