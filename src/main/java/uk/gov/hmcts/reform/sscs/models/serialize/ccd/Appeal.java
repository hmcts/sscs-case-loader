package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Appeal {
    private Appellant appellant;
    private BenefitType benefitType;
    private HearingOptions hearingOptions;
}
