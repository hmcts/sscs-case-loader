package uk.gov.hmcts.reform.sscs.models.refdata;

import lombok.Data;

@Data
public class District {

    private Integer districtId;
    private String dstDesc;
    private Integer itsCentreId;
    private String dstDescBp;
    private Integer districtIdBp;

}
