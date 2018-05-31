package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MrnDetails {
    private String dwpIssuingOffice;
    private String mrnDate;
    private String mrnLateReason;
    private String mrnMissingReason;

    @JsonCreator
    public MrnDetails(@JsonProperty("dwpIssuingOffice") String dwpIssuingOffice,
                      @JsonProperty("mrnDate") String mrnDate,
                      @JsonProperty("mrnLateReason") String mrnLateReason,
                      @JsonProperty("mrnMissingReason") String mrnMissingReason) {
        this.dwpIssuingOffice = dwpIssuingOffice;
        this.mrnDate = mrnDate;
        this.mrnLateReason = mrnLateReason;
        this.mrnMissingReason = mrnMissingReason;
    }
}
