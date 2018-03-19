package uk.gov.hmcts.reform.sscs.services.ccd;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;

@Service
public class SearchCoreCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final CoreCaseDataProperties coreCaseDataProperties;

    public SearchCoreCaseDataService(CoreCaseDataApi coreCaseDataApi, CoreCaseDataProperties coreCaseDataProperties) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.coreCaseDataProperties = coreCaseDataProperties;
    }

    @Retryable(backoff = @Backoff(delay = 2000))
    public List<CaseDetails> findCaseByCaseRef(String caseRef, String idamOauth2Token, String serviceAuthorization) {
        return coreCaseDataApi.searchForCaseworker(
            idamOauth2Token,
            serviceAuthorization,
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            ImmutableMap.of("case.caseReference", caseRef)
        );
    }
}
