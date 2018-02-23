package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.time.LocalDateTime;
import lombok.Value;

@Value
public class MajorStatus {
    private String bfDate;
    private String statusId;
    private String dateClosed;

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
    private LocalDateTime dateSet;

    public MajorStatus(@JsonProperty("BF_Date") String bfDate,
                   @JsonProperty("Status_Id") String statusId,
                   @JsonProperty("Date_Closed") String dateClosed,
                   @JsonProperty("Date_Set") LocalDateTime dateSet) {
        this.bfDate = bfDate;
        this.statusId = statusId;
        this.dateClosed = dateClosed;
        this.dateSet = dateSet;
    }
}
