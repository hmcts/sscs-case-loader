package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Appeal {
    private MrnDetails mrnDetails;
    private Appellant appellant;
    private BenefitType benefitType;
    private HearingOptions hearingOptions;
    private AppealReasons appealReasons;
    private Representative rep;
    private String signer;

    public Appeal(@JsonProperty("mrnDetails") MrnDetails mrnDetails,
                  @JsonProperty("appellant") Appellant appellant,
                  @JsonProperty("benefitType") BenefitType benefitType,
                  @JsonProperty("hearingOptions") HearingOptions hearingOptions,
                  @JsonProperty("appealReasons") AppealReasons appealReasons,
                  @JsonProperty("rep") Representative rep,
                  @JsonProperty("signer") String signer) {
        this.mrnDetails = mrnDetails;
        this.appellant = appellant;
        this.benefitType = benefitType;
        this.hearingOptions = hearingOptions;
        this.appealReasons = appealReasons;
        this.rep = rep;
        this.signer = signer;
    }
}
