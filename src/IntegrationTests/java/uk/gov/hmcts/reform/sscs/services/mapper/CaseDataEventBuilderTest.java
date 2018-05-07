package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        )).willReturn(Collections.singletonList(getCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON)));

        List<Events> events = caseDataEventBuilder.buildPostponedEvent(appeal);

        assertTrue("No postponed event expected here", events.isEmpty());
    }

    private static CaseDetails getCaseDetails(String caseDetails) throws IOException {
        String caseDetailsWithHearings = FileUtils.readFileToString(new File(caseDetails),
            StandardCharsets.UTF_8.name());
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        return mapper.readerFor(CaseDetails.class).readValue(caseDetailsWithHearings);
    }
}
