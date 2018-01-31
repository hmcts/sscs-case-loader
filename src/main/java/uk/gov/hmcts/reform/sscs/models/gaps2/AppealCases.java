package uk.gov.hmcts.reform.sscs.models.gaps2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class AppealCases {
    private List<AppealCase> appealCaseList;

    public AppealCases(@JsonProperty("Appeal_Case") List<AppealCase> appealCaseList) {
        this.appealCaseList = appealCaseList;
    }
}