package uk.gov.hmcts.reform.sscs.models.refdata;

import lombok.Data;

@Data
public class BusinessRulesGroup {

    private Integer businessRulesGrpId;
    private String brgShortDesc;
    private String brgLongDesc;
    private Integer tribunalId;
    private Integer jurisdictionId;

}
