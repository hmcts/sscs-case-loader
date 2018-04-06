package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class Hearing {
    private HearingDetails value;

    @JsonCreator
    public Hearing(@JsonProperty("value") HearingDetails value) {
        this.value = value;
    }
}
