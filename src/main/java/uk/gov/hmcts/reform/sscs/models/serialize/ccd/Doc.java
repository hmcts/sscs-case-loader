package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Doc {
    private String dateReceived;
    private String evidenceType;
    private String evidenceProvidedBy;
}
