package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Contact {
    private String email;
    private String phone;
    private String mobile;
}
