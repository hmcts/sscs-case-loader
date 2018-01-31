package uk.gov.hmcts.reform.sscs.models.gaps2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class Gaps2Extract {
    private AppealCases appealCases;

    @JsonCreator
    public Gaps2Extract(@JsonProperty("Appeal_Cases") AppealCases appealCases) {
        this.appealCases = appealCases;
    }
}