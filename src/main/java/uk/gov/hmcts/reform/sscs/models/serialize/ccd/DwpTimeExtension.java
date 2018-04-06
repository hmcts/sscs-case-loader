package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder
public class DwpTimeExtension {
    private DwpTimeExtensionDetails value;

    @JsonCreator
    public DwpTimeExtension(@JsonProperty("value") DwpTimeExtensionDetails value) {
        this.value = value;
    }
}
