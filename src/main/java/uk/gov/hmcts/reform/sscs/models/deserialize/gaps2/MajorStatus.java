package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import lombok.Value;

@Value
public class MajorStatus {
    private String bfDate;
    private String statusId;
    private String dateClosed;
    private ZonedDateTime dateSet;

    public MajorStatus(@JsonProperty("BF_Date") String bfDate,
                       @JsonProperty("Status_Id") String statusId,
                       @JsonProperty("Date_Closed") String dateClosed,
                       @JsonProperty("Date_Set") ZonedDateTime dateSet) {
        this.bfDate = bfDate;
        this.statusId = statusId;
        this.dateClosed = dateClosed;
        this.dateSet = dateSet;
    }
}
