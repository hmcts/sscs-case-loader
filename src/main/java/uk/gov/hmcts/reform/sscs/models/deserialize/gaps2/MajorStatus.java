package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.ZonedDateTime;
import lombok.Value;

@Value
public class MajorStatus implements Comparable<MajorStatus> {

    String bfDate;
    String statusId;
    String dateClosed;
    ZonedDateTime dateSet;

    public MajorStatus(@JsonAlias("BF_Date") String bfDate,
                       @JsonAlias("Status_Id") String statusId,
                       @JsonAlias("Date_Closed") String dateClosed,
                       @JsonAlias("Date_Set") ZonedDateTime dateSet) {
        this.bfDate = bfDate;
        this.statusId = statusId;
        this.dateClosed = dateClosed;
        this.dateSet = dateSet;
    }

    @Override
    public int compareTo(MajorStatus o) {
        return dateSet.compareTo(o.dateSet);
    }
}
