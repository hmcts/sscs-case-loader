package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.ZonedDateTime;
import lombok.Value;

@Value
public class MinorStatus {

    String bfDate;
    String statusId;
    ZonedDateTime dateSet;

    public MinorStatus(@JsonAlias("BF_Date") String bfDate,
                       @JsonAlias("Status_Id") String statusId,
                       @JsonAlias("Date_Set") ZonedDateTime dateSet) {
        this.bfDate = bfDate;
        this.statusId = statusId;
        this.dateSet = dateSet;
    }
}
