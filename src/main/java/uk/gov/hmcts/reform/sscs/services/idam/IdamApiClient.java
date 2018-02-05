package uk.gov.hmcts.reform.sscs.services.idam;

import org.apache.http.HttpHeaders;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "idam-api", url = "${idam.url}")
public interface IdamApiClient {

    @RequestMapping(method = RequestMethod.POST, value = "/oauth2/authorize")
    String authorize(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);

}
