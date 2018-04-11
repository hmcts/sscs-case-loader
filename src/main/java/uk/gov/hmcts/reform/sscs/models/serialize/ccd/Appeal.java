package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder
public class Appeal {
    private Appellant appellant;
    private BenefitType benefitType;
    private HearingOptions hearingOptions;

    public Appeal(@JsonProperty("appellant") Appellant appellant,
                  @JsonProperty("benefitType") BenefitType benefitType,
                  @JsonProperty("hearingOptions") HearingOptions hearingOptions) {
        this.appellant = appellant;
        this.benefitType = benefitType;
        this.hearingOptions = hearingOptions;
    }
}
