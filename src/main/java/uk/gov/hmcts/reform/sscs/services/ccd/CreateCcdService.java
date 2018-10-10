package uk.gov.hmcts.reform.sscs.services.ccd;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
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
    private final RetryTemplate retryTemplate;

    @Autowired
    CreateCcdService(CcdRequestDetails ccdRequestDetails, CoreCaseDataApi ccd, IdamService idamService,
                     StartEventCcdService startEventCcdService, SearchCcdService searchCcdService,
                     RetryTemplate retryTemplate) {
        this.ccdRequestDetails = ccdRequestDetails;
        this.coreCaseDataApi = ccd;
        this.idamService = idamService;
        this.startEventCcdService = startEventCcdService;
        this.searchCcdService = searchCcdService;
        this.retryTemplate = retryTemplate;
    }

    public CaseDetails create(SscsCaseData caseData, IdamTokens idamTokens) {
        log.info("*** case-loader *** Starting create case process with SC number {} and ccdID {} ...",
            caseData.getCaseReference(), caseData.getCcdCaseId());
        try {
            retryTemplate.execute(getRetryCallback(caseData, idamTokens), getRecoveryCallback(caseData, idamTokens));
        } catch (Throwable throwable) {
            log.error("Recovery mechanism failed when creating case with SC {} and ccdID {}...",
                caseData.getCaseReference(), caseData.getCcdCaseId());
        }
        return null;
    }

    private RecoveryCallback<CaseDetails> getRecoveryCallback(SscsCaseData caseData, IdamTokens idamTokens) {
        return context -> {
            log.info("*** case-loader *** Recovery method called when creating case with SC number {} and ccdID {}...",
                caseData.getCaseReference(), caseData.getCcdCaseId());
            idamTokens.setIdamOauth2Token(idamService.getIdamOauth2Token());
            idamTokens.setServiceAuthorization(idamService.generateServiceAuthorization());
            List<CaseDetails> ccdCases = searchCcdService.searchCasesByScNumberAndCcdId(idamTokens, caseData);
            if (ccdCases.isEmpty()) {
                return createCaseInCcd(caseData, idamTokens);
            }
            return ccdCases.get(0);
        };
    }

    private RetryCallback<CaseDetails, ? extends Throwable> getRetryCallback(SscsCaseData caseData,
                                                                             IdamTokens idamTokens) {
        return (RetryCallback<CaseDetails, Throwable>) context -> {
            log.info("*** case-loader *** create case with SC number {} and ccdID {} and retry number {}",
                caseData.getCaseReference(), caseData.getCcdCaseId(), context.getRetryCount());
            List<CaseDetails> ccdCases = Collections.emptyList();
            if (context.getRetryCount() > 0) {
                ccdCases = searchCcdService.searchCasesByScNumberAndCcdId(idamTokens, caseData);
            }
            if (ccdCases.isEmpty()) {
                return createCaseInCcd(caseData, idamTokens);
            }
            return ccdCases.get(0);
        };
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
