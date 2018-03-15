package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.stub;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class MockCcdIdamSftpServices {

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @MockBean
    CoreCaseDataApi coreCaseDataApi;

    @MockBean
    IdamApiClient idamApiClient;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @Before
    public void setUp() {

        Map<String, Object> caseDataMap = new HashMap<>(1);
        Map<String, Object> evidenceMap = new LinkedHashMap<>();
        evidenceMap.put("documents", new ArrayList<HashMap<String, Object>>());
        caseDataMap.put("evidence", evidenceMap);

        stub(authTokenGenerator.generate()).toReturn("s2s token");

        stub(idamApiClient.authorizeCodeType(anyString(), anyString(), anyString(), anyString()))
            .toReturn(new Authorize("url", "code", ""));

        stub(idamApiClient.authorizeToken(anyString(), anyString(), anyString(), anyString(), anyString()))
            .toReturn(new Authorize("", "", "accessToken"));

        stub(coreCaseDataApi.startForCaseworker(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .toReturn(StartEventResponse.builder().build());

        stub(coreCaseDataApi.submitForCaseworker(
            anyString(), anyString(), anyString(), anyString(), anyString(),
            eq(Boolean.TRUE), any(CaseDataContent.class)))
            .toReturn(CaseDetails.builder().data(caseDataMap).build());

        stub(coreCaseDataApi.searchForCaseworker(
            anyString(), anyString(), anyString(), anyString(), anyString(), any()))
            .toReturn(Collections.singletonList(CaseDetails.builder().id(1L).data(caseDataMap).build()));

        stub(coreCaseDataApi.searchForCaseworker(
            anyString(), anyString(), anyString(), anyString(), anyString(),
            eq(ImmutableMap.of("case.caseReference", "SC068/17/00004"))))
            .toReturn(new ArrayList<>());

        stub(coreCaseDataApi.startEventForCaseWorker(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .toReturn(StartEventResponse.builder().build());

        stub(coreCaseDataApi.submitEventForCaseWorker(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
            anyBoolean(), any(CaseDataContent.class)))
            .toReturn(CaseDetails.builder().build());

        String deltaFilename = "SSCS_Extract_Delta_2018-05-01-01-01-01.xml";

        stub(channelAdapter.listIncoming()).toReturn(newArrayList(new Gaps2File(deltaFilename)));
        stub(channelAdapter.getInputStream(deltaFilename)).toAnswer(x ->
                getClass().getClassLoader().getResourceAsStream("process_case_test_delta.xml"));
        stub(channelAdapter.listProcessed()).toReturn(newArrayList());
        stub(channelAdapter.listFailed()).toReturn(newArrayList());
    }

}
