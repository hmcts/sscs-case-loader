package uk.gov.hmcts.reform.sscs.controller.test.smoke;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCcdService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@Controller
public class Smoke {

    @Autowired
    private SearchCcdService searchCcdService;
    @Autowired
    private IdamService idamService;

    @GetMapping("/smoke-test")
    @ResponseBody
    public List<CaseDetails> smoke() {
        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token(idamService.getIdamOauth2Token())
            .serviceAuthorisation(idamService.generateServiceAuthorization())
            .build();
        return searchCcdService.findCaseByCaseRef("SC068/18/01217", idamTokens);
    }

}
