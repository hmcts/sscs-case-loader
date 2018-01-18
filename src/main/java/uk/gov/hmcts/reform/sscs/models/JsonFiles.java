package uk.gov.hmcts.reform.sscs.models;

import lombok.Builder;
import lombok.Value;
import org.json.JSONObject;

@Value
@Builder
public class JsonFiles {
    private JSONObject delta;
    private JSONObject ref;
}
