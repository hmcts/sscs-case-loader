package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CaseData {
    private String caseReference;
    private Appeal appeal;
    private List<Hearing> hearings;
    private Evidence evidence;
    private List<DwpTimeExtension> dwpTimeExtension;
    private List<Events> events;
    private String generatedNino;
    private String generatedSurname;
    private String generatedEmail;
    private String generatedMobile;

    @JsonIgnore
    public Event getLatestEvent() {
        return events != null && !events.isEmpty() ? events.get(0).getValue() : null;
    }

    @JsonIgnore
    public String getLatestEventType() {
        Event latestEvent = getLatestEvent();
        return latestEvent != null ? latestEvent.getType() : null;
    }
}
