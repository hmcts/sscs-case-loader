package uk.gov.hmcts.reform.sscs.controllers.smoke.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCoreCaseDataService;

import java.util.List;

@Controller
public class Smoke {

    @GetMapping("/smoke-test")
    @ResponseBody
    public List smoke() {
        return getCase("SC068/18/01217");
    }

    @Autowired
    private SearchCoreCaseDataService searchCoreCaseDataService;


    public List getCase(String caseid) {
        return searchCoreCaseDataService.findCaseByCaseRef(caseid);
    }


}
