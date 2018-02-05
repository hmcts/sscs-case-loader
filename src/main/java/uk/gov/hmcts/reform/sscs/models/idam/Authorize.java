package uk.gov.hmcts.reform.sscs.models.idam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Authorize {
    @JsonProperty(value = "default-url")
    private String defaultUrl;
    @JsonProperty(value = "access-token")
    private String accessToken;
}
