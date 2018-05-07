package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.hmcts.reform.sscs.CaseDetailsUtils;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCcdService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@RunWith(JUnitParamsRunner.class)
public class CaseDataEventBuilderTest extends CaseDataBuilderBaseTest {

    private static final String SESSION_DATE = "2017-05-23T00:00:00+01:00";
    private static final String LOCAL_SESSION_DATE = "2017-05-23T00:00:00";
    private static final String CASE_DETAILS_WITH_HEARINGS_JSON = "CaseDetailsWithHearings.json";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private IdamService idamService;
    @Mock
    private SearchCcdService searchCcdService;
    @Mock
    private PostponedEventService<Hearing> postponedEventInferredFromDelta;
    private CaseDataEventBuilder caseDataEventBuilder;
    private List<Events> events;

    @Before
    public void setUp() {
        when(idamService.getIdamOauth2Token()).thenReturn("oauth2Token");
        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuthorizationToken");

        caseDataEventBuilder = new CaseDataEventBuilder(searchCcdService, idamService, postponedEventInferredFromDelta);
    }

    @Test
    @Parameters(method = "getMinotStatusListParameters")
    public void whenMinorStatusIsNullOrEmptyThenPostponedEventIsNotCreated(List<MinorStatus> minorStatus) {
        AppealCase appealWithMinorStatusNull = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Arrays.asList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), APPEAL_RECEIVED_DATE),
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.HEARING_POSTPONED.getStatus(),
                    HEARING_POSTPONED_DATE)
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
        assertThat(event.getDate(), equalTo(LOCAL_SESSION_DATE));
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
        assertThat(event.getDate(), equalTo(LOCAL_SESSION_DATE));
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
        assertThat(event.getDate(), equalTo(LOCAL_SESSION_DATE));
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

    @Test
    public void givenMinorStatusId27AndOneSinglePostponedWithGrantedYesThenNewPostponedIsCreated() {
        AppealCase appealWithMinorStatusId27AndPostponedGrantedY = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), APPEAL_RECEIVED_DATE)
            ))
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("27", MINOR_STATUS_ID_27_DATE)))
            .postponementRequests(Collections.singletonList(
                new PostponementRequests(
                    "Y", null, null, null)
            ))
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithMinorStatusId27AndPostponedGrantedY);

        assertEquals("Events size expected is 1 here", 1, events.size());
        assertEquals("Postponed event is expected here", events.get(0).getValue().getType(),
            GapsEvent.HEARING_POSTPONED.getType());
    }

    @Test
    public void givenMinorStatusId27AndOneSinglePostponedRequestWithGrantedNoThenNoPostponedIsCreated() {
        AppealCase appealWithMinorStatusId27AndPostponedGrantedY = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), APPEAL_RECEIVED_DATE)
            ))
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("27", MINOR_STATUS_ID_27_DATE)))
            .postponementRequests(Collections.singletonList(
                new PostponementRequests(
                    "N", null, null, null)
            ))
            .build();

        events = caseDataEventBuilder.buildPostponedEvent(appealWithMinorStatusId27AndPostponedGrantedY);

        assertTrue("Events size expected is empty here", events.isEmpty());
    }

    /*
        scenario1:
        Given minor status with id 27
        And multiple hearing objects
        And two postponed request elements with the granted field to 'Y'
        And none of them matching the hearing id field neither in Delta or in CCD
        Then NO postponed element is created
     */
    @Test
    public void givenScenario1ThenNoPostponedEventIsNotCreated() throws Exception {
        AppealCase appeal = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), APPEAL_RECEIVED_DATE)
            ))
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("27", MINOR_STATUS_ID_27_DATE)))
            .hearing(Arrays.asList(
                Hearing.builder().hearingId("1").build(),
                Hearing.builder().hearingId("2").build()
            ))
            .postponementRequests(Arrays.asList(
                new PostponementRequests(
                    "Y", "3", null, null),
                new PostponementRequests(
                    "Y", "4", null, null)
            ))
            .build();

        when(searchCcdService.findCaseByCaseRef(anyString(), Matchers.any(IdamTokens.class)))
            .thenReturn(Collections.singletonList(CaseDetailsUtils.getCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON)));

        events = caseDataEventBuilder.buildPostponedEvent(appeal);

        assertTrue("No postponed event expected here", events.isEmpty());
    }

    /*
        scenario2:
        Given minor status with id 27
        And multiple hearing objects
        And two postponed request elements with the granted field to 'Y'
        And one of them matching the hearing id field to the hearing in the Delta
        Then one postponed element is created
     */
    @Test
    public void givenScenario2ThenPostponedIsCreated() {
        AppealCase appeal = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), APPEAL_RECEIVED_DATE)
            ))
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("27", MINOR_STATUS_ID_27_DATE)))
            .hearing(Arrays.asList(
                Hearing.builder().hearingId("1").build(),
                Hearing.builder().hearingId("2").build()
            ))
            .postponementRequests(Arrays.asList(
                new PostponementRequests(
                    "Y", "1", null, null),
                new PostponementRequests(
                    "Y", "", null, null)
            ))
            .build();

        when(postponedEventInferredFromDelta.matchToHearingId(eq(appeal.getPostponementRequests()),
            eq(appeal.getHearing()))).thenReturn(true);

        events = caseDataEventBuilder.buildPostponedEvent(appeal);

        assertEquals("One postponed event expected here", 1, events.size());
        assertEquals("type expected is postponed", GapsEvent.HEARING_POSTPONED.getType(),
            events.get(0).getValue().getType());
        LocalDateTime actualPostponedDate = LocalDateTime.parse(events.get(0).getValue().getDate());
        LocalDateTime expectedDate = ZonedDateTime.parse(MINOR_STATUS_ID_27_DATE).toLocalDateTime();
        assertEquals(expectedDate, actualPostponedDate);
    }

    /*
        scenario3:
        Given minor status with id 27
        And multiple hearing objects
        And two postponed request elements with the granted field to 'Y'
        And one of them matching the hearing id field to the hearing in the existing Case in CDD
        Then one postponed element is created
     */
    @Test
    @Parameters({"6, ", "7, ", ", 6", ", 7"})
    public void givenScenario3ThenPostponedIsCreated(String appealHearingId1, String appealHearingId2)
        throws Exception {
        AppealCase appeal = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .majorStatus(Collections.singletonList(
                super.buildMajorStatusGivenStatusAndDate(GapsEvent.APPEAL_RECEIVED.getStatus(), APPEAL_RECEIVED_DATE)
            ))
            .minorStatus(Collections.singletonList(
                super.buildMinorStatusGivenIdAndDate("27", MINOR_STATUS_ID_27_DATE)))
            .hearing(Arrays.asList(
                Hearing.builder().hearingId("1").build(),
                Hearing.builder().hearingId("2").build()
            ))
            .postponementRequests(Arrays.asList(
                new PostponementRequests(
                    "Y", appealHearingId1, null, null),
                new PostponementRequests(
                    "Y", appealHearingId2, null, null)
            ))
            .build();

        when(searchCcdService.findCaseByCaseRef(anyString(), Matchers.any(IdamTokens.class)))
            .thenReturn(Collections.singletonList(CaseDetailsUtils.getCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON)));

        events = caseDataEventBuilder.buildPostponedEvent(appeal);

        verify(searchCcdService, times(1))
            .findCaseByCaseRef(anyString(), Matchers.any(IdamTokens.class));

        assertEquals("One postponed event expected here", 1, events.size());
        assertEquals("type expected is postponed", GapsEvent.HEARING_POSTPONED.getType(),
            events.get(0).getValue().getType());
        LocalDateTime actualPostponedDate = LocalDateTime.parse(events.get(0).getValue().getDate());
        LocalDateTime expectedDate = ZonedDateTime.parse(MINOR_STATUS_ID_27_DATE).toLocalDateTime();
        assertEquals(expectedDate, actualPostponedDate);
    }


    /*
        scenario4
        Given two minor status id 27 with different dates
        And one single postponed Request with granted Yes
        Then only one single postponed event is created from the latest minor status
        todo: check scenario with business: can we get more than one minus status id 27 in Delta??
     */
}
