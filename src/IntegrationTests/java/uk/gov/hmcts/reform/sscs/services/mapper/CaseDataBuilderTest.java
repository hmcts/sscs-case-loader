package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CaseDataBuilderTest extends CaseDataBuilderBaseTest {

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @Autowired
    private CaseDataBuilder caseDataBuilder;
    private List<Events> events;

    @Test
    public void givenAppealCaseHasAMinorStatusWithId26ThenAPostponedEventIsCreated() {
        AppealCase appealCaseWithMinorStatusId26AndMajorStatuses = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), TEST_DATE)
            ))
            .hearing(getHearing())
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("26", TEST_DATE2)
            ))
            .build();

        events = caseDataBuilder.buildEvent(appealCaseWithMinorStatusId26AndMajorStatuses);

        assertTrue("size of Events should be 2", events.size() == 2);
        assertTrue("Latest event should be the Postponed one here", events.get(0).getValue().getType()
            .equals(GapsEvent.HEARING_POSTPONED.getType()));
    }

    @Test
    public void givenAppealCaseWithMinorStatusId26AndPostponedEventWithSameDatesThenNoPostponedEventIsCreated() {
        AppealCase appealCaseWithMinorStatusId26AndPostponedWithSameDates = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Arrays.asList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), TEST_DATE),
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.HEARING_POSTPONED.getStatus(), TEST_DATE2)
            ))
            .hearing(getHearing())
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("26", TEST_DATE2)
            ))
            .build();

        events = caseDataBuilder.buildEvent(appealCaseWithMinorStatusId26AndPostponedWithSameDates);
        System.out.println(events);

        assertTrue("Events size should be 2 here", events.size() == 2);
        int actualNumberOfPostponedEventsWithSameDate = events.stream()
            .filter(event -> event.getValue().getType().equals(GapsEvent.HEARING_POSTPONED.getType()))
            .filter(event -> event.getValue().getDate().equals(
                ZonedDateTime.parse(TEST_DATE2).toLocalDateTime().toString()))
            .collect(Collectors.toList())
            .size();
        assertTrue("Only one postponed event with same minor status date expected here",
            actualNumberOfPostponedEventsWithSameDate == 1);
    }

    @Test
    public void whenBuildEventMethodIsCalledThenItReturnsAnEventListSortedByDateInDescOrder() {
        AppealCase appealCaseWithMinorStatusId26AndMajorStatuses = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), TEST_DATE)
            ))
            .hearing(getHearing())
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("26", TEST_DATE2)
            ))
            .build();

        events = caseDataBuilder.buildEvent(appealCaseWithMinorStatusId26AndMajorStatuses);

        assertTrue("events size only has 1 element", events.size() > 1);
        Event actualMostRecentEvent = events.get(0).getValue();
        assertTrue("expected most recent Event is wrong",
            actualMostRecentEvent.getType().equals(GapsEvent.HEARING_POSTPONED.getType()));
    }

    @Test
    public void givenAFewMinorStatuesShouldCreatePostponedEventFromTheLatestMinorStatus() {
        AppealCase appealCaseWithTwoMinorStatusId26WithDifferentDates = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), TEST_DATE)
            ))
            .hearing(getHearing())
            .minorStatus(Arrays.asList(
                super.buildMinorStatusGivenIdAndDate("26", TEST_DATE),
                super.buildMinorStatusGivenIdAndDate("26", TEST_DATE2)
            ))
            .build();

        events = caseDataBuilder.buildEvent(appealCaseWithTwoMinorStatusId26WithDifferentDates);

        assertTrue("latest event expected here is postponed",
            events.get(0).getValue().getType().equals(GapsEvent.HEARING_POSTPONED.getType()));
    }

    private List<Hearing> getHearing() {
        Hearing hearing = new Hearing("outcome",
            "venue",
            "outcomeDate",
            "notificationDate",
            "2017-05-24T00:00:00+01:00",
            "2017-05-24T10:30:00+01:00",
            "id");
        return newArrayList(hearing);
    }

}
