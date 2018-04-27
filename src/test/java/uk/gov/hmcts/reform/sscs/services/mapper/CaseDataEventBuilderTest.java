package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;

@RunWith(JUnitParamsRunner.class)
public class CaseDataEventBuilderTest extends CaseDataBuilderBaseTest {

    private final CaseDataEventBuilder caseDataEventBuilder = new CaseDataEventBuilder();
    private List<Events> events;

    @Test
    public void givenMinorStatusWithId26ThenAPostponedEventIsCreated() {
        AppealCase appealCaseWithMinorStatusId26 = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(super.buildMajorStatusGivenStatuses(GapsEvent.APPEAL_RECEIVED))
            .minorStatus(super.getMinorStatusId26(TEST_DATE2))
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(
            appealCaseWithMinorStatusId26);

        assertTrue("event is not of type Postponed",
            events.get(0).getValue().getType().equals(GapsEvent.HEARING_POSTPONED.getType()));

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
            .majorStatus(super.buildMajorStatusGivenStatuses(GapsEvent.APPEAL_RECEIVED, GapsEvent.HEARING_POSTPONED))
            .minorStatus(super.getMinorStatusId26(TEST_DATE2))
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithPostponedAndMinorStatusWithSameDated);

        assertTrue("Postponed event should not be created here", events.isEmpty());
    }

    @Test
    public void givenAnExistingPostponedEventAndMinorStatusId26WithDifferentDatesThenANewPostponedEventIsCreated() {
        AppealCase appealWithPostponedAndMinorStatusWithDifferentDates = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(super.buildMajorStatusGivenStatuses(GapsEvent.APPEAL_RECEIVED, GapsEvent.HEARING_POSTPONED))
            .minorStatus(super.getMinorStatusId26(TEST_DATE))
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithPostponedAndMinorStatusWithDifferentDates);

        assertTrue("Postponed event should be created here", events.size() == 1);

        Event actualEvent = events.get(0).getValue();
        assertTrue("event is not Postponed", actualEvent.getType().equals(
            GapsEvent.HEARING_POSTPONED.getType()));

        String existingPostponedDate = ZonedDateTime.parse(TEST_DATE2).toLocalDateTime().toString();
        assertFalse("new postponed date cannot be equal to the existing postpone date",
            actualEvent.getDate().equals(existingPostponedDate));

        String expectedPostponedDate = appealWithPostponedAndMinorStatusWithDifferentDates
            .getMinorStatus().get(0).getDateSet().toLocalDateTime().toString();
        assertTrue("new postponed event date is not equal to the minor status date",
            actualEvent.getDate().equals(expectedPostponedDate));
    }

    @Test
    @Parameters(method = "getMinotStatusListParameters")
    public void whenMinorStatusIsNullOrEmptyThenPostponedEventIsNotCreated(List<MinorStatus> minorStatus) {
        AppealCase appealWithMinorStatusNull = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(super.buildMajorStatusGivenStatuses(GapsEvent.APPEAL_RECEIVED, GapsEvent.HEARING_POSTPONED))
            .minorStatus(minorStatus)
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithMinorStatusNull);

        assertTrue("No Postponed event should be created here", events.isEmpty());

    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object getMinotStatusListParameters() {
        return new Object[]{new Object[]{null}, new Object[]{Collections.emptyList()}};
    }

}
