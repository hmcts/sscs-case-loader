package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.models.GapsEvent.APPEAL_RECEIVED;
import static uk.gov.hmcts.reform.sscs.models.GapsEvent.RESPONSE_RECEIVED;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.*;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.*;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscription;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;

@RunWith(JUnitParamsRunner.class)
public class CcdCasesSenderTest {

    private static final String CASE_DETAILS_JSON = "CaseDetailsWithOneEventAndNoEvidence.json";
    private static final String CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON =
        "CaseDetailsWithOneEvidenceAndOneEvent.json";
    private static final String IDAM_OAUTH_2_TOKEN = "idamOauth2Token";
    private static final String SERVICE_AUTHORIZATION = "serviceAuthorization";
    public static final String CASE_DETAILS_WITH_HEARINGS_JSON = "CaseDetailsWithHearings.json";
    public static final String CASE_DETAILS_WITH_NO_HEARINGS_JSON = "CaseDetailsWithNoHearings.json";

    @Mock
    private CreateCcdService createCcdService;
    @Mock
    private UpdateCcdService updateCcdService;

    private CcdCasesSender ccdCasesSender;
    private IdamTokens idamTokens;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ccdCasesSender = new CcdCasesSender(createCcdService, updateCcdService);
        idamTokens = IdamTokens.builder()
            .idamOauth2Token(IDAM_OAUTH_2_TOKEN)
            .authenticationService(SERVICE_AUTHORIZATION)
            .build();
    }

    @Test
    public void givenACaseUpdate_shouldNotOverwriteSubscriptions() throws Exception {
        CaseData caseData = buildTestCaseDataWithEventAndEvidence();
        Subscriptions subscription = Subscriptions.builder()
            .appellantSubscription(Subscription.builder()
                .tya("001")
                .build())
            .build();
        caseData.setSubscriptions(subscription);

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        assertNull(caseData.getSubscriptions());
    }

    @Test
    public void shouldCreateInCcdGivenThereIsANewCaseAfterIgnoreCasesBeforeDateProperty() {
        ccdCasesSender.sendCreateCcdCases(buildCaseData(APPEAL_RECEIVED), idamTokens);

        verify(createCcdService, times(1))
            .create(eq(buildCaseData(APPEAL_RECEIVED)), eq(idamTokens));
    }

    @Test
    @Parameters({"APPEAL_RECEIVED", "RESPONSE_RECEIVED", "HEARING_BOOKED", "HEARING_POSTPONED", "APPEAL_LAPSED",
        "APPEAL_WITHDRAWN", "HEARING_ADJOURNED", "APPEAL_DORMANT"})
    public void shouldUpdateCcdGivenThereIsAnEventChange(GapsEvent gapsEvent) throws Exception {
        ccdCasesSender.sendUpdateCcdCases(buildCaseData(gapsEvent), getCaseDetails(CASE_DETAILS_JSON), idamTokens);

        verify(updateCcdService, times(1))
            .update(eq(buildCaseData(gapsEvent)), anyLong(), eq(gapsEvent.getType()), eq(idamTokens));
    }

    private CaseDetails getCaseDetails(String caseDetails) throws Exception {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(caseDetails);
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        return mapper.readerFor(CaseDetails.class).readValue(resourceAsStream);
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

        ccdCasesSender.sendUpdateCcdCases(caseData, getCaseDetails(CASE_DETAILS_JSON), idamTokens);

        verify(updateCcdService, times(0))
            .update(eq(caseData), anyLong(), any(), eq(idamTokens));
    }

    @Test
    public void shouldNotUpdateCcdGivenNewEventIsNull() throws Exception {
        CaseData caseData = CaseData.builder().build();

        ccdCasesSender.sendUpdateCcdCases(caseData, getCaseDetails(CASE_DETAILS_JSON), idamTokens);

        verify(updateCcdService, times(0))
            .update(eq(caseData), anyLong(), any(), eq(idamTokens));
    }

    @Test
    public void shouldNotUpdateCcdGivenNoNewFurtherEvidenceReceived() throws Exception {
        CaseData caseData = buildTestCaseDataWithEventAndEvidence();

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdService, times(0))
            .update(any(CaseData.class), anyLong(), eq("evidenceReceived"), eq(idamTokens));
    }

    private CaseData buildTestCaseDataWithEventAndEvidence() {
        return CaseData.builder()
            .evidence(Evidence.builder()
                .documents(Collections.singletonList(Documents.builder()
                    .value(Doc.builder()
                        .dateReceived("2017-05-24")
                        .evidenceType("Medical evidence")
                        .evidenceProvidedBy("Appellant")
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

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdService, times(1))
            .update(any(CaseData.class), anyLong(), eq("evidenceReceived"), eq(idamTokens));

        verify(updateCcdService, times(0))
            .update(any(CaseData.class), anyLong(), eq("appealReceived"), eq(idamTokens));
    }

    @Test
    public void shouldAddExistingHearingDetailsToTheCaseIfItsMissingInComingGaps2Xml() throws Exception {

        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);

        CaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        caseData.setHearings(buildHearings());

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdService).update(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getHearings().size(), equalTo(2));
        assertThat(caseDataArgumentCaptor.getValue().getHearings().get(0).getValue().getHearingDateTime(),
            equalTo("2018-04-0510:00:00"));
        assertThat(caseDataArgumentCaptor.getValue().getHearings().get(1).getValue().getHearingDateTime(),
            equalTo("2017-05-2410:00"));


    }

    @Test
    public void shouldAddNewHearingDetailsFromGap2XmlToTheCcd() throws Exception {

        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);

        CaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        caseData.setHearings(buildHearings());

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_NO_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdService).update(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getHearings().size(), equalTo(1));
        assertThat(caseDataArgumentCaptor.getValue().getHearings().get(0).getValue().getHearingDateTime(),
            equalTo("2018-04-0510:00:00"));

    }

    @Test
    public void shouldAddExistingNewHearingDetailsFromCcdToCaseWhenNoHearingDetailsinGaps2Xml() throws Exception {

        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);

        CaseData caseData = buildCaseData(RESPONSE_RECEIVED);

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdService).update(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getHearings().size(), equalTo(1));
        assertThat(caseDataArgumentCaptor.getValue().getHearings().get(0).getValue().getHearingDateTime(),
            equalTo("2017-05-2410:00"));


    }


    private List<Hearing> buildHearings() {
        return Collections.singletonList(
            Hearing.builder().value(
                HearingDetails.builder().hearingDate("2018-04-05").time("10:00:00").build())
                .build());
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
            .evidenceType("Medical evidence")
            .evidenceProvidedBy("Appellant")
            .dateReceived("2017-05-24")
            .build();

        Doc document2 = Doc.builder()
            .evidenceType("Medical evidence")
            .evidenceProvidedBy("Appellant")
            .dateReceived("2017-05-25")
            .build();

        List<Documents> documents = new ArrayList<>();

        documents.add(Documents.builder().value(document1).build());
        documents.add(Documents.builder().value(document2).build());

        return Evidence.builder().documents(documents).build();

    }
}
