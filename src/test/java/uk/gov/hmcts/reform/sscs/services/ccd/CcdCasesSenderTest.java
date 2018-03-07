package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Doc;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Documents;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Evidence;

@RunWith(JUnitParamsRunner.class)
public class CcdCasesSenderTest {

    private static final String CASE_DETAILS_JSON = "src/test/resources/CaseDetailsWithOneEventAndNoEvidence.json";
    private static final String CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON =
        "src/test/resources/CaseDetailsWithOneEvidenceAndOneEvent.json";
    @Mock
    private UpdateCoreCaseDataService updateCoreCaseDataService;
    @Mock
    private CreateCoreCaseDataService createCoreCaseDataService;
    @Mock
    private SearchCoreCaseDataService searchCoreCaseDataService;
    @Mock

    private CcdCasesSender ccdCasesSender;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ccdCasesSender = new CcdCasesSender(createCoreCaseDataService, searchCoreCaseDataService,
            updateCoreCaseDataService);
    }

    @Test
    @Parameters({"APPEAL_RECEIVED", "RESPONSE_RECEIVED", "HEARING_BOOKED", "HEARING_POSTPONED", "APPEAL_LAPSED",
        "APPEAL_WITHDRAWN", "HEARING_ADJOURNED", "APPEAL_DORMANT"})
    public void givenThereIsAnEventChange_shouldUpdateCcd(GapsEvent gapsEvent) throws Exception {
        when(searchCoreCaseDataService.findCaseByCaseRef(anyString()))
            .thenReturn(Collections.singletonList(getCaseDetails(CASE_DETAILS_JSON)));

        ccdCasesSender.sendUpdateCcdCases(Collections.singletonList(buildUpdateCaseData(gapsEvent)));

        verify(updateCoreCaseDataService, times(1))
            .updateCase(eq(buildUpdateCaseData(gapsEvent)), anyLong(), eq(gapsEvent.getType()));
    }

    private CaseDetails getCaseDetails(String caseDetails) throws Exception {
        String caseDetailsJson = FileUtils.readFileToString(new File(caseDetails),
            StandardCharsets.UTF_8.name());
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        return mapper.readerFor(CaseDetails.class).readValue(caseDetailsJson);
    }

    @Test
    @Parameters({"APPEAL_RECEIVED", "RESPONSE_RECEIVED", "HEARING_BOOKED", "HEARING_POSTPONED", "APPEAL_LAPSED",
        "APPEAL_WITHDRAWN", "HEARING_ADJOURNED", "APPEAL_DORMANT"})
    public void givenThereIsNoEventChange_shouldNoUpdateCcd(GapsEvent gapsEvent) throws Exception {
        when(searchCoreCaseDataService.findCaseByCaseRef(anyString()))
            .thenReturn(Collections.singletonList(getCaseDetails(CASE_DETAILS_JSON)));

        CaseData caseData = CaseData.builder()
            .events(Collections.singletonList(Events.builder()
                .value(Event.builder()
                    .type(gapsEvent.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .build();

        ccdCasesSender.sendUpdateCcdCases(Collections.singletonList(caseData));

        verify(updateCoreCaseDataService, times(0))
            .updateCase(eq(caseData), anyLong(), any());
    }

    @Test
    public void givenNewEventIseNull_shouldNotUpdateCcd() throws Exception {
        when(searchCoreCaseDataService.findCaseByCaseRef(anyString()))
            .thenReturn(Collections.singletonList(getCaseDetails(CASE_DETAILS_JSON)));

        CaseData caseData = CaseData.builder().build();

        ccdCasesSender.sendUpdateCcdCases(Collections.singletonList(caseData));

        verify(updateCoreCaseDataService, times(0))
            .updateCase(eq(caseData), anyLong(), any());
    }

    @Test
    public void givenNoNewFurtherEvidenceReceived_shouldNotUpdateCcd() throws Exception {
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

        when(searchCoreCaseDataService.findCaseByCaseRef(anyString()))
            .thenReturn(Collections.singletonList(existingCaseDetails));

        ccdCasesSender.sendUpdateCcdCases(Collections.singletonList(caseData));

        verify(updateCoreCaseDataService, times(0))
            .updateCase(any(CaseData.class), anyLong(), eq("evidenceReceived"));
    }

    @Test
    public void givenFileWithFurtherEvidence_shouldUpdateCcdTwice() throws Exception {
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

        when(searchCoreCaseDataService.findCaseByCaseRef(anyString()))
            .thenReturn(Collections.singletonList(existingCaseDetails));

        ccdCasesSender.sendUpdateCcdCases(Collections.singletonList(caseData));

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq("evidenceReceived"));

        verify(updateCoreCaseDataService, times(0))
            .updateCase(any(CaseData.class), anyLong(), eq("appealReceived"));
    }

    private CaseData buildUpdateCaseData(GapsEvent event) {
        Event appealCreatedEvent = Event.builder()
            .type("appealCreated")
            .description("Appeal Created")
            .date("2018-01-14T21:59:43.10-05:00")
            .build();

        Event updateEvent = Event.builder()
            .type(event.getType())
            .description(event.getDescription())
            .date("2018-01-15T21:59:43.10-05:00")
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
