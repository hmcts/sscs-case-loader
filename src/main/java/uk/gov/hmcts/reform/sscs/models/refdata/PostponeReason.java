package uk.gov.hmcts.reform.sscs.models.refdata;

import lombok.Data;

@Data
public class PostponeReason {

    private Integer postponeReasonId;
    private String pprCode;
    private String pprDesc;

}
