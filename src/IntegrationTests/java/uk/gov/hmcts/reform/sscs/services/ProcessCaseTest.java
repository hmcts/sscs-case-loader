package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.BAT_CODE_MAP;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.BEN_ASSESS_TYPE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.CASE_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BAT_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BENEFIT_DESC;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BEN_ASSESS_TYPE_ID;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.refdata.RefDataRepository;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@ContextConfiguration
@TestPropertySource(properties = {"sscs.case.loader.ignoreCasesBeforeDate=2017-01-01"})
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessCaseTest {

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private IdamApiClient idamApiClient;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private RefDataRepository refDataRepository;

    @Autowired
    private ReferenceDataService referenceDataService;

    @Autowired
    private CaseLoaderService caseLoaderService;

    @Before
    public void setUp() {

        Map<String, Object> caseDataMap = new HashMap<>(1);
        Map<String, Object> evidenceMap = new LinkedHashMap<>();
        evidenceMap.put("documents", new ArrayList<HashMap<String, Object>>());
        caseDataMap.put("evidence", evidenceMap);

        String deltaFilename = "SSCS_Extract_Delta_2018-05-01-01-01-01.xml";

        stub(channelAdapter.listFailed()).toReturn(newArrayList());
        stub(channelAdapter.listProcessed()).toReturn(newArrayList());
        stub(channelAdapter.listIncoming()).toReturn(newArrayList(new Gaps2File(deltaFilename)));

        stub(channelAdapter.getInputStream(deltaFilename)).toAnswer(x ->
            getClass().getClassLoader().getResourceAsStream("process_case_test_delta.xml"));

        stub(idamApiClient.authorizeCodeType(anyString(), anyString(), anyString(), anyString()))
            .toReturn(new Authorize("url", "code", ""));

        given(authTokenGenerator.generate()).willReturn("s2s token");

        stub(coreCaseDataApi.searchForCaseworker(
            anyString(), anyString(), anyString(), anyString(), anyString(), any()))
            .toReturn(newArrayList());

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

        stub(refDataRepository.find(CASE_CODE, "1001", BEN_ASSESS_TYPE_ID)).toReturn("bat");
        stub(refDataRepository.find(BEN_ASSESS_TYPE, "bat", BAT_CODE)).toReturn("code");
        stub(refDataRepository.find(BAT_CODE_MAP, "code", BENEFIT_DESC)).toReturn("PIP");

        referenceDataService.setRefDataRepo(refDataRepository);
    }

    @Test
    public void shouldBeSavedIntoCcdGivenDeltaXmlInSftp() {

        caseLoaderService.process();

        verify(coreCaseDataApi).searchForCaseworker(
            anyString(), anyString(), anyString(), anyString(), anyString(), any());
    }
}
