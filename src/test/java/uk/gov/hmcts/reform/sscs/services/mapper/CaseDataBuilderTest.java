package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.services.mapper.CaseDataBuilder.NO;

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
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitType;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
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
        caseDataBuilder = new CaseDataBuilder(refDataService, caseDataEventBuilder, regionalProcessingCentreService);
    }

    public List<uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing> getHearing() {
        uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing hearing =
            new uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing("outcome",
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
        Subscriptions subscriptions = caseDataBuilder.buildSubscriptions(Optional.empty(), null);
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
            .phone2("07123456789")
            .roleId(3)
            .build();
        Subscriptions subscriptions = caseDataBuilder.buildSubscriptions(Optional.of(party), null);
        assertNotNull("AppellantSubscription is null", subscriptions.getAppellantSubscription());
        assertNotNull("Representative Subscription is null", subscriptions.getRepresentativeSubscription());
        String appealNumber = subscriptions.getRepresentativeSubscription().getTya();
        assertTrue("appealNumber is empty", !"".equals(appealNumber));
        assertEquals("appealNumber length is not 10 digits", 10, appealNumber.length());
        assertEquals("email is not " + party.getEmail(),
            party.getEmail(), subscriptions.getRepresentativeSubscription().getEmail());
        assertEquals("mobile number is not " + party.getPhone1(),
            party.getPhone2(), subscriptions.getRepresentativeSubscription().getMobile());
        assertEquals("email should be un-subscribed", "No",
            subscriptions.getRepresentativeSubscription().getSubscribeEmail());
        assertEquals("sms should be un-subscribed", "No",
            subscriptions.getRepresentativeSubscription().getSubscribeSms());
    }

    @Test
    @Parameters({"invalid,", "07123456789,07123456789"})
    public void givenInvalidMobile_shouldFallbackToEmptyString(String mobileNumber, String expectedMobileNumber) {
        Parties party = Parties.builder()
            .email("my@email.com")
            .phone2(mobileNumber)
            .roleId(3)
            .build();

        Subscriptions subscriptions = caseDataBuilder.buildSubscriptions(Optional.of(party), null);

        assertEquals(expectedMobileNumber, subscriptions.getRepresentativeSubscription().getMobile());
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
    public void givenHearingInDeltaWhenBuildingHearingThenHearingIdIsBuilt() {
        when(refDataService.getVenueDetails("venue")).thenReturn(VenueDetails.builder()
            .venName("name")
            .build());

        List<Hearing> hearingList = caseDataBuilder.buildHearings(appeal);

        assertEquals("id", hearingList.get(0).getValue().getHearingId());
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

}
