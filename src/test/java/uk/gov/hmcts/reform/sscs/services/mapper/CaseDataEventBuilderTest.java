package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;

@RunWith(JUnitParamsRunner.class)
public class CaseDataEventBuilderTest extends CaseDataBuilderBaseTest {

    private static final String SESSION_DATE = "2017-05-23T00:00:00+01:00";
    private final CaseDataEventBuilder caseDataEventBuilder = new CaseDataEventBuilder();
    private List<Events> events;

    @Test
    public void givenMinorStatusWithId26ThenAPostponedEventIsCreated() {
        AppealCase appealCaseWithMinorStatusId26 = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), TEST_DATE)))
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("26", TEST_DATE2)))
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(
            appealCaseWithMinorStatusId26);

        assertEquals("event is not of type Postponed", events.get(0).getValue().getType(),
            GapsEvent.HEARING_POSTPONED.getType());

        LocalDateTime actualDateEvent = LocalDateTime.parse(events.get(0).getValue().getDate());
        LocalDateTime expectedDate = appealCaseWithMinorStatusId26.getMinorStatus().get(0).getDateSet()
            .toLocalDateTime();
        assertTrue("event date does not match minor status date_set field",
            actualDateEvent.isEqual(expectedDate));
    }

    @Test
    public void givenAnExistingPostponedEventAndMinorStatusId26WithSameDatesThenNoNewPostponedEventIsCreated() {
        AppealCase appealWithPostponedAndMinorStatusWithSameDated = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Arrays.asList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), TEST_DATE),
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.HEARING_POSTPONED.getStatus(), TEST_DATE2)
            ))
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("26", TEST_DATE2)))
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithPostponedAndMinorStatusWithSameDated);

        assertTrue("Postponed event should not be created here", events.isEmpty());
    }

    @Test
    public void givenAnExistingPostponedEventAndMinorStatusId26WithDifferentDatesThenANewPostponedEventIsCreated() {
        AppealCase appealWithPostponedAndMinorStatusWithDifferentDates = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Arrays.asList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), TEST_DATE),
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.HEARING_POSTPONED.getStatus(), TEST_DATE2)
            ))
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("26", TEST_DATE)))
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithPostponedAndMinorStatusWithDifferentDates);

        assertEquals("Postponed event should be created here", 1, events.size());

        Event actualEvent = events.get(0).getValue();
        assertEquals("event is not Postponed", actualEvent.getType(), GapsEvent.HEARING_POSTPONED.getType());

        String existingPostponedDate = ZonedDateTime.parse(TEST_DATE2).toLocalDateTime().toString();
        assertNotEquals("new postponed date cannot be equal to the existing postpone date",
            actualEvent.getDate(), existingPostponedDate);

        String expectedPostponedDate = appealWithPostponedAndMinorStatusWithDifferentDates
            .getMinorStatus().get(0).getDateSet().toLocalDateTime().toString();
        assertEquals("new postponed event date is not equal to the minor status date",
            actualEvent.getDate(), expectedPostponedDate);
    }

    @Test
    @Parameters(method = "getMinotStatusListParameters")
    public void whenMinorStatusIsNullOrEmptyThenPostponedEventIsNotCreated(List<MinorStatus> minorStatus) {
        AppealCase appealWithMinorStatusNull = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Arrays.asList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), TEST_DATE),
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.HEARING_POSTPONED.getStatus(), TEST_DATE2)
            ))
            .minorStatus(minorStatus)
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithMinorStatusNull);

        assertTrue("No Postponed event should be created here", events.isEmpty());

    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object getMinotStatusListParameters() {
        return new Object[]{new Object[]{null}, new Object[]{Collections.emptyList()}};
    }

    @Test
    public void givenTwoMinorStatusesArePresentThenTwoNewPostponedEventAreCreated() {
        AppealCase appealWithTwoMinorStatusesAndNoPostponed = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), TEST_DATE)
            ))
            .minorStatus(Arrays.asList(
                buildMinorStatusGivenIdAndDate("26", TEST_DATE),
                buildMinorStatusGivenIdAndDate("26", TEST_DATE2)))
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithTwoMinorStatusesAndNoPostponed);

        assertEquals("Two new postponed event should be created here", 2, events.size());
        LocalDateTime actualEvenDate = LocalDateTime.parse(events.get(0).getValue().getDate());
        LocalDateTime expectedEventDate = ZonedDateTime.parse(TEST_DATE).toLocalDateTime();
        assertEquals("", actualEvenDate, expectedEventDate);
    }

    @Test
    public void givenTwoMinorStatusAndOneExistingPostponedEventArePresentThenOnlyOneNewPostponedEventIsCreated() {
        AppealCase appealWithTwoMinorStatusesAndNoPostponed = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Arrays.asList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), TEST_DATE),
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.HEARING_POSTPONED.getStatus(), TEST_DATE2)
            ))
            .minorStatus(Arrays.asList(
                buildMinorStatusGivenIdAndDate("26", TEST_DATE),
                buildMinorStatusGivenIdAndDate("26", TEST_DATE2)))
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithTwoMinorStatusesAndNoPostponed);

        assertEquals("Only One new postponed event should be created here", 1, events.size());
        LocalDateTime actualEvenDate = LocalDateTime.parse(events.get(0).getValue().getDate());
        LocalDateTime expectedEventDate = ZonedDateTime.parse(TEST_DATE).toLocalDateTime();
        assertEquals("", actualEvenDate, expectedEventDate);
    }

    @Test
    public void givenTwoMinorStatusWithTheSameDateAnoNoPostponedPresentThenOnlyOneNewPostponedIsCreated() {
        AppealCase appealWithTwoMinorStatusesAndNoPostponed = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), TEST_DATE)
            ))
            .minorStatus(Arrays.asList(
                buildMinorStatusGivenIdAndDate("26", TEST_DATE),
                buildMinorStatusGivenIdAndDate("26", TEST_DATE)))
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithTwoMinorStatusesAndNoPostponed);

        assertEquals("Only one postponed should be created here", 1, events.size());
    }

    @Test
    public void shouldReturnAdjournedEventIfHearingOutComeIdIs110() {
        ArrayList<Hearing> hearings = new ArrayList<>();
        Hearing hearing = Hearing.builder().sessionDate(SESSION_DATE).outcomeId("110").build();
        hearings.add(hearing);
        AppealCase appealCase = AppealCase.builder().hearing(hearings).build();

        List<Events> events = caseDataEventBuilder.buildAdjournedEvents(appealCase);

        assertThat(events.size(), equalTo(1));
        Event event = events.get(0).getValue();

        assertThat(event.getType(), equalTo(GapsEvent.HEARING_ADJOURNED.getType()));
        assertThat(event.getDescription(), equalTo(GapsEvent.HEARING_ADJOURNED.getDescription()));
        assertThat(event.getDate(), equalTo(SESSION_DATE));

    }

    @Test
    public void shouldReturnAdjournedEventIfHearingOutComeIdIs126() {
        ArrayList<Hearing> hearings = new ArrayList<>();
        Hearing hearing = Hearing.builder().sessionDate(SESSION_DATE).outcomeId("126").build();
        hearings.add(hearing);
        AppealCase appealCase = AppealCase.builder().hearing(hearings).build();

        List<Events> events = caseDataEventBuilder.buildAdjournedEvents(appealCase);

        assertThat(events.size(), equalTo(1));
        Event event = events.get(0).getValue();

        assertThat(event.getType(), equalTo(GapsEvent.HEARING_ADJOURNED.getType()));
        assertThat(event.getDescription(), equalTo(GapsEvent.HEARING_ADJOURNED.getDescription()));
        assertThat(event.getDate(), equalTo(SESSION_DATE));

    }

    @Test
    @Parameters({"110", "111", "112", "113", "114", "115", "116", "117", "118", "119", "120", "121", "122",
        "123", "124", "125", "126"})
    public void shouldReturnAdjournedEventIfHearingOutComeIdIsInTheRange110To126(String outcomeId) {
        ArrayList<Hearing> hearings = new ArrayList<>();
        Hearing hearing = Hearing.builder().sessionDate(SESSION_DATE).outcomeId(outcomeId).build();
        hearings.add(hearing);
        AppealCase appealCase = AppealCase.builder().hearing(hearings).build();

        List<Events> events = caseDataEventBuilder.buildAdjournedEvents(appealCase);

        assertThat(events.size(), equalTo(1));
        Event event = events.get(0).getValue();

        assertThat(event.getType(), equalTo(GapsEvent.HEARING_ADJOURNED.getType()));
        assertThat(event.getDescription(), equalTo(GapsEvent.HEARING_ADJOURNED.getDescription()));
        assertThat(event.getDate(), equalTo(SESSION_DATE));
    }

    @Test
    @Parameters({"109", "127"})
    public void shouldNotReturnAdjournedEventIfHearingOutComeIdIsLessThan110OrGreaterThan126(String outcomeId) {
        ArrayList<Hearing> hearings = new ArrayList<>();
        Hearing hearing = Hearing.builder().sessionDate(SESSION_DATE).outcomeId(outcomeId).build();
        hearings.add(hearing);
        AppealCase appealCase = AppealCase.builder().hearing(hearings).build();

        List<Events> events = caseDataEventBuilder.buildAdjournedEvents(appealCase);

        assertThat(events.size(), equalTo(0));
    }

    @Test
    public void shouldReturnEmptyListIfThereAreNoHearing() {
        AppealCase appealCase = AppealCase.builder().build();

        List<Events> events = caseDataEventBuilder.buildAdjournedEvents(appealCase);

        assertThat(events, equalTo(Collections.EMPTY_LIST));
    }

    @Test
    public void shouldIgnoreHearingsWithNoOutComeIdWhenCreatingAdjournedEvents() {
        ArrayList<Hearing> hearings = new ArrayList<>();
        Hearing hearing1 = Hearing.builder().sessionDate(SESSION_DATE).outcomeId("110").build();
        Hearing hearing2 = Hearing.builder().sessionDate(SESSION_DATE).build();
        hearings.add(hearing1);
        hearings.add(hearing2);
        AppealCase appealCase = AppealCase.builder().hearing(hearings).build();

        List<Events> events = caseDataEventBuilder.buildAdjournedEvents(appealCase);

        assertThat(events.size(), equalTo(1));

    }
}
