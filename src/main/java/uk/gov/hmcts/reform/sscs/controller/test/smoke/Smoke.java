package uk.gov.hmcts.reform.sscs.controller.test.smoke;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCcdService;

@Controller
public class Smoke {

    @Autowired
    private SearchCcdService searchCcdService;
    @Autowired
    private IdamService idamService;

    @GetMapping("/smoke-test")
    @ResponseBody
    public List<CaseDetails> smoke() {
        String oauth2Token = idamService.getIdamOauth2Token();
        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token(oauth2Token)
            .serviceAuthorization(idamService.generateServiceAuthorization())
            .userId(idamService.getUserId(oauth2Token))
            .build();
        return searchCcdService.findCaseByCaseRef("SC068/18/01217", idamTokens);
    }

}
