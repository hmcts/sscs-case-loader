package uk.gov.hmcts.reform.sscs.services.ccd;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;

@Service
@Slf4j
public class SearchCcdService {

    private final SearchCcdServiceByCaseRef searchCcdServiceByCaseRef;
    private final SearchCcdServiceByCaseId searchCcdServiceByCaseId;

    public SearchCcdService(SearchCcdServiceByCaseRef searchCcdServiceByCaseRef,
                            SearchCcdServiceByCaseId searchCcdServiceByCaseId) {
        this.searchCcdServiceByCaseRef = searchCcdServiceByCaseRef;
        this.searchCcdServiceByCaseId = searchCcdServiceByCaseId;
    }

    public List<CaseDetails> findCaseByCaseRef(String caseRef, IdamTokens idamTokens) {
        return searchCcdServiceByCaseRef.findCaseByCaseRef(caseRef, idamTokens);
    }

    public List<CaseDetails> findCaseByCaseId(String caseId, IdamTokens idamTokens) {
        return searchCcdServiceByCaseId.findCaseByCaseId(caseId, idamTokens);
    }

}
