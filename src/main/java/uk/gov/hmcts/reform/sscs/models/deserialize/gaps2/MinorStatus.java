package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import lombok.Value;

@Value
public class MinorStatus {
    String bfDate;
    String statusId;
    ZonedDateTime dateSet;

    public MinorStatus(@JsonProperty("BF_Date") String bfDate,
                       @JsonProperty("Status_Id") String statusId,
                       @JsonProperty("Date_Set") ZonedDateTime dateSet) {
        this.bfDate = bfDate;
        this.statusId = statusId;
        this.dateSet = dateSet;
    }
}
