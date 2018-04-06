package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder
public class DwpTimeExtensionDetails {
    private String requested;
    private String granted;


    @JsonCreator
    public DwpTimeExtensionDetails(@JsonProperty("requested") String requested,
                                   @JsonProperty("granted") String granted) {
        this.requested = requested;
        this.granted = granted;
    }
}
