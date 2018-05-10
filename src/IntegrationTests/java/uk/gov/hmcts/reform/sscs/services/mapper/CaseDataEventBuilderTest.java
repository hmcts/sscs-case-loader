package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CaseDataEventBuilderTest extends CaseDataBuilderBaseTest {


    //fixme: we need to inject this object even though it's not used in this context
    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;
    @MockBean
    private IdamService idamService;
    @Autowired
    private CaseDataEventBuilder caseDataEventBuilder;
    @Autowired
    private CoreCaseDataProperties coreCaseDataProperties;

    private static final String CASE_DETAILS_WITH_HEARINGS_JSON = "src/test/resources/CaseDetailsWithHearings.json";
    private List<Events> events;

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
            .appealCaseRefNum("SC068/17/00011")
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

        given(idamService.getIdamOauth2Token()).willReturn("oauth2Token");
        given(idamService.generateServiceAuthorization()).willReturn("serviceToken");

        given(coreCaseDataApi.searchForCaseworker(
            "oauth2Token",
            "serviceToken",
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            ImmutableMap.of("case.caseReference", appeal.getAppealCaseRefNum())
        )).willReturn(Collections.singletonList(getCaseDetails()));

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
    public void givenScenario3ThenPostponedIsCreated()
        throws Exception {
        AppealCase appeal = AppealCase.builder()
            .appealCaseCaseCodeId("1")
            .appealCaseRefNum("SC068/17/00011")
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
                    "Y", "6", null, null),
                new PostponementRequests(
                    "Y", "", null, null)
            ))
            .build();


        given(idamService.getIdamOauth2Token()).willReturn("oauth2Token");
        given(idamService.generateServiceAuthorization()).willReturn("serviceToken");

        given(coreCaseDataApi.searchForCaseworker(
            "oauth2Token",
            "serviceToken",
            coreCaseDataProperties.getUserId(),
            coreCaseDataProperties.getJurisdictionId(),
            coreCaseDataProperties.getCaseTypeId(),
            ImmutableMap.of("case.caseReference", appeal.getAppealCaseRefNum())
        )).willReturn(Collections.singletonList(getCaseDetails()));

        events = caseDataEventBuilder.buildPostponedEvent(appeal);

        assertEquals("One postponed event expected here", 1, events.size());
        assertEquals("type expected is postponed", GapsEvent.HEARING_POSTPONED.getType(),
            events.get(0).getValue().getType());
        LocalDateTime actualPostponedDate = LocalDateTime.parse(events.get(0).getValue().getDate());
        LocalDateTime expectedDate = ZonedDateTime.parse(MINOR_STATUS_ID_27_DATE).toLocalDateTime();
        assertEquals(expectedDate, actualPostponedDate);
    }

    private static CaseDetails getCaseDetails() throws IOException {
        String caseDetailsWithHearings = FileUtils.readFileToString(
            new File(CaseDataEventBuilderTest.CASE_DETAILS_WITH_HEARINGS_JSON),
            StandardCharsets.UTF_8.name());
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        return mapper.readerFor(CaseDetails.class).readValue(caseDetailsWithHearings);
    }
}
