package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;

public class CaseDataEventBuilderTest extends CaseDataBuilderBaseTest {

    private final CaseDataEventBuilder caseDataEventBuilder = new CaseDataEventBuilder();
    private List<Events> events;

    @Test
    public void givenMinorStatusWithId26ThenAPostponedEventIsCreated() {
        AppealCase appealCaseWithMinorStatusId26 = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(super.getAppealReceivedStatus())
            .minorStatus(getMinorStatusId26())
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
    public void givenAPostponedEventIsAlreadyPresentThenAnotherPostponedWithSameDateIsNotCreated() {
        AppealCase appealWithPostponedEventAndMinorStatus = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(getPostponedAndOtherMajorStatuses())
            .minorStatus(getMinorStatusId26())
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithPostponedEventAndMinorStatus);

        assertTrue("Postponed event should not be created here", events.isEmpty());
    }

    private List<MinorStatus> getMinorStatusId26() {
        return Collections.singletonList(
            new MinorStatus("", "26", ZonedDateTime.parse(TEST_DATE2)));
    }

    private List<MajorStatus> getPostponedAndOtherMajorStatuses() {
        MajorStatus appealReceivedStatus = new MajorStatus("", GapsEvent.APPEAL_RECEIVED.getStatus(),
            "", ZonedDateTime.parse(TEST_DATE));
        MajorStatus postponedStatus = new MajorStatus("", GapsEvent.HEARING_POSTPONED.getStatus(),
            "", ZonedDateTime.parse(TEST_DATE2));
        return newArrayList(appealReceivedStatus, postponedStatus);
    }

    @Test
    @Ignore
    public void whenMinorStatusIsNullThenPostponedEventIsNotCreated() {

    }

    @Test
    @Ignore
    public void whenMinorStatusIsEmptyThenPostponedEventIsNotCreated() {

    }
}
