package uk.gov.hmcts.reform.sscs.models.refdata;

import lombok.Data;

@Data
public class Office {

    private Integer officeId;
    private String offRef;
    private String offName;
    private String offAddress1;
    private String offAddress2;
    private String offTown;
    private String offCounty;
    private String offPostcode;
    private String offTelNo;
    private String offFaxNo;
    private String offOpen;
    private String offEmail;
    private Integer officeTypeId;

}
