package uk.gov.hmcts.reform.sscs.models;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class XmlFiles {
    private String delta;
    private String ref;
}
