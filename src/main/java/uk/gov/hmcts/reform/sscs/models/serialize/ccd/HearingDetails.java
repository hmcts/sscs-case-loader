package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class HearingDetails {
    private Venue venue;
    private String hearingDate;
    private String time;
    private String adjourned;

    @JsonCreator
    public HearingDetails(@JsonProperty("venue") Venue venue,
                          @JsonProperty("hearingDate") String hearingDate,
                          @JsonProperty("time") String time,
                          @JsonProperty("adjourned") String adjourned) {

        this.venue = venue;
        this.hearingDate = hearingDate;
        this.time = time;
        this.adjourned = adjourned;
    }

    @JsonIgnore
    public String getHearingDateTime() {
        return hearingDate + time;
    }
}
