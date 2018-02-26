package uk.gov.hmcts.reform.sscs.models.refdata;

import lombok.Data;

@Data
public class SeparateCorrespondence {

    private Integer separateCorrespondenceId;
    private String scCode;
    private String scDesc;
    private String scUserSelectableFlag;

}
