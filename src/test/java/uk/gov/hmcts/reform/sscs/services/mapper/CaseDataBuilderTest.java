package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscs.services.mapper.CaseDataBuilder.NO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.service.RegionalProcessingCenterService;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
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
        Subscriptions subscriptions = caseDataBuilder.buildSubscriptions();
        assertNotNull("AppellantSubscription is null", subscriptions.getAppellantSubscription());
        assertNotNull("RepresentativeSubscription is null", subscriptions.getRepresentativeSubscription());
        String appealNumber = subscriptions.getAppellantSubscription().getTya();
        assertTrue("appealNumber is empty", !"".equals(appealNumber));
        assertEquals("appealNumber length is not 10 digits", 10, appealNumber.length());
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
