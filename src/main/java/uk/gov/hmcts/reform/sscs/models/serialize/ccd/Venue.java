package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class Venue {
    private String name;
    private Address address;
    private String googleMapLink;

    @JsonCreator
    public Venue(@JsonProperty("name") String name,
                 @JsonProperty("address") Address address,
                 @JsonProperty("googleMapLink") String googleMapLink) {
        this.name = name;
        this.address = address;
        this.googleMapLink = googleMapLink;
    }
}
