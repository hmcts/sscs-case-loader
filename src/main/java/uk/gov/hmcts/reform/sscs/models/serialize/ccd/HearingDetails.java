package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HearingDetails {
    private Venue venue;
    private String hearingDate;
}
