package uk.gov.hmcts.reform.sscs.services.ccd;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

@Component
@Slf4j
public class SearchCcdServiceByCaseId {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CcdRequestDetails ccdRequestDetails;
    private final IdamService idamService;

    public SearchCcdServiceByCaseId(CoreCaseDataApi coreCaseDataApi, CcdRequestDetails ccdRequestDetails,
                                    IdamService idamService) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.ccdRequestDetails = ccdRequestDetails;
        this.idamService = idamService;
    }

    @Retryable
    public List<CaseDetails> findCaseByCaseId(String caseId, IdamTokens idamTokens) {
        return tryFindCaseByCaseId(caseId, idamTokens);
    }

    @Recover
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private List<CaseDetails> requestNewTokensAndTryToFindCaseAgain(String caseId, IdamTokens idamTokens) {
        log.info("*** case-loader *** Requesting new idam and s2s tokens");
        idamTokens.setIdamOauth2Token(idamService.getIdamOauth2Token());
        idamTokens.setServiceAuthorization(idamService.generateServiceAuthorization());
        return tryFindCaseByCaseId(caseId, idamTokens);
    }

    private List<CaseDetails> tryFindCaseByCaseId(String caseId, IdamTokens idamTokens) {

        CaseDetails caseDetails = coreCaseDataApi.readForCaseWorker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(),
            idamTokens.getUserId(),
            ccdRequestDetails.getJurisdictionId(),
            ccdRequestDetails.getCaseTypeId(),
            caseId
        );

        if (caseDetails != null) {
            return Collections.singletonList(caseDetails);
        }

        return Collections.emptyList();
    }

}
