package uk.gov.hmcts.reform.sscs.models.refdata;

import lombok.Data;

@Data
public class BenAssessType {

    private Integer benAssessTypeId;
    private String batCode;
    private String batDesc;
    private String batRefPrefix;
    private String batRefPrefixUt;

}
