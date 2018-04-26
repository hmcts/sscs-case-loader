package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CaseDataBuilderTest {

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @Autowired
    private CaseDataBuilder caseDataBuilder;
    private AppealCase appealCaseWithMinorAndMajorStatuses;

    private static final String TEST_DATE = "2018-05-24T00:00:00+01:00";
    private static final String TEST_DATE2 = "2017-05-24T00:00:00+01:00";

    @Before
    public void setUp() {
        appealCaseWithMinorAndMajorStatuses = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(getStatus())
            .hearing(getHearing())
            .minorStatus(Collections.singletonList(
                new MinorStatus("", "26", ZonedDateTime.parse(TEST_DATE))))
            .build();
    }

    @Test
    @Ignore
    public void givenAppealCaseHasAMinorStatusWithId26ThenAPostponedEvenIsCreatedIfItDoesNotExist() {

    }

    @Test
    public void whenBuildEventMethodIsCalledThenItReturnsAnEventListSortedByDateInDescOrder() {
        List<Events> events = caseDataBuilder.buildEvent(appealCaseWithMinorAndMajorStatuses);
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

    private List<MajorStatus> getStatus() {
        MajorStatus status = new MajorStatus("", "3", "", ZonedDateTime.parse(TEST_DATE2));
        return newArrayList(status);
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
