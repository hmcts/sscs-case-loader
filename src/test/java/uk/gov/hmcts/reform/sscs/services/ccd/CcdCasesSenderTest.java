package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscs.CaseDetailsUtils.getCaseDetails;
import static uk.gov.hmcts.reform.sscs.models.GapsEvent.APPEAL_RECEIVED;
import static uk.gov.hmcts.reform.sscs.models.GapsEvent.RESPONSE_RECEIVED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.refdata.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.*;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscription;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;
import uk.gov.hmcts.reform.sscs.services.refdata.RegionalProcessingCenterService;

@RunWith(JUnitParamsRunner.class)
public class CcdCasesSenderTest {

    private static final String CASE_DETAILS_JSON = "CaseDetailsWithOneEventAndNoEvidence.json";
    private static final String CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON =
        "CaseDetailsWithOneEvidenceAndOneEvent.json";
    private static final String OAUTH2 = "token";
    private static final String SERVICE_AUTHORIZATION = "serviceAuthorization";
    private static final String USER_ID = "16";
    private static final String CASE_DETAILS_WITH_HEARINGS_JSON = "CaseDetailsWithHearings.json";
    private static final String CASE_DETAILS_WITH_NO_HEARINGS_JSON = "CaseDetailsWithNoHearings.json";
    private static final String CASE_DETAILS_WITH_HEARING_OPTIONS_JSON = "CaseDetailsWithHearingOptions.json";
    private static final String CASE_DETAILS_WITH_APPEAL_RECEIVED_JSON = "CaseDetailsWithAppealReceived.json";


    @Mock
    private CreateCcdService createCcdService;
    @Mock
    private UpdateCcdService updateCcdService;
    @Mock
    private RegionalProcessingCenterService regionalProcessingCenterService;

    private CcdCasesSender ccdCasesSender;
    private IdamTokens idamTokens;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ccdCasesSender = new CcdCasesSender(createCcdService, updateCcdService, regionalProcessingCenterService);
        idamTokens = IdamTokens.builder()
            .idamOauth2Token(OAUTH2)
            .serviceAuthorization(SERVICE_AUTHORIZATION)
            .userId(USER_ID)
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

        CaseDetails existingCaseDetails = getCaseDetails(
            CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        assertNull(caseData.getSubscriptions());
    }

    @Test
    public void givenACaseUpdate_shouldOnlyOverwriteFieldsThatExistInGapsData() throws Exception {
        CaseData caseData = buildTestCaseDataWithAppellantAndBenefitType();
        Appellant appellant = caseData.getAppeal().getAppellant();
        BenefitType benefitType = caseData.getAppeal().getBenefitType();

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_HEARING_OPTIONS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        assertEquals(appellant, caseData.getAppeal().getAppellant());
        assertEquals(benefitType, caseData.getAppeal().getBenefitType());

        assertNull(caseData.getAppeal().getMrnDetails());
        assertNull(caseData.getAppeal().getAppealReasons());
        assertNull(caseData.getAppeal().getHearingOptions());
        assertNull(caseData.getAppeal().getRep());
        assertNull(caseData.getAppeal().getSigner());
    }

    @Test
    public void shouldCreateInCcdGivenThereIsANewCaseAfterIgnoreCasesBeforeDateProperty() {
        when(regionalProcessingCenterService.getByScReferenceCode(anyString()))
            .thenReturn(getRegionalProcessingCenter());
        ccdCasesSender.sendCreateCcdCases(buildCaseData(APPEAL_RECEIVED), idamTokens);

        CaseData caseData = buildCaseData(APPEAL_RECEIVED);
        caseData.setRegion(getRegionalProcessingCenter().getName());
        caseData.setRegionalProcessingCenter(getRegionalProcessingCenter());

        verify(createCcdService, times(1))
            .create(eq(caseData), eq(idamTokens));
    }

    @Test
    public void shouldNotAddRegionalProcessingCenterIfDisabled() {
        ReflectionTestUtils.setField(ccdCasesSender, "lookupRpcByVenueId", true);
        ccdCasesSender.sendCreateCcdCases(buildCaseData(APPEAL_RECEIVED), idamTokens);

        CaseData caseData = buildCaseData(APPEAL_RECEIVED);

        verifyZeroInteractions(regionalProcessingCenterService);
        verify(createCcdService, times(1))
            .create(eq(caseData), eq(idamTokens));
    }

    @Test
    @Parameters({"APPEAL_RECEIVED", "RESPONSE_RECEIVED", "HEARING_BOOKED", "HEARING_POSTPONED", "APPEAL_LAPSED",
        "APPEAL_WITHDRAWN", "HEARING_ADJOURNED", "APPEAL_DORMANT"})
    public void shouldUpdateCcdGivenThereIsAnEventChange(GapsEvent gapsEvent) throws Exception {
        when(regionalProcessingCenterService.getByScReferenceCode(anyString()))
            .thenReturn(getRegionalProcessingCenter());
        ccdCasesSender.sendUpdateCcdCases(buildCaseData(gapsEvent),
            getCaseDetails(CASE_DETAILS_JSON), idamTokens);

        CaseData caseData = buildCaseData(gapsEvent);
        caseData.setRegion(getRegionalProcessingCenter().getName());
        caseData.setRegionalProcessingCenter(getRegionalProcessingCenter());
        verify(updateCcdService, times(1))
            .update(eq(caseData), anyLong(), eq(gapsEvent.getType()), eq(idamTokens));
    }

    @Test
    public void shouldUpdateCcdGivenThereIsADataChange() throws Exception {
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

        verify(updateCcdService, times(1))
            .update(eq(caseData), anyLong(), eq("caseUpdated"), eq(idamTokens));
    }

    @Test
    public void shouldNotUpdateCcdGivenThereIsNoEventChangeOrDataChange() throws Exception {
        CaseData caseData = buildTestCaseDataWithAppellantAndBenefitType();

        ccdCasesSender.sendUpdateCcdCases(caseData, getCaseDetails(CASE_DETAILS_WITH_APPEAL_RECEIVED_JSON), idamTokens);

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

        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(getRegionalProcessingCenter());

        CaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        caseData.setHearings(buildHearings());
        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdService).update(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getHearings().size(), equalTo(3));
        assertThat(caseDataArgumentCaptor.getValue().getHearings().get(0).getValue().getHearingDateTime(),
            equalTo("2018-04-0510:00:00"));
        assertThat(caseDataArgumentCaptor.getValue().getHearings().get(1).getValue().getHearingDateTime(),
            equalTo("2017-05-2410:00"));
    }

    @Test
    public void shouldAddNewHearingDetailsFromGap2XmlToTheCcd() throws Exception {

        CaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        caseData.setHearings(buildHearings());
        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);

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

        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(getRegionalProcessingCenter());

        CaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdService).update(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getHearings().size(), equalTo(2));
        assertThat(caseDataArgumentCaptor.getValue().getHearings().get(0).getValue().getHearingDateTime(),
            equalTo("2017-05-2410:00"));


    }

    @Test
    public void shouldAddRegionalProcessingCenterForANewCaseInCcdFromGaps2() {
        RegionalProcessingCenter regionalProcessingCenter = getRegionalProcessingCenter();
        CaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(regionalProcessingCenter);
        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);

        ccdCasesSender.sendCreateCcdCases(caseData, idamTokens);

        verify(createCcdService).create(caseDataArgumentCaptor.capture(), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getRegion(), equalTo(regionalProcessingCenter.getName()));
        assertThat(caseDataArgumentCaptor.getValue().getRegionalProcessingCenter(), equalTo(regionalProcessingCenter));

    }


    @Test
    public void shouldAddRegionalProcessingCenterForAnExistingCaseIfItsNotAlreadyPresentInCcd() throws Exception {
        RegionalProcessingCenter regionalProcessingCenter = getRegionalProcessingCenter();
        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);

        CaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(regionalProcessingCenter);

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdService).update(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getRegionalProcessingCenter(), equalTo(regionalProcessingCenter));
        assertThat(caseDataArgumentCaptor.getValue().getRegion(), equalTo(regionalProcessingCenter.getName()));
    }

    @Test
    public void shouldNotAddRegionalProcessingCenterForAnExistingCaseIfItsNotAlreadyPresentInCcdIfDisabled()
        throws Exception {
        ReflectionTestUtils.setField(ccdCasesSender, "lookupRpcByVenueId", true);

        RegionalProcessingCenter regionalProcessingCenter = getRegionalProcessingCenter();
        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);

        CaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(regionalProcessingCenter);

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdService).update(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()), eq(idamTokens));

        verifyZeroInteractions(regionalProcessingCenterService);
    }

    @Test
    public void shouldAddRegionalProcessingCenterOnlyIfItsPresent() throws Exception {
        ArgumentCaptor<CaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(CaseData.class);

        CaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(null);

        CaseDetails existingCaseDetails = getCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdService).update(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getRegionalProcessingCenter(), equalTo(null));


    }

    private RegionalProcessingCenter getRegionalProcessingCenter() {
        return RegionalProcessingCenter.builder()
            .name("CARDIFF")
            .address1("HM Courts & Tribunals Service")
            .address2("Social Security & Child Support Appeals")
            .address3("Eastgate House")
            .address4("Newport Road")
            .city("CARDIFF")
            .postcode("CF24 0AB")
            .phoneNumber("0300 123 1142")
            .faxNumber("0870 739 4438")
            .build();
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

        events.sort(Collections.reverseOrder());

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

    private CaseData buildTestCaseDataWithAppellantAndBenefitType() {
        Name name = Name.builder()
            .title("Mr")
            .firstName("User")
            .lastName("Test")
            .build();
        Contact contact = Contact.builder()
            .email("mail@email.com")
            .phone("01234567890")
            .mobile("01234567890")
            .build();
        Identity identity = Identity.builder()
            .dob("1904-03-10")
            .nino("AB 22 55 66 B")
            .build();
        Appellant appellant = Appellant.builder()
            .name(name)
            .contact(contact)
            .identity(identity)
            .build();
        BenefitType benefitType = BenefitType.builder()
            .code("PIP")
            .build();

        final Appeal appeal = Appeal.builder()
            .appellant(appellant)
            .benefitType(benefitType)
            .build();

        Event event = Event.builder()
            .type("appealReceived")
            .description("Appeal Received")
            .date("2001-12-14T21:59:43.10-05:00")
            .build();
        Events events = Events.builder()
            .value(event)
            .build();

        return CaseData.builder()
            .appeal(appeal)
            .events(Collections.singletonList(events))
            .build();
    }

}
