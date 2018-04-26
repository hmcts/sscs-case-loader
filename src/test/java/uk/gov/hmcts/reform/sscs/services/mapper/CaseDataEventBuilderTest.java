package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;

public class CaseDataEventBuilderTest extends CaseDataBuilderBaseTest {

    @Test
    public void givenMinorStatusIsPresentInTheXmlCaseWhenBuildPostponedEventIsCalledThenAPostponedEventIsCreated() {
        CaseDataEventBuilder caseDataEventBuilder = new CaseDataEventBuilder();
        List<Events> events = caseDataEventBuilder.buildPostponedEvent(super.getAppeal());
        assertTrue("event is not of type Postponed",
            events.get(0).getValue().getType().equals(GapsEvent.HEARING_POSTPONED.getType()));

        LocalDateTime actualDateEvent = LocalDateTime.parse(events.get(0).getValue().getDate());
        assertTrue("event date does not matches minor status date_set field",
            actualDateEvent.isEqual(ZonedDateTime.parse(TEST_DATE).toLocalDateTime()));
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
