package uk.gov.hmcts.reform.sscs.models.idam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class Authorize {
    private String defaultUrl;
    private String accessToken;

    public Authorize(@JsonProperty("default-url") String defaultUrl,
                     @JsonProperty("access-token") String accessToken) {
        this.defaultUrl = defaultUrl;
        this.accessToken = accessToken;
    }
}
