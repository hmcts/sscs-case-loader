package uk.gov.hmcts.reform.sscs.services.ccd;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

@Component
@Slf4j
public class CreateCcdService {

    private final CcdRequestDetails ccdRequestDetails;
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamService idamService;
    private final StartEventCcdService startEventCcdService;
    private final SearchCcdService searchCcdService;

    @Autowired
    CreateCcdService(CcdRequestDetails ccdRequestDetails, CoreCaseDataApi ccd, IdamService idamService,
                     StartEventCcdService startEventCcdService, SearchCcdService searchCcdService) {
        this.ccdRequestDetails = ccdRequestDetails;
        this.coreCaseDataApi = ccd;
        this.idamService = idamService;
        this.startEventCcdService = startEventCcdService;
        this.searchCcdService = searchCcdService;
    }

    @Retryable
    public CaseDetails create(SscsCaseData caseData, IdamTokens idamTokens) {
        log.info("*** case-loader *** Starting create case process with SC number {} and ccdID {} ...",
            caseData.getCaseReference(), caseData.getCcdCaseId());
        return tryCreate(caseData, idamTokens);
    }


    @Recover
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private CaseDetails requestNewTokensAndTryToCreateAgain(SscsCaseData caseData, IdamTokens idamTokens) {
        log.info("*** case-loader *** Recovering method called when creating case with SC number {} and ccdID {}...",
            caseData.getCaseReference(), caseData.getCcdCaseId());
        idamTokens.setIdamOauth2Token(idamService.getIdamOauth2Token());
        idamTokens.setServiceAuthorization(idamService.generateServiceAuthorization());
        return tryCreate(caseData, idamTokens);
    }

    private CaseDetails tryCreate(SscsCaseData caseData, IdamTokens idamTokens) {
        List<CaseDetails> ccdCases = searchCcdService.searchCasesByScNumberAndCcdId(idamTokens, caseData);
        return (ccdCases.isEmpty() ? createCaseInCcd(caseData, idamTokens) : ccdCases.get(0));
    }

    private CaseDetails createCaseInCcd(SscsCaseData caseData, IdamTokens idamTokens) {
        StartEventResponse startEventResponse = startEventCcdService.startCase(idamTokens, "appealCreated");
        return coreCaseDataApi.submitForCaseworker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(),
            idamTokens.getUserId(),
            ccdRequestDetails.getJurisdictionId(),
            ccdRequestDetails.getCaseTypeId(),
            true,
            buildCaseDataContent(caseData, startEventResponse));
    }

    private CaseDataContent buildCaseDataContent(SscsCaseData caseData, StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("GAPS2 Case")
                .description("CaseLoader Case created")
                .build())
            .data(caseData)
            .build();
    }

}
