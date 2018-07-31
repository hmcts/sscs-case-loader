package uk.gov.hmcts.reform.sscs.controller.test.functional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;

@Controller
public class Functional {

    @Autowired
    private CaseLoaderService caseLoaderService;

    @GetMapping("/functional-test")
    @ResponseBody
    public void functional() {
        caseLoaderService.process();
    }

}
