package uk.gov.hmcts.reform.sscs.models;

import java.io.InputStream;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GapsInputStream {

    private Boolean isDelta;
    private Boolean isReference;
    private InputStream inputStream;
    private String fileName;
}
