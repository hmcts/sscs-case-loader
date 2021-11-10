package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.services.mapper.CaseDataBuilder.NO;
import static uk.gov.hmcts.reform.sscs.services.mapper.CaseDataBuilder.buildSubscriptionWithDefaults;
import static uk.gov.hmcts.reform.sscs.services.mapper.TransformAppealCaseToCaseData.APPELLANT_ROLE_ID;
import static uk.gov.hmcts.reform.sscs.services.mapper.TransformAppealCaseToCaseData.APPOINTEE_ROLE_ID;
import static uk.gov.hmcts.reform.sscs.services.mapper.TransformAppealCaseToCaseData.REP_ROLE_ID;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;
import uk.gov.hmcts.reform.sscs.service.AirLookupService;
import uk.gov.hmcts.reform.sscs.service.RegionalProcessingCenterService;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@RunWith(JUnitParamsRunner.class)
public class CaseDataBuilderTest extends CaseDataBuilderBase {

    public static final String YES = "Yes";
    @Mock
    private ReferenceDataService refDataService;
    @Mock
    private CaseDataEventBuilder caseDataEventBuilder;
    @Mock
    private RegionalProcessingCenterService regionalProcessingCentreService;
    @Mock
    private AirLookupService airLookupService;
    private CaseDataBuilder caseDataBuilder;
    private AppealCase appeal;
    private Parties applicantParty;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        createAppealCase(getHearing());
    }

    private void createAppealCase(List<uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing> hearings) {
        applicantParty = Parties.builder().postCode("AB1 1AB").build();
        appeal = AppealCase.builder()
            .parties(Collections.singletonList(applicantParty))
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), APPEAL_RECEIVED_DATE)
            ))
            .hearing(hearings)
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("26", HEARING_POSTPONED_DATE)))
            .build();
        caseDataBuilder = new CaseDataBuilder(refDataService, caseDataEventBuilder, regionalProcessingCentreService,
            airLookupService);
    }

    public List<uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing> getHearing() {
        uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing hearing =
            new uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing("100",
                "venue",
                "outcomeDate",
                "notificationDate",
                "2017-05-24T00:00:00+01:00",
                "2017-05-24T10:30:00+01:00",
                "id");
        return newArrayList(hearing);
    }

    @Test
    public void shouldBuildBenefitTypeGivenAppealCase() {
        when(refDataService.getBenefitType("1")).thenReturn("A");

        BenefitType benefitType = caseDataBuilder.buildBenefitType(appeal);
        assertThat(benefitType.getCode(), is("A"));
    }

    @Test
    public void shouldBuildSubscriptionsWithAppealCaseNumber() {
        Subscriptions subscriptions = caseDataBuilder.buildSubscriptions(
            Optional.empty(), Optional.empty(), Optional.empty(), null
        );
        assertNotNull("AppellantSubscription is null", subscriptions.getAppellantSubscription());
        assertNotNull("Representative Subscription is null", subscriptions.getRepresentativeSubscription());
        String appealNumber = subscriptions.getAppellantSubscription().getTya();
        assertTrue("appealNumber is empty", !"".equals(appealNumber));
        assertEquals("appealNumber length is not 10 digits", 10, appealNumber.length());
        assertEquals("representative email is not empty", "", subscriptions.getRepresentativeSubscription().getEmail());
    }

    @Test
    public void shouldBuildRepresentativeSubscriptionsWithAppealCaseNumber() {
        Parties party = Parties.builder()
            .email("my@email.com")
            .phone1("07123456789")
            .roleId(REP_ROLE_ID)
            .build();
        Subscriptions subscriptions = caseDataBuilder.buildSubscriptions(
            Optional.empty(), Optional.of(party), Optional.empty(), null
        );

        assertNotNull("AppellantSubscription is null", subscriptions.getAppellantSubscription());
        assertNotNull("Representative Subscription is null", subscriptions.getRepresentativeSubscription());
        assertNotNull("AppointeeSubscription is null", subscriptions.getAppointeeSubscription());

        String appealNumber = subscriptions.getRepresentativeSubscription().getTya();
        assertTrue("appealNumber is empty", !"".equals(appealNumber));
        assertEquals("appealNumber length is not 10 digits", 10, appealNumber.length());
        assertEquals("email is not " + party.getEmail(),
            party.getEmail(), subscriptions.getRepresentativeSubscription().getEmail());
        assertEquals("mobile number is not " + party.getMobile(),
            party.getMobile(), subscriptions.getRepresentativeSubscription().getMobile());
        assertEquals("email should be un-subscribed", "No",
            subscriptions.getRepresentativeSubscription().getSubscribeEmail());
        assertEquals("sms should be un-subscribed", "No",
            subscriptions.getRepresentativeSubscription().getSubscribeSms());
    }

    @Test
    @Parameters({",", "invalid,", "07123456789,07123456789"})
    public void givenInvalidAppellantMobile_shouldFallbackToEmptyString(
        String mobileNumber,
        String expectedMobileNumber
    ) {
        Parties party = Parties.builder()
            .email("my@email.com")
            .phone1(mobileNumber)
            .roleId(APPELLANT_ROLE_ID)
            .build();

        Subscriptions subscriptions = caseDataBuilder.buildSubscriptions(
            Optional.of(party), Optional.empty(), Optional.empty(), null
        );

        assertEquals(expectedMobileNumber, subscriptions.getAppellantSubscription().getMobile());
    }

    @Test
    @Parameters({",","invalid,", "07123456789,07123456789"})
    public void givenInvalidRepMobile_shouldFallbackToEmptyString(String mobileNumber, String expectedMobileNumber) {
        Parties party = Parties.builder()
            .email("my@email.com")
            .phone1(mobileNumber)
            .roleId(REP_ROLE_ID)
            .build();

        Subscriptions subscriptions = caseDataBuilder.buildSubscriptions(
            Optional.empty(), Optional.of(party), Optional.empty(), null
        );

        assertEquals(expectedMobileNumber, subscriptions.getRepresentativeSubscription().getMobile());
    }

    @Test
    @Parameters({",", "invalid,", "07123456789,07123456789"})
    public void givenInvalidAppointeeMobile_shouldFallbackToEmptyString(
        String mobileNumber,
        String expectedMobileNumber
    ) {
        Parties party = Parties.builder()
            .email("my@email.com")
            .phone1(mobileNumber)
            .roleId(APPOINTEE_ROLE_ID)
            .build();

        Subscriptions subscriptions = caseDataBuilder.buildSubscriptions(
            Optional.empty(), Optional.empty(), Optional.of(party), null
        );

        assertEquals(expectedMobileNumber, subscriptions.getAppointeeSubscription().getMobile());
    }

    @Test
    public void givenAnEmptyParty_shouldBuildEmptySubscription() {
        String appealNumber = "";
        Subscription subscription = buildSubscriptionWithDefaults(Optional.empty(), null, appealNumber);

        assertEquals("", subscription.getEmail());
        assertEquals("", subscription.getMobile());
        assertEquals("", subscription.getReason());
        assertEquals(NO, subscription.getSubscribeEmail());
        assertEquals(NO, subscription.getSubscribeSms());
        assertEquals(appealNumber, subscription.getTya());
    }

    @Test
    public void givenAParty_shouldBuildSubscriptionWithDefaults() {
        String email = "my@email.com";
        String mobile = "07123456789";

        Parties party = Parties.builder()
            .email(email)
            .phone1(mobile)
            .roleId(APPOINTEE_ROLE_ID)
            .build();

        String appealNumber = "123";
        Subscription subscription = buildSubscriptionWithDefaults(Optional.of(party), null, appealNumber);

        assertEquals(email, subscription.getEmail());
        assertEquals(mobile, subscription.getMobile());
        assertEquals("", subscription.getReason());
        assertEquals(NO, subscription.getSubscribeEmail());
        assertEquals(NO, subscription.getSubscribeSms());
        assertEquals(appealNumber, subscription.getTya());
    }

    @Test
    public void shouldBuildHearingsGivenAppealWithHearingBookedStatus() {
        VenueDetails venue = VenueDetails.builder()
            .venName("name")
            .build();

        when(refDataService.getVenueDetails("venue")).thenReturn(venue);

        List<Hearing> hearings = caseDataBuilder.buildHearings(appeal);

        Hearing hearing = hearings.get(0);

        assertThat(hearing.getValue().getAdjourned(), is(NO));
        assertThat(hearing.getValue().getHearingDate(), is("2017-05-24"));
        assertThat(hearing.getValue().getTime(), is("10:30:00"));
        assertThat(hearing.getValue().getVenue().getName(), is("name"));
    }

    @Test
    public void shouldCallAdjournedEventsBuilder() {
        AppealCase appealCase = AppealCase.builder().build();

        caseDataBuilder.buildEvent(appealCase);

        verify(caseDataEventBuilder, times(1)).buildAdjournedEvents(appealCase);
    }

    @Test
    public void shouldCallPostponedEventsBuilder() {
        AppealCase appealCase = AppealCase.builder().build();

        caseDataBuilder.buildEvent(appealCase);

        verify(caseDataEventBuilder, times(1)).buildPostponedEvent(appealCase);
    }

    @Test
    public void givenHearingInDeltaWhenBuildingHearingThenHearingIdAndVenueIdIsBuilt() {
        when(refDataService.getVenueDetails("venue")).thenReturn(VenueDetails.builder()
            .venName("name")
            .build());

        List<Hearing> hearingList = caseDataBuilder.buildHearings(appeal);

        assertEquals("id", hearingList.get(0).getValue().getHearingId());
        assertEquals("venue", hearingList.get(0).getValue().getVenueId());
    }

    @Test
    public void shouldBuildRegionalProcessingCentreFromPostcodeWhenThereAreNoHearings() {
        createAppealCase(Collections.emptyList());
        RegionalProcessingCenter expectedRegionalProcessingCentre = RegionalProcessingCenter.builder()
            .name("RPC form postcode")
            .build();
        when(regionalProcessingCentreService.getByPostcode(applicantParty.getPostCode()))
            .thenReturn(expectedRegionalProcessingCentre);
        RegionalProcessingCenter regionalProcessingCentre =
            caseDataBuilder.buildRegionalProcessingCentre(appeal, applicantParty);

        assertThat(regionalProcessingCentre, is(expectedRegionalProcessingCentre));
    }

    @Test
    public void shouldBuildRegionalProcessingCentreFromPostcodeWhenHearingsIsNull() {
        createAppealCase(null);
        RegionalProcessingCenter expectedRegionalProcessingCentre = RegionalProcessingCenter.builder()
            .name("RPC form postcode")
            .build();
        when(regionalProcessingCentreService.getByPostcode(applicantParty.getPostCode()))
            .thenReturn(expectedRegionalProcessingCentre);
        RegionalProcessingCenter regionalProcessingCentre =
            caseDataBuilder.buildRegionalProcessingCentre(appeal, applicantParty);

        assertThat(regionalProcessingCentre, is(expectedRegionalProcessingCentre));
    }

    @Test
    public void shouldBuildRegionalProcessingCentreFromHearing() {
        RegionalProcessingCenter expectedRegionalProcessingCentre = RegionalProcessingCenter.builder()
            .name("RPC form Hearing")
            .build();
        when(regionalProcessingCentreService.getByVenueId("venue")).thenReturn(expectedRegionalProcessingCentre);
        RegionalProcessingCenter regionalProcessingCentre =
            caseDataBuilder.buildRegionalProcessingCentre(appeal, applicantParty);

        assertThat(regionalProcessingCentre, is(expectedRegionalProcessingCentre));
    }

    @Test
    public void shouldBuildRegionalProcessingCentreFromLastHearing() {
        uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing hearing1 =
            uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing.builder()
                .venueId("venue1")
                .sessionDate("2017-05-24T00:00:00+01:00")
                .appealTime("2017-05-24T10:30:00+01:00")
                .build();
        uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing hearing2 =
            uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing.builder()
                .venueId("venue2")
                .sessionDate("2017-05-26T00:00:00+01:00")
                .appealTime("2017-05-26T11:30:00+01:00")
                .build();
        uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing hearing3 =
            uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing.builder()
                .venueId("venue3")
                .sessionDate("2017-05-26T00:00:00+01:00")
                .appealTime("2017-05-26T10:30:00+01:00")
                .build();

        createAppealCase(Arrays.asList(hearing1, hearing2, hearing3));

        RegionalProcessingCenter expectedRegionalProcessingCentre = RegionalProcessingCenter.builder()
            .name("RPC form 2nd Hearing")
            .build();
        when(regionalProcessingCentreService.getByVenueId("venue3")).thenReturn(expectedRegionalProcessingCentre);
        RegionalProcessingCenter regionalProcessingCentre =
            caseDataBuilder.buildRegionalProcessingCentre(appeal, applicantParty);

        assertThat(regionalProcessingCentre, is(expectedRegionalProcessingCentre));
    }

    @Test
    public void shouldSetHearingOptionsWantsToAttendToNoForPaperCase() {
        String tribunalsTypeId = "1";

        Parties parties = Parties.builder().build();

        when(refDataService.getTbtCode(tribunalsTypeId)).thenReturn("P");

        HearingOptions hearingOptions = caseDataBuilder.buildHearingOptions(parties, tribunalsTypeId);

        assertThat(hearingOptions.getWantsToAttend(), equalTo(NO));


    }


    @Test
    public void shouldSetHearingOptionsWantsToAttendToYesForOralCase() {
        String tribunalsTypeId = "2";

        Parties parties = Parties.builder().build();

        when(refDataService.getTbtCode(tribunalsTypeId)).thenReturn("O");

        HearingOptions hearingOptions = caseDataBuilder.buildHearingOptions(parties, tribunalsTypeId);

        assertThat(hearingOptions.getWantsToAttend(), equalTo(YES));

    }


    @Test
    public void shouldSetHearingOptionsWantsToAttendToYesForDomiciliaryCase() {
        String tribunalsTypeId = "3";

        Parties parties = Parties.builder().build();

        when(refDataService.getTbtCode(tribunalsTypeId)).thenReturn("D");

        HearingOptions hearingOptions = caseDataBuilder.buildHearingOptions(parties, tribunalsTypeId);

        assertThat(hearingOptions.getWantsToAttend(), equalTo(NO));

    }

    @Test
    public void shouldFindProcessingVenueFromAppellantPostcode() {
        createAppealCase(Collections.emptyList());

        when(airLookupService.lookupAirVenueNameByPostCode("AB1 1AB",
            BenefitType.builder().code("PIP").build())).thenReturn("venue1");

        String result = caseDataBuilder.findProcessingVenue(appeal.getAppealCaseId(),
            BenefitType.builder().code("PIP").build(), Optional.of(applicantParty), Optional.empty());

        assertEquals("venue1", result);
    }

    @Test
    public void shouldFindProcessingVenueFromAppointeePostcode() {
        Parties appointeeParty = Parties.builder().postCode("ZZ1 1BC").build();

        createAppealCase(Collections.emptyList());

        when(airLookupService.lookupAirVenueNameByPostCode("ZZ1 1BC",
            BenefitType.builder().code("PIP").build())).thenReturn("venue1");

        String result = caseDataBuilder.findProcessingVenue(appeal.getAppealCaseId(),
            BenefitType.builder().code("PIP").build(), Optional.of(applicantParty), Optional.of(appointeeParty));

        assertEquals("venue1", result);
    }

    @Test
    public void shouldFindProcessingVenueFromAppellentPostcodeWhenAppointeePostcodeIsNull() {
        createAppealCase(Collections.emptyList());

        when(airLookupService.lookupAirVenueNameByPostCode("AB1 1AB",
            BenefitType.builder().code("PIP").build())).thenReturn("venue1");

        String result = caseDataBuilder.findProcessingVenue(appeal.getAppealCaseId(),
            BenefitType.builder().code("PIP").build(), Optional.of(applicantParty), Optional.empty());

        assertEquals("venue1", result);
    }

}
