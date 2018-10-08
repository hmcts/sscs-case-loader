package uk.gov.hmcts.reform.sscs.services.ccd;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

@Service
@Slf4j
public class SearchCcdService {

    private final SearchCcdServiceByCaseRef searchCcdServiceByCaseRef;
    private final SearchCcdServiceByCaseId searchCcdServiceByCaseId;

    SearchCcdService(SearchCcdServiceByCaseRef searchCcdServiceByCaseRef,
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

    public List<CaseDetails> searchCasesByScNumberAndCcdId(IdamTokens idamTokens, SscsCaseData caseData) {
        List<CaseDetails> ccdCases = Collections.emptyList();
        if (StringUtils.isNotBlank(caseData.getCaseReference())) {
            log.info("*** case-loader *** searching cases by SC number {} in CDD", caseData.getCaseReference());
            ccdCases = this.findCaseByCaseRef(caseData.getCaseReference(), idamTokens);
        }

        if (ccdCases.isEmpty() && StringUtils.isNotBlank(caseData.getCcdCaseId())) {
            log.info("*** case-loader *** searching cases by ccdID {} in CDD", caseData.getCcdCaseId());
            ccdCases = this.findCaseByCaseId(caseData.getCcdCaseId(), idamTokens);
        }
        log.info("*** case-loader *** total cases found in CCD: {}", ccdCases.size());
        return ccdCases;
    }

}
