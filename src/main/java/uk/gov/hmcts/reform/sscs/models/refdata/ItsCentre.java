package uk.gov.hmcts.reform.sscs.models.refdata;

import lombok.Data;

@Data
public class ItsCentre {

    private Integer itsCentreId;
    private String itsCode;
    private String itsName;
    private String itsAddr1;
    private String itsAddr2;
    private String itsTown;
    private String itsCounty;
    private String itsPostcde;
    private String itsTelNo;
    private String itsFaxNo;
    private String itsEmail;
    private String itsService;
    private String itsCdbInclude;
    private Integer tribunalId;

}
