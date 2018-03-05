package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Venue {
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String town;
    private String county;
    private String postcode;
    private String googleMapLink;
}
