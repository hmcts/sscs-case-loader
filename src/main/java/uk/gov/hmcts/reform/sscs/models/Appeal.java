package uk.gov.hmcts.reform.sscs.models;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Appeal {
    private String mrnDate;
    private String mrnMissingReason;
    private Appellant appellant;
}
