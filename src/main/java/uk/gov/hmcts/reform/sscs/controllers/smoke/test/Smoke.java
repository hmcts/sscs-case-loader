package uk.gov.hmcts.reform.sscs.controllers.smoke.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Smoke {

    @GetMapping("/smoke-test")
    @ResponseBody
    public String smoke() {
        return "Hi Satya, how are you today?";
    }


}
