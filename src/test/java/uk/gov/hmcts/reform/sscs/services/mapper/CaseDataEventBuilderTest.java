package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
        AppealCase appealCaseWithMinorStatusId26AndSomeMajorStatuses = super.getAppeal();

        events = caseDataEventBuilder.buildPostponedEvent(
            appealCaseWithMinorStatusId26AndSomeMajorStatuses);

        assertTrue("event is not of type Postponed",
            events.get(0).getValue().getType().equals(GapsEvent.HEARING_POSTPONED.getType()));

        LocalDateTime actualDateEvent = LocalDateTime.parse(events.get(0).getValue().getDate());
        assertTrue("event date does not match minor status date_set field",
            actualDateEvent.isEqual(ZonedDateTime.parse(TEST_DATE2).toLocalDateTime()));
    }

    @Test
    @Ignore
    public void givenAPostponedEventIsAlreadyPresentThenAnotherPostponedWithSameDateIsNotCreated() {
        AppealCase appealWithPostponedAndMinorStatus = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(getStatus())
            .hearing(super.getHearing())
            .minorStatus(getMinorStatus())
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithPostponedAndMinorStatus);

        List<Events> postponedEvents = events.stream()
            .filter(event -> event.getValue().getType().equals(GapsEvent.HEARING_POSTPONED.getType()))
            .collect(Collectors.toList());

        assertTrue("Same Postponed event created", postponedEvents.size() == 1);

        String postponedDate = postponedEvents.get(0).getValue().getDate();
        ZonedDateTime minorStatusId26Date = appealWithPostponedAndMinorStatus.getMinorStatus().get(0).getDateSet();
        assertTrue(postponedDate.equals(minorStatusId26Date));
    }

    private List<MinorStatus> getMinorStatus() {
        return Collections.singletonList(
            new MinorStatus("", "26", ZonedDateTime.parse(TEST_DATE2)));
    }

    public List<MajorStatus> getStatus() {
        MajorStatus appealReceivedStatus = new MajorStatus("", "3", "",
            ZonedDateTime.parse(TEST_DATE));
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
