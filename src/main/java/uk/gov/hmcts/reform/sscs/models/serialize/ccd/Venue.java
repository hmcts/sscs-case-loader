package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Venue {
    private String name;
    private Address address;
    private String googleMapLink;
}
