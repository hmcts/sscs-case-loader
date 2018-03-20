package uk.gov.hmcts.reform.sscs.services.ccd;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@Service
@Slf4j
public class SearchCoreCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CoreCaseDataProperties coreCaseDataProperties;
    private final IdamService idamService;

    public SearchCoreCaseDataService(CoreCaseDataApi coreCaseDataApi, CoreCaseDataProperties coreCaseDataProperties,
                                     IdamService idamService) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.coreCaseDataProperties = coreCaseDataProperties;
        this.idamService = idamService;
    }

    @Retryable
    public List<CaseDetails> findCaseByCaseRef(String caseRef, IdamTokens idamTokens) {
        return coreCaseDataApi.searchForCaseworker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getAuthenticationService(),
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            ImmutableMap.of("case.caseReference", caseRef)
        );
    }

    @Recover
    public List<CaseDetails> requestNewTokensAndTryToFindCaseAgain(String caseRef,
                                                                   IdamTokens idamTokens) {
        idamTokens.setIdamOauth2Token(idamService.getIdamOauth2Token());
        idamTokens.setAuthenticationService(idamService.generateServiceAuthorization());
        return coreCaseDataApi.searchForCaseworker(
            idamTokens.getIdamOauth2Token(),
            idamTokens.getAuthenticationService(),
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            ImmutableMap.of("case.caseReference", caseRef));
    }

}
