package uk.gov.hmcts.reform.sscs.controller.test.functional;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCcdService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@Controller
public class Functional {

    @Autowired
    private CaseLoaderService caseLoaderService;
    @Autowired
    private IdamService idamService;
    @Autowired
    private SearchCcdService searchCcdService;

    @GetMapping("/functional-test")
    @ResponseBody
    public void functional() {
        caseLoaderService.process();
    }

    @GetMapping("/functional-test/{referenceNumber}")
    @ResponseBody
    public List<CaseDetails> getCase(@PathVariable String referenceNumber) {
        String oauth2Token = idamService.getIdamOauth2Token();
        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token(oauth2Token)
            .serviceAuthorization(idamService.generateServiceAuthorization())
            .userId(idamService.getUserId(oauth2Token))
            .build();
        return searchCcdService.findCaseByCaseRef(referenceNumber, idamTokens);
    }

}
