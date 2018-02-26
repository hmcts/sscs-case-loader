package uk.gov.hmcts.reform.sscs.models.refdata;

import lombok.Data;

@Data
public class PttpRole {

    private Integer pttpRoleId;
    private String ptrCode;
    private String ptrDesc;
    private Integer pttpRoleIdAlt;

}
