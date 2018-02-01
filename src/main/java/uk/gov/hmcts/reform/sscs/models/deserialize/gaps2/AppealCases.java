package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Value;

@Value
public class AppealCases {
    private List<AppealCase> appealCaseList;

    public AppealCases(@JsonProperty("Appeal_Case") List<AppealCase> appealCaseList) {
        this.appealCaseList = appealCaseList;
    }
}
