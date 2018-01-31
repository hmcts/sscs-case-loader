package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Name {

    private String title;
    private String firstName;
    private String lastName;

}
