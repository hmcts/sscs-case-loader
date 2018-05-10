package uk.gov.hmcts.reform.sscs.services.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;
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
    public void whenBuildEventMethodIsCalledThenItReturnsAnEventListSortedByDateInDescOrder() {
        AppealCase appealCaseWithMinorStatusId27AndPostponedRequestAndMajorStatuses = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), APPEAL_RECEIVED_DATE)
            ))
            .hearing(getHearing())
            .minorStatus(Collections.singletonList(
                buildMinorStatusGivenIdAndDate("27", MINOR_STATUS_ID_27_DATE)
            ))
            .postponementRequests(Collections.singletonList(
                new PostponementRequests(
                    "Y", null, null, null)
            ))
            .build();

        events = caseDataBuilder.buildEvent(appealCaseWithMinorStatusId27AndPostponedRequestAndMajorStatuses);

        assertTrue("events size only has 1 element", events.size() > 1);
        Event actualMostRecentEvent = events.get(0).getValue();
        assertEquals("expected most recent Event is wrong",
            actualMostRecentEvent.getType(), GapsEvent.HEARING_POSTPONED.getType());
    }

    @Test
    public void givenThatThereIsAOutComeIdInHearingAndItsInRangeOf110To126ThenAAdjournedEventShouldBeCreated() {
        AppealCase appealCase = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), APPEAL_RECEIVED_DATE)
            ))
            .hearing(newArrayList(Hearing.builder().outcomeId("110").sessionDate("2017-05-27T00:00:00+01:00").build()))
            .build();

        events = caseDataBuilder.buildEvent(appealCase);

        assertEquals(events.get(0).getValue().getType(), GapsEvent.HEARING_ADJOURNED.getType());
    }

    @Test
    public void givenThatThereIsAOutComeIdInHearingAndItsInRangeOf12o16ThenPostponedEventShouldBeCreated() {
        AppealCase appealCase = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), APPEAL_RECEIVED_DATE)
            ))
            .hearing(newArrayList(Hearing.builder().outcomeId("12").sessionDate("2017-05-27T00:00:00+01:00").build()))
            .build();

        events = caseDataBuilder.buildEvent(appealCase);

        assertTrue(events.get(0).getValue().getType().equals(GapsEvent.HEARING_POSTPONED.getType()));

    }

    private List<Hearing> getHearing() {
        Hearing hearing = new Hearing("1",
            "venue",
            "outcomeDate",
            "notificationDate",
            "2017-05-24T00:00:00+01:00",
            "2017-05-24T10:30:00+01:00",
            "id");
        return newArrayList(hearing);
    }

}
