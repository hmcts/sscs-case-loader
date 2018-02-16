package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Appellant {

    private Name name;
    private Address address;
    private Contact contact;
    private Identity identity;

}
