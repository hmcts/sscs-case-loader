package uk.gov.hmcts.reform.sscs.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;

public class CaseDetailsTest {
    @Test
    public void givenCaseDetailsObject_shouldGetEventsList() throws Exception {
        CaseDetails caseDetails = givenCaseDetails();

        //When
        @SuppressWarnings("unchecked")
        List<Events> events = (List<Events>) caseDetails.getData().get("events");

        //Should
        assertTrue(events.size() == 1);
        assertEquals("AppealReceived", events.get(0).getValue().getType());
    }

    private CaseDetails givenCaseDetails() {
        Map<String, Object> data = new HashMap<>();
        data.put("events", buildEvents());
        return CaseDetails.builder().data(data).build();
    }

    private List<Events> buildEvents() {
        Event value = Event.builder()
            .date(LocalDate.now().toString())
            .description("test")
            .type("AppealReceived")
            .build();
        return Collections.singletonList(Events.builder()
            .value(value)
            .build());
    }
}
