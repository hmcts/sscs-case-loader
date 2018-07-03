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
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.models.idam.UserDetails;
import uk.gov.hmcts.reform.sscs.refdata.RefDataRepository;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessCaseTest {

    private static final String USER_AUTH =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1"
            + "NiJ9.eyJzdWIiOiIxNiIsIm5hbWUiOiJ"
            + "UZXN0IiwianRpIjoiMTIzNCIsImlhdCI"
            + "6MTUyNjkyOTk1MiwiZXhwIjoxNTI2OTM"
            + "zNTg5fQ.lZwrWNjG-y1Olo1qWocKIuq3"
            + "_fdffVF8BTcR5l87FTg";

    private static final String USER_AUTH_WITH_TYPE = "Bearer " + USER_AUTH;

    private static final String SERVER_AUTH =
        "Bearer "
            + "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ"
            + "zc2NzIiwiZXhwIjoxNTI2NjU2NTEyfQ."
            + "aADJFE6_FJPNpDO_0NbqS-oYIDM9Bjjh"
            + "18ZyB1imXGXAqOEc8Iyy0zxBe6BhXFl8"
            + "E8panNAv3zdDDeOhlrEViQ";

    private static final String USER_ID = "16";

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
        Map<String, Object> appealMap = new LinkedHashMap<>();
        evidenceMap.put("documents", new ArrayList<HashMap<String, Object>>());
        caseDataMap.put("evidence", evidenceMap);
        caseDataMap.put("appeal", appealMap);

        String refFilename = "SSCS_Extract_Reference_2017-05-24-16-14-19.xml";
        String deltaFilename = "SSCS_Extract_Delta_2018-05-01-01-01-01.xml";

        stub(channelAdapter.listFailed()).toReturn(newArrayList());
        stub(channelAdapter.listProcessed()).toReturn(newArrayList());
        stub(channelAdapter.listIncoming())
            .toReturn(newArrayList(new Gaps2File(refFilename), new Gaps2File(deltaFilename)));

        stub(channelAdapter.getInputStream(refFilename)).toAnswer(x ->
            getClass().getClassLoader().getResourceAsStream("SSCS_Extract_Reference_2017-05-24-16-14-19.xml"));

        stub(channelAdapter.getInputStream(deltaFilename)).toAnswer(x ->
            getClass().getClassLoader().getResourceAsStream("process_case_test_delta.xml"));

        stub(idamApiClient.authorizeCodeType(anyString(), anyString(), anyString(), anyString()))
            .toReturn(new Authorize("url", "code", ""));

        given(authTokenGenerator.generate()).willReturn(SERVER_AUTH);

        stub(idamApiClient.authorizeToken(anyString(), anyString(), anyString(), anyString(), anyString()))
            .toReturn(new Authorize("", "", USER_AUTH));

        stub(idamApiClient.getUserDetails(eq(USER_AUTH_WITH_TYPE))).toReturn(new UserDetails("16"));

        stub(refDataRepository.find(CASE_CODE, "1001", BEN_ASSESS_TYPE_ID)).toReturn("bat");
        stub(refDataRepository.find(BEN_ASSESS_TYPE, "bat", BAT_CODE)).toReturn("code");
        stub(refDataRepository.find(BAT_CODE_MAP, "code", BENEFIT_DESC)).toReturn("PIP");

        referenceDataService.setRefDataRepo(refDataRepository);

        // SC reference case

        CaseDetails scReferenceCaseDetails =
            CaseDetails.builder()
                .id(123L)
                .data(caseDataMap)
                .build();

        stub(coreCaseDataApi.searchForCaseworker(
            anyString(), anyString(), anyString(), anyString(), anyString(),
            eq(ImmutableMap.of("case.caseReference", "SC068/01/00001"))))
            .toReturn(new ArrayList<>());

        stub(coreCaseDataApi.startForCaseworker(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .toReturn(StartEventResponse.builder().build());

        stub(coreCaseDataApi.submitForCaseworker(
            anyString(), anyString(), anyString(), anyString(), anyString(),
            eq(Boolean.TRUE), any(CaseDataContent.class)))
            .toReturn(scReferenceCaseDetails);

        // CCD ID case

        CaseDetails ccdCaseDetails =
            CaseDetails.builder()
                .id(456L)
                .data(caseDataMap)
                .build();

        stub(coreCaseDataApi.readForCaseWorker(
            anyString(), anyString(), anyString(), anyString(), anyString(),
            eq("1234567890")))
            .toReturn(ccdCaseDetails);

        stub(coreCaseDataApi.startEventForCaseWorker(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .toReturn(StartEventResponse.builder().build());

        stub(coreCaseDataApi.submitEventForCaseWorker(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
            anyBoolean(), any(CaseDataContent.class)))
            .toReturn(ccdCaseDetails);

    }

    @Test
    public void shouldBeSavedIntoCcdGivenDeltaXmlInSftp() {

        caseLoaderService.process();

        // SC reference case

        verify(coreCaseDataApi).searchForCaseworker(
            eq(USER_AUTH_WITH_TYPE),
            eq(SERVER_AUTH),
            eq(USER_ID),
            anyString(),
            anyString(),
            eq(ImmutableMap.of("case.caseReference", "SC068/01/00001"))
        );

        verify(coreCaseDataApi).submitForCaseworker(
            eq(USER_AUTH_WITH_TYPE),
            eq(SERVER_AUTH),
            eq(USER_ID),
            eq("SSCS"),
            eq("Benefit"),
            eq(true),
            notNull(CaseDataContent.class)
        );

        // CCD ID case

        verify(coreCaseDataApi).readForCaseWorker(
            eq(USER_AUTH_WITH_TYPE),
            eq(SERVER_AUTH),
            eq(USER_ID),
            anyString(),
            anyString(),
            eq("1234567890")
        );

        verify(coreCaseDataApi).submitEventForCaseWorker(
            eq(USER_AUTH_WITH_TYPE),
            eq(SERVER_AUTH),
            eq(USER_ID),
            eq("SSCS"),
            eq("Benefit"),
            eq("456"),
            eq(true),
            notNull(CaseDataContent.class)
        );
    }
}
