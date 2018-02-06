package uk.gov.hmcts.reform.sscs.services.idam;

import org.apache.http.HttpHeaders;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;

@FeignClient(name = "idam-api", url = "${idam.url}")
public interface IdamApiClient {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/oauth2/authorize"
    )
    Authorize authorize(
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/oauth2/authorize"
    )
    String authorizeCodeType(
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation,
        @RequestParam("response_type") final String responseType,
        @RequestParam("client_id") final String clientId,
        @RequestParam("redirect_uri") final String redirectUri
    );

}
