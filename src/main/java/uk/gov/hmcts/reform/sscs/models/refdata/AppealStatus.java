package uk.gov.hmcts.reform.sscs.models.refdata;

import lombok.Data;

@Data
public class AppealStatus {

    private Integer appealStatusId;
    private Integer apsMinor;
    private Integer apsBfDays;
    private String apsDesc;
    private String apsDormant;
    private String apsSearch;

}

