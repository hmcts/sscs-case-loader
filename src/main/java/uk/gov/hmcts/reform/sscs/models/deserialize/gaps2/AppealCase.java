package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class AppealCase {
    private String appealCaseRefNum;
    private String appealCaseNino;
    private Parties parties;

    public AppealCase(@JsonProperty("Appeal_Case_RefNum") String appealCaseRefNum,
            @JsonProperty("Appeal_Case_NINO") String appealCaseNino,
            @JsonProperty("Parties") Parties parties) {
        this.appealCaseRefNum = appealCaseRefNum;
        this.appealCaseNino = appealCaseNino;
        this.parties = parties;
    }
}
