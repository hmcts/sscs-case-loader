package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.models.GapsEvent.APPEAL_RECEIVED;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.*;

@RunWith(JUnitParamsRunner.class)
public class CcdCasesSenderTest {

    private static final String CASE_DETAILS_JSON = "src/test/resources/CaseDetailsWithOneEventAndNoEvidence.json";
    private static final String CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON =
        "src/test/resources/CaseDetailsWithOneEvidenceAndOneEvent.json";

    @Mock
    private CcdApiWrapper ccdApiWrapper;
    @Mock
    private CcdCasesSender ccdCasesSender;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ccdCasesSender = new CcdCasesSender(ccdApiWrapper);
    }

    @Test
    public void shouldCreateInCcdGivenThereIsANewCaseAfterIgnoreCasesBeforeDateProperty() {
        ccdCasesSender.sendCreateCcdCases(buildCaseData(APPEAL_RECEIVED));

        verify(ccdApiWrapper, times(1))
            .create(eq(APPEAL_RECEIVED.getType()), eq(buildCaseData(APPEAL_RECEIVED)));
    }

    @Test
    @Parameters({"APPEAL_RECEIVED", "RESPONSE_RECEIVED", "HEARING_BOOKED", "HEARING_POSTPONED", "APPEAL_LAPSED",
        "APPEAL_WITHDRAWN", "HEARING_ADJOURNED", "APPEAL_DORMANT"})
    public void shouldUpdateCcdGivenThereIsAnEventChange(GapsEvent gapsEvent) throws Exception {
        ccdCasesSender.sendUpdateCcdCases(buildCaseData(gapsEvent), getCaseDetails(CASE_DETAILS_JSON));

        verify(ccdApiWrapper, times(1))
            .update(eq(buildCaseData(gapsEvent)), anyLong(), eq(gapsEvent.getType()));
    }

    private CaseDetails getCaseDetails(String caseDetails) throws Exception {
        String caseDetailsJson = FileUtils.readFileToString(new File(caseDetails),
            StandardCharsets.UTF_8.name());
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        return mapper.readerFor(CaseDetails.class).readValue(caseDetailsJson);
    }

    @Test
    public void shouldNotUpdateCcdGivenThereIsNoEventChange() throws Exception {
        CaseData caseData = CaseData.builder()
            .events(Collections.singletonList(Events.builder()
                .value(Event.builder()
                    .type(APPEAL_RECEIVED.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .build();

        ccdCasesSender.sendUpdateCcdCases(caseData, getCaseDetails(CASE_DETAILS_JSON));

        verify(ccdApiWrapper, times(0))
            .update(eq(caseData), anyLong(), any());
    }

    @Test
    public void shouldNotUpdateCcdGivenNewEventIsNull() throws Exception {
        CaseData caseData = CaseData.builder().build();

        ccdCasesSender.sendUpdateCcdCases(caseData, getCaseDetails(CASE_DETAILS_JSON));

        verify(ccdApiWrapper, times(0))
            .update(eq(caseData), anyLong(), any());
    }

    @Test
    public void shouldNotUpdateCcdGivenNoNewFurtherEvidenceReceived() throws Exception {
        CaseData caseData = CaseData.builder()
            .evidence(Evidence.builder()
                .documents(Collections.singletonList(Documents.builder()
                    .value(Doc.builder()
                        .dateReceived("2017-05-24")
                        .description("1")
                        .build())
                    .build()))
                .build())
            .events(Collections.singletonList(Events.builder()
                .value(Event.builder()
                    .type(APPEAL_RECEIVED.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .build();

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails);

        verify(ccdApiWrapper, times(0))
            .update(any(CaseData.class), anyLong(), eq("evidenceReceived"));
    }

    @Test
    public void shouldUpdateCcdTwiceGivenFileWithFurtherEvidence() throws Exception {
        CaseData caseData = CaseData.builder()
            .evidence(buildEvidence())
            .events(Collections.singletonList(Events.builder()
                .value(Event.builder()
                    .type(APPEAL_RECEIVED.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .build();

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails);

        verify(ccdApiWrapper, times(1))
            .update(any(CaseData.class), anyLong(), eq("evidenceReceived"));

        verify(ccdApiWrapper, times(0))
            .update(any(CaseData.class), anyLong(), eq("appealReceived"));
    }

    private CaseData buildCaseData(GapsEvent event) {
        Event appealCreatedEvent = Event.builder()
            .type("appealCreated")
            .description("Appeal Created")
            .date("2018-01-14T21:59:43.10")
            .build();

        Event updateEvent = Event.builder()
            .type(event.getType())
            .description(event.getDescription())
            .date("2018-01-15T21:59:43.10")
            .build();

        List<Events> events = new ArrayList<>();

        events.add(Events.builder().value(appealCreatedEvent).build());
        events.add(Events.builder().value(updateEvent).build());

        Collections.sort(events, Collections.reverseOrder());

        return CaseData.builder()
            .caseReference("SC068/17/00011")
            .events(events)
            .build();
    }

    private Evidence buildEvidence() {
        Doc document1 = Doc.builder()
            .description("1")
            .dateReceived("2017-05-24")
            .build();

        Doc document2 = Doc.builder()
            .description("Second evidence")
            .dateReceived("2017-05-25")
            .build();

        List<Documents> documents = new ArrayList<>();

        documents.add(Documents.builder().value(document1).build());
        documents.add(Documents.builder().value(document2).build());

        return Evidence.builder().documents(documents).build();

    }
}
