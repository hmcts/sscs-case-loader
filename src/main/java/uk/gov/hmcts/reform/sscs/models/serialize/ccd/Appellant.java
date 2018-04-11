package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder
public class Appellant {
    private Name name;
    private Contact contact;
    private Identity identity;

    @JsonCreator
    public Appellant(@JsonProperty("name") Name name,
                     @JsonProperty("contact") Contact contact,
                     @JsonProperty("identity") Identity identity) {
        this.name = name;
        this.contact = contact;
        this.identity = identity;
    }

}
