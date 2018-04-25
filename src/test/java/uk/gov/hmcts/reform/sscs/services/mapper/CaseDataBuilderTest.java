package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.services.mapper.CaseDataBuilder.NO;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.BenefitType;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Hearing;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataBuilderTest {

    private static final String TEST_DATE = "2018-05-24T00:00:00+01:00";
    private static final String TEST_DATE2 = "2017-05-24T00:00:00+01:00";
    @Mock
    private ReferenceDataService refDataService;
    private AppealCase appeal;
    private CaseDataBuilder caseDataBuilder;
    private List<Events> events;

    @Before
    public void setUp() {
        CaseDataEventBuilder caseDataEventBuilder = new CaseDataEventBuilder();
        caseDataBuilder = new CaseDataBuilder(refDataService, caseDataEventBuilder);
        appeal = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(getStatus())
            .hearing(getHearing())
            .minorStatus(Collections.singletonList(
                new MinorStatus("", "26", ZonedDateTime.parse(TEST_DATE))))
            .build();
    }

    private List<MajorStatus> getStatus() {
        MajorStatus status = new MajorStatus("", "3", "", ZonedDateTime.parse(TEST_DATE2));
        return newArrayList(status);
    }

    private List<uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing> getHearing() {
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
        assertTrue("appealNumber length is not 10 digits", appealNumber.length() == 10);
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

    //fixme Move this test to the integrating test because it is testing other dependencies.
    @Test
    public void givenMinorStatusIsPresentInTheXmlCaseWhenBuildEventIsCalledThenAPostponedEventIsCreated() {
        events = caseDataBuilder.buildEvent(appeal);
        assertTrue("event is not of type Postponed",
            events.get(0).getValue().getType().equals(GapsEvent.HEARING_POSTPONED.getType()));

        LocalDateTime actualDateEvent = LocalDateTime.parse(events.get(0).getValue().getDate());
        assertTrue("event date does not matches minor status date_set field",
            actualDateEvent.isEqual(ZonedDateTime.parse(TEST_DATE).toLocalDateTime()));
    }

    @Test
    public void whenBuildEventMethodIsCalledThenItReturnsAnEventListSortedByDateInDescOrder() {
        events = caseDataBuilder.buildEvent(appeal);
        assertTrue("events size only has 1 element", events.size() > 1);
        Event actualMostRecentEvent = events.get(0).getValue();
        assertTrue("expected most recent Event is wrong",
            actualMostRecentEvent.getType().equals(GapsEvent.HEARING_POSTPONED.getType()));
    }


    @Test
    @Ignore
    public void givenAFewMinorStatuesShouldCreatePostponedEventFromTheLatestMinorStatus() {

    }

    @Test
    @Ignore
    public void givenAMinorStatusShouldCreatePostponedEventIfItDoesNotExistAlready() {

    }

}
