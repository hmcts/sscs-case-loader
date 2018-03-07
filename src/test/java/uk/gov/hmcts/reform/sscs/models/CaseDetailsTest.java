package uk.gov.hmcts.reform.sscs.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;

public class CaseDetailsTest {
    @Test
    public void givenCaseDetailsObject_shouldGetEventsList() {
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

    @Test
    public void givenCaseDetailsJson_shouldSerialiseEvents() throws Exception {
        String caseDetailsJson = FileUtils.readFileToString(new File("src/test/resources/CaseDetails.json"),
            StandardCharsets.UTF_8.name());
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        CaseDetails caseDetails = mapper.readerFor(CaseDetails.class).readValue(caseDetailsJson);
        List eventObjects = (ArrayList) caseDetails.getData().get("events");
        for (Object event : eventObjects) {
            LinkedHashMap value = (LinkedHashMap) ((LinkedHashMap) event).get("value");
            event = Event.builder()
                .date((String) value.get("date"))
                .type((String) value.get("type"))
                .description((String) value.get("description"))
                .build();
            assertNotNull(event);
        }
    }

    @Test
    public void givenCaseDetailsJson_shouldSerialiseEvidence() throws Exception {
        String caseDetailsJson = FileUtils.readFileToString(new File("src/test/resources/CaseDetails.json"),
            StandardCharsets.UTF_8.name());
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        CaseDetails caseDetails = mapper.readerFor(CaseDetails.class).readValue(caseDetailsJson);
        LinkedHashMap evidence = (LinkedHashMap) caseDetails.getData().get("evidence");
        assertNotNull(evidence);
    }

}
