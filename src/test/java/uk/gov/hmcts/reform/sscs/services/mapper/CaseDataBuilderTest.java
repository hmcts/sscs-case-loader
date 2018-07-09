package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscs.services.mapper.CaseDataBuilder.NO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.refdata.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.BenefitType;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Hearing;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;
import uk.gov.hmcts.reform.sscs.services.refdata.RegionalProcessingCenterService;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataBuilderTest extends CaseDataBuilderBase {

    @Mock
    private ReferenceDataService refDataService;
    @Mock
    private CaseDataEventBuilder caseDataEventBuilder;
    @Mock
    private RegionalProcessingCenterService regionalProcessingCentreService;
    private CaseDataBuilder caseDataBuilder;
    private AppealCase appeal;

    @Before
    public void setUp() {
        createAppealCase(getHearing());
    }

    private void createAppealCase(List<uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing> hearings) {
        appeal = AppealCase.builder()
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
        assertNotNull("SupporterSubscription is null", subscriptions.getSupporterSubscription());
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
    public void shouldBuildRegionalProcessingCentre() {
        RegionalProcessingCenter expectedRegionalProcessingCentre = RegionalProcessingCenter.builder().build();
        when(regionalProcessingCentreService.getByVenueId("venue")).thenReturn(expectedRegionalProcessingCentre);
        RegionalProcessingCenter regionalProcessingCentre = caseDataBuilder.buildRegionalProcessingCentre(appeal);

        assertThat(regionalProcessingCentre, is(expectedRegionalProcessingCentre));
    }

    @Test
    public void shouldReturnNullRemoteProcessingCentreIfNoHearings() {
        createAppealCase(null);
        RegionalProcessingCenter regionalProcessingCentre = caseDataBuilder.buildRegionalProcessingCentre(appeal);

        assertThat(regionalProcessingCentre, is(nullValue()));
    }

    @Test
    public void shouldBuildRegionalProcessingCentreFromLatestHearing() {
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

        RegionalProcessingCenter expectedRegionalProcessingCentre = RegionalProcessingCenter.builder().build();
        when(regionalProcessingCentreService.getByVenueId("venue2")).thenReturn(expectedRegionalProcessingCentre);
        RegionalProcessingCenter regionalProcessingCentre = caseDataBuilder.buildRegionalProcessingCentre(appeal);

        assertThat(regionalProcessingCentre, is(expectedRegionalProcessingCentre));
    }
}
