package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Representative {

    private String hasRepresentative;
    private String organisation;
    private Name name;
    private Address address;
    private Contact contact;

    @JsonCreator
    public Representative(@JsonProperty("hasRepresentative") String hasRepresentative,
                          @JsonProperty("organisation") String organisation,
                          @JsonProperty("name") Name name,
                          @JsonProperty("address") Address address,
                          @JsonProperty("contact") Contact contact) {
        this.hasRepresentative = hasRepresentative;
        this.organisation = organisation;
        this.name = name;
        this.address = address;
        this.contact = contact;
    }
}
