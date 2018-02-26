package uk.gov.hmcts.reform.sscs.models.refdata;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Venue {

    private Integer venueId;
    private String venName;
    private String venAddressLine1;
    private String venAddressLine2;
    private String venAddressTown;
    private String venAddressCounty;
    private String venAddressPostcode;
    private String venAddressTelNo;
    private String venAddressFaxNo;
    private LocalDateTime venCloseDate;
    private String venEmail;
    private Integer districtId;

}
