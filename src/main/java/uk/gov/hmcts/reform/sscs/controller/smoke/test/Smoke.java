package uk.gov.hmcts.reform.sscs.controller.smoke.test;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@Controller
public class Smoke {

    @Autowired
    private SearchCoreCaseDataService searchCoreCaseDataService;
    @Autowired
    private IdamService idamService;

    @GetMapping("/smoke-test")
    @ResponseBody
    public List<CaseDetails> smoke() {
        String idamOauth2Token = idamService.getIdamOauth2Token();
        return searchCoreCaseDataService.findCaseByCaseRef("SC068/18/01217", idamOauth2Token);
    }

}
