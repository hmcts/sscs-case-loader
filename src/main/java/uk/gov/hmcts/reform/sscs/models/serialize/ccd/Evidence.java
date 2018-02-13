package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Evidence {
    private List<Documents> documents;
}
