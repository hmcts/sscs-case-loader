package uk.gov.hmcts.reform.sscs.models.refdata;

import lombok.Data;

@Data
public class Tribunal {

    private Integer tribunalId;
    private String tribunalCode;
    private String tribunalDesc;
    private String tribunalWwwAddress;

}
