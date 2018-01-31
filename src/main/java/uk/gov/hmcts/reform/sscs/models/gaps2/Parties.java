package uk.gov.hmcts.reform.sscs.models.gaps2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class Parties {
    private String initials;
    private String title;
    private String surname;

    public Parties(@JsonProperty("INITIALS") String initials,
            @JsonProperty("Title") String title,
            @JsonProperty("Surname") String surname) {
        this.initials = initials;
        this.title = title;
        this.surname = surname;
    }
}
