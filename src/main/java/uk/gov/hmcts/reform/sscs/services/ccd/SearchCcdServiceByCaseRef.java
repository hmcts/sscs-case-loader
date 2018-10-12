package uk.gov.hmcts.reform.sscs.services.ccd;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
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
public class SearchCcdServiceByCaseRef {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CcdRequestDetails ccdRequestDetails;
    private final IdamService idamService;

    public SearchCcdServiceByCaseRef(CoreCaseDataApi coreCaseDataApi, CcdRequestDetails ccdRequestDetails,
                                     IdamService idamService) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.ccdRequestDetails = ccdRequestDetails;
        this.idamService = idamService;
    }

    @Retryable(backoff = @Backoff(delay = 2000L))
    List<CaseDetails> findCaseByCaseRef(String caseRef, IdamTokens idamTokens) {
        return tryFindCaseByCaseRef(caseRef, idamTokens);
    }

    @Recover
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private List<CaseDetails> requestNewTokensAndTryToFindCaseAgain(String caseRef, IdamTokens idamTokens) {
        log.info("*** case-loader *** Requesting new idam and s2s tokens");
        idamTokens.setIdamOauth2Token(idamService.getIdamOauth2Token());
        idamTokens.setServiceAuthorization(idamService.generateServiceAuthorization());
        return tryFindCaseByCaseRef(caseRef, idamTokens);
    }

    private List<CaseDetails> tryFindCaseByCaseRef(String caseRef, IdamTokens idamTokens) {
        return coreCaseDataApi.searchForCaseworker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(),
            idamTokens.getUserId(),
            ccdRequestDetails.getJurisdictionId(),
            ccdRequestDetails.getCaseTypeId(),
            ImmutableMap.of("case.caseReference", caseRef)
        );
    }

}
