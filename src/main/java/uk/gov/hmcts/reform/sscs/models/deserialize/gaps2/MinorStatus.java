package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MinorStatus {
    String bfDate;
    String statusId;
    ZonedDateTime dateSet;
}
