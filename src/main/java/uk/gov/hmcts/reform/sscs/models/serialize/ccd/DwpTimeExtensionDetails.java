package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DwpTimeExtensionDetails {
    private String requested;
    private String granted;
}
