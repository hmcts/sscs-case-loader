package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

@Service
@Slf4j
class UpdateCcdService {

    private final CcdRequestDetails ccdRequestDetails;
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamService idamService;
    private final StartEventCcdService startEventCcdService;

    @Autowired
    UpdateCcdService(CcdRequestDetails ccdRequestDetails, CoreCaseDataApi coreCaseDataApi,
                     IdamService idamService, StartEventCcdService startEventCcdService) {
        this.ccdRequestDetails = ccdRequestDetails;
        this.coreCaseDataApi = coreCaseDataApi;
        this.idamService = idamService;
        this.startEventCcdService = startEventCcdService;
    }

    @Retryable(backoff = @Backoff(delay = 2000L))
    CaseDetails update(SscsCaseData caseData, Long caseId, String eventType, IdamTokens idamTokens) {
        return tryUpdate(caseData, caseId, eventType, idamTokens);
    }

    @Recover
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private CaseDetails requestNewTokensAndTryToUpdateAgain(SscsCaseData caseData, Long caseId, String eventType,
                                                            IdamTokens idamTokens) {
        log.info("*** case-loader *** Requesting new idam and s2s tokens");
        idamTokens.setIdamOauth2Token(idamService.getIdamOauth2Token());
        idamTokens.setServiceAuthorization(idamService.generateServiceAuthorization());
        return tryUpdate(caseData, caseId, eventType, idamTokens);

    }

    private CaseDetails tryUpdate(SscsCaseData caseData, Long caseId, String eventType, IdamTokens idamTokens) {
        StartEventResponse startEventResponse = startEventCcdService
            .startEvent(idamTokens, caseId.toString(), eventType);
        return coreCaseDataApi.submitEventForCaseWorker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(),
            idamTokens.getUserId(),
            ccdRequestDetails.getJurisdictionId(),
            ccdRequestDetails.getCaseTypeId(),
            caseId.toString(),
            true,
            buildCaseDataContent(caseData, startEventResponse));
    }

    private CaseDataContent buildCaseDataContent(SscsCaseData caseData, StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("GAPS2 Case")
                .description("CaseLoader Case updated")
                .build())
            .data(caseData)
            .build();
    }

}
