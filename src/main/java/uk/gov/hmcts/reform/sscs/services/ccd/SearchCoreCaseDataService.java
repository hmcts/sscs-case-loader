package uk.gov.hmcts.reform.sscs.services.ccd;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@Service
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


    public List<CaseDetails> findCaseByCaseRef(String caseRef) {
        return coreCaseDataApi.searchForCaseworker(
            idamService.getIdamOauth2Token(),
            idamService.generateServiceAuthorization(),
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            ImmutableMap.of("case.caseReference", caseRef)
        );
    }
}
