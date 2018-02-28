package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Events implements Comparable<Events> {
    Event value;

    @Override
    public int compareTo(Events o) {
        return value.getDate().compareTo(o.getValue().getDate());
    }

}
