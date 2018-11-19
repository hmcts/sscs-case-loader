package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscs.CaseDetailsUtils.getSscsCaseDetails;
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
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.service.RegionalProcessingCenterService;

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
    public static final String SSCS_APPEAL_UPDATED_EVENT = "SSCS - appeal updated event";
    public static final String UPDATED_SSCS = "Updated SSCS";
    public static final String CASE_DETAILS_WITH_SUBSCRIPTIONS_JSON = "CaseDetailsWithSubscriptions.json";
    public static final String FIRST_NAME = "first-name";
    public static final String LAST_NAME = "last-name";
    public static final String EMAIL_EMAIL_COM = "email@email.com";
    public static final String NINO = "AB46575S";
    public static final String DOB = "12-20-2018";
    public static final String MOBILE = "07777777777";


    @Mock
    private UpdateCcdCaseService updateCcdCaseService;
    @Mock
    private RegionalProcessingCenterService regionalProcessingCenterService;
    @Mock
    private CcdService ccdService;

    private CcdCasesSender ccdCasesSender;
    private IdamTokens idamTokens;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ccdCasesSender = new CcdCasesSender(ccdService, updateCcdCaseService, regionalProcessingCenterService);
        idamTokens = IdamTokens.builder()
            .idamOauth2Token(OAUTH2)
            .serviceAuthorization(SERVICE_AUTHORIZATION)
            .userId(USER_ID)
            .build();
    }

    @Test
    public void givenACaseUpdate_shouldNotOverwriteSubscriptions() throws Exception {
        SscsCaseData caseData = buildTestCaseDataWithEventAndEvidence();
        Subscriptions subscription = Subscriptions.builder()
            .appellantSubscription(Subscription.builder()
                .tya("001")
                .build())
            .build();
        caseData.setSubscriptions(subscription);
        caseData.setAppeal(buildAppeal());

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_SUBSCRIPTIONS_JSON);

        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(caseDataArgumentCaptor.capture(), eq(existingCaseDetails.getId()), eq("evidenceReceived"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        verify(updateCcdCaseService, times(1))
            .updateCase(caseDataArgumentCaptor.capture(), eq(existingCaseDetails.getId()), eq("caseUpdated"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        Subscriptions subscriptions = caseDataArgumentCaptor.getValue().getSubscriptions();

        assertNotNull(subscriptions);

        assertThat(subscriptions.getAppellantSubscription().getTya(), equalTo("abcde12345"));


    }

    @Test
    public void givenACaseUpdate_shouldOnlyOverwriteFieldsThatExistInGapsData() throws Exception {
        SscsCaseData caseData = buildTestCaseDataWithAppellantAndBenefitType();
        Appellant appellant = caseData.getAppeal().getAppellant();
        BenefitType benefitType = caseData.getAppeal().getBenefitType();

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARING_OPTIONS_JSON);

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

        SscsCaseData caseData = buildCaseData(APPEAL_RECEIVED);
        caseData.setRegion(getRegionalProcessingCenter().getName());
        caseData.setRegionalProcessingCenter(getRegionalProcessingCenter());

        verify(ccdService, times(1))
            .createCase(eq(caseData), eq(idamTokens));
    }

    @Test
    public void shouldNotAddRegionalProcessingCenterIfDisabled() {
        ReflectionTestUtils.setField(ccdCasesSender, "lookupRpcByVenueId", true);
        ccdCasesSender.sendCreateCcdCases(buildCaseData(APPEAL_RECEIVED), idamTokens);

        SscsCaseData caseData = buildCaseData(APPEAL_RECEIVED);

        verifyZeroInteractions(regionalProcessingCenterService);
        verify(ccdService, times(1))
            .createCase(eq(caseData), eq(idamTokens));
    }

    @Test
    @Parameters({"APPEAL_RECEIVED", "RESPONSE_RECEIVED", "HEARING_BOOKED", "HEARING_POSTPONED", "APPEAL_LAPSED",
        "APPEAL_WITHDRAWN", "HEARING_ADJOURNED", "APPEAL_DORMANT"})
    public void shouldUpdateCcdGivenThereIsAnEventChange(GapsEvent gapsEvent) throws Exception {
        when(regionalProcessingCenterService.getByScReferenceCode(anyString()))
            .thenReturn(getRegionalProcessingCenter());
        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        ccdCasesSender.sendUpdateCcdCases(buildCaseData(gapsEvent),
            sscsCaseDetails, idamTokens);

        SscsCaseData caseData = buildCaseData(gapsEvent);
        caseData.setRegion(getRegionalProcessingCenter().getName());
        caseData.setRegionalProcessingCenter(getRegionalProcessingCenter());
        verify(updateCcdCaseService, times(1))
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(gapsEvent.getType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void shouldUpdateCcdGivenThereIsADataChange() throws Exception {
        SscsCaseData caseData = SscsCaseData.builder()
            .events(Collections.singletonList(Event.builder()
                .value(EventDetails.builder()
                    .type(APPEAL_RECEIVED.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .appeal(buildAppeal())
            .build();

        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq("caseUpdated"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void shouldNotUpdateCcdGivenThereIsNoEventChangeOrDataChange() throws Exception {
        SscsCaseData caseData = buildTestCaseDataWithAppellantAndBenefitType();

        ccdCasesSender.sendUpdateCcdCases(caseData,
            getSscsCaseDetails(CASE_DETAILS_WITH_APPEAL_RECEIVED_JSON), idamTokens);

        verify(updateCcdCaseService, times(0))
            .updateCase(eq(caseData), anyLong(), any(),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void shouldNotUpdateCcdGivenNewEventIsNull() throws Exception {
        SscsCaseData caseData = SscsCaseData.builder().build();

        ccdCasesSender.sendUpdateCcdCases(caseData, getSscsCaseDetails(CASE_DETAILS_JSON), idamTokens);

        verify(updateCcdCaseService, times(0))
            .updateCase(eq(caseData), anyLong(), any(),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void shouldNotUpdateCcdGivenNoNewFurtherEvidenceReceived() throws Exception {
        SscsCaseData caseData = buildTestCaseDataWithEventAndEvidence();
        caseData.setAppeal(buildAppeal());

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(0))
            .updateCase(any(SscsCaseData.class), anyLong(), eq("evidenceReceived"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    private SscsCaseData buildTestCaseDataWithEventAndEvidence() {
        return SscsCaseData.builder()
            .evidence(Evidence.builder()
                .documents(Collections.singletonList(Document.builder()
                    .value(DocumentDetails.builder()
                        .dateReceived("2017-05-24")
                        .evidenceType("Medical evidence")
                        .evidenceProvidedBy("Appellant")
                        .build())
                    .build()))
                .build())
            .events(Collections.singletonList(Event.builder()
                .value(EventDetails.builder()
                    .type(APPEAL_RECEIVED.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .build();
    }

    @Test
    public void shouldUpdateCcdTwiceGivenFileWithFurtherEvidence() throws Exception {
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);
        SscsCaseData caseData = SscsCaseData.builder()
            .evidence(buildEvidence())
            .events(Collections.singletonList(Event.builder()
                .value(EventDetails.builder()
                    .type(APPEAL_RECEIVED.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .appeal(buildAppeal())
            .build();

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(caseDataArgumentCaptor.capture(), eq(existingCaseDetails.getId()), eq("evidenceReceived"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        verify(updateCcdCaseService, times(0))
            .updateCase(any(SscsCaseData.class), anyLong(), eq("appealReceived"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        Evidence evidence = caseDataArgumentCaptor.getValue().getEvidence();

        assertThat(evidence, not(equalTo(null)));
        assertThat(evidence.getDocuments().size(), equalTo(2));

    }

    @Test
    public void shouldAddExistingHearingDetailsToTheCaseIfItsMissingInComingGaps2Xml() throws Exception {

        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(getRegionalProcessingCenter());

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        caseData.setHearings(buildHearings());
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCase(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getHearings().size(), equalTo(3));

        HearingDetails hearingDetails = caseDataArgumentCaptor.getValue().getHearings().get(0).getValue();
        HearingDetails hearingDetails1 = caseDataArgumentCaptor.getValue().getHearings().get(1).getValue();

        assertThat(hearingDetails.getHearingDate() + hearingDetails.getTime(), equalTo("2018-04-0510:00:00"));
        assertThat(hearingDetails1.getHearingDate() + hearingDetails1.getTime(),
            equalTo("2017-05-2410:00"));
    }

    @Test
    public void shouldAddNewHearingDetailsFromGap2XmlToTheCcd() throws Exception {

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        caseData.setHearings(buildHearings());
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_NO_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCase(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        HearingDetails hearingDetails = caseDataArgumentCaptor.getValue().getHearings().get(0).getValue();

        assertThat(caseDataArgumentCaptor.getValue().getHearings().size(), equalTo(1));
        assertThat(hearingDetails.getHearingDate() + hearingDetails.getTime(),
            equalTo("2018-04-0510:00:00"));
    }

    @Test
    public void shouldAddExistingNewHearingDetailsFromCcdToCaseWhenNoHearingDetailsinGaps2Xml() throws Exception {

        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(getRegionalProcessingCenter());

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCase(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        HearingDetails hearingDetails = caseDataArgumentCaptor.getValue().getHearings().get(0).getValue();

        assertThat(caseDataArgumentCaptor.getValue().getHearings().size(), equalTo(2));
        assertThat(hearingDetails.getHearingDate() + hearingDetails.getTime(),
            equalTo("2017-05-2410:00"));
    }

    @Test
    public void shouldAddRegionalProcessingCenterForANewCaseInCcdFromGaps2() {
        RegionalProcessingCenter regionalProcessingCenter = getRegionalProcessingCenter();
        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(regionalProcessingCenter);
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        ccdCasesSender.sendCreateCcdCases(caseData, idamTokens);

        verify(ccdService).createCase(caseDataArgumentCaptor.capture(), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getRegion(), equalTo(regionalProcessingCenter.getName()));
        assertThat(caseDataArgumentCaptor.getValue().getRegionalProcessingCenter(), equalTo(regionalProcessingCenter));

    }


    @Test
    public void shouldAddRegionalProcessingCenterForAnExistingCaseIfItsNotAlreadyPresentInCcd() throws Exception {
        RegionalProcessingCenter regionalProcessingCenter = getRegionalProcessingCenter();
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(regionalProcessingCenter);

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCase(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
        assertThat(caseDataArgumentCaptor.getValue().getRegionalProcessingCenter(), equalTo(regionalProcessingCenter));
        assertThat(caseDataArgumentCaptor.getValue().getRegion(), equalTo(regionalProcessingCenter.getName()));
    }

    @Test
    public void shouldNotAddRegionalProcessingCenterForAnExistingCaseIfItsNotAlreadyPresentInCcdIfDisabled()
        throws Exception {
        ReflectionTestUtils.setField(ccdCasesSender, "lookupRpcByVenueId", true);

        RegionalProcessingCenter regionalProcessingCenter = getRegionalProcessingCenter();
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(regionalProcessingCenter);

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCase(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        verifyZeroInteractions(regionalProcessingCenterService);
    }

    @Test
    public void shouldAddRegionalProcessingCenterOnlyIfItsPresent() throws Exception {
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        when(regionalProcessingCenterService.getByScReferenceCode("SC068/17/00011"))
            .thenReturn(null);

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCase(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getRegionalProcessingCenter(), equalTo(null));


    }

    @Test
    public void shouldUpdateAppellantDetailsIfThereIsAChangeInGaps2Record() throws Exception {
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        SscsCaseData caseData = buildTestCaseDataWithAppellantAndBenefitType();

        Appellant appellant = Appellant.builder()
            .name(Name.builder().firstName(FIRST_NAME).lastName(LAST_NAME).title("Mr").build())
            .contact(Contact.builder().email(EMAIL_EMAIL_COM).mobile(MOBILE).build())
            .identity(Identity.builder().nino(NINO).dob(DOB).build())
            .build();

        caseData.getAppeal().setAppellant(appellant);

        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(caseDataArgumentCaptor.capture(), anyLong(), eq("caseUpdated"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        SscsCaseData sscsCaseData = caseDataArgumentCaptor.getValue();

        assertThat(sscsCaseData.getAppeal().getAppellant().getName().getFirstName(), equalTo(FIRST_NAME));
        assertThat(sscsCaseData.getAppeal().getAppellant().getName().getLastName(), equalTo(LAST_NAME));
        assertThat(sscsCaseData.getAppeal().getAppellant().getContact().getEmail(), equalTo(EMAIL_EMAIL_COM));
        assertThat(sscsCaseData.getAppeal().getAppellant().getIdentity().getNino(), equalTo(NINO));
        assertThat(sscsCaseData.getGeneratedSurname(), equalTo(LAST_NAME));
        assertThat(sscsCaseData.getGeneratedNino(), equalTo(NINO));
        assertThat(sscsCaseData.getGeneratedEmail(), equalTo(EMAIL_EMAIL_COM));
        assertThat(sscsCaseData.getGeneratedDob(), equalTo(DOB));
        assertThat(sscsCaseData.getGeneratedMobile(), equalTo(MOBILE));
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

    private SscsCaseData buildCaseData(GapsEvent event) {
        final Appeal appeal = buildAppeal();
        EventDetails appealCreatedEvent = EventDetails.builder()
            .type("appealCreated")
            .description("Appeal Created")
            .date("2018-01-14T21:59:43.10")
            .build();

        EventDetails updateEvent = EventDetails.builder()
            .type(event.getType())
            .description(event.getDescription())
            .date("2018-01-15T21:59:43.10")
            .build();

        List<Event> events = new ArrayList<>();

        events.add(Event.builder().value(appealCreatedEvent).build());
        events.add(Event.builder().value(updateEvent).build());

        events.sort(Collections.reverseOrder());

        return SscsCaseData.builder()
            .caseReference("SC068/17/00011")
            .appeal(appeal)
            .events(events)
            .build();
    }

    private Appeal buildAppeal() {
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

        return Appeal.builder()
            .appellant(appellant)
            .benefitType(benefitType)
            .build();
    }

    private Evidence buildEvidence() {
        DocumentDetails document1 = DocumentDetails.builder()
            .evidenceType("Medical evidence")
            .evidenceProvidedBy("Appellant")
            .dateReceived("2017-05-24")
            .build();

        DocumentDetails document2 = DocumentDetails.builder()
            .evidenceType("Medical evidence")
            .evidenceProvidedBy("Appellant")
            .dateReceived("2017-05-25")
            .build();

        List<Document> documents = new ArrayList<>();

        documents.add(Document.builder().value(document1).build());
        documents.add(Document.builder().value(document2).build());

        return Evidence.builder().documents(documents).build();

    }

    private SscsCaseData buildTestCaseDataWithAppellantAndBenefitType() {
        final Appeal appeal = buildAppeal();

        EventDetails event = EventDetails.builder()
            .type("appealReceived")
            .description("Appeal Received")
            .date("2001-12-14T21:59:43.10-05:00")
            .build();
        Event events = Event.builder()
            .value(event)
            .build();

        return SscsCaseData.builder()
            .appeal(appeal)
            .events(Collections.singletonList(events))
            .build();
    }

}
