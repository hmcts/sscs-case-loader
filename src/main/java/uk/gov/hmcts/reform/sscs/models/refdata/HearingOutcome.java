package uk.gov.hmcts.reform.sscs.models.refdata;

import lombok.Data;

@Data
public class HearingOutcome {

    private Integer hearingOutcomeId;
    private String hocCode;
    private String hocDesc;
    private String hocAppealFlg;
    private Integer referenceIdOutcomeType;

}
