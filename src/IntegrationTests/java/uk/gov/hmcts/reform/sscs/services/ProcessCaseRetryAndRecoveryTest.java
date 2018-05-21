package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.BAT_CODE_MAP;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.BEN_ASSESS_TYPE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.CASE_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BAT_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BENEFIT_DESC;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BEN_ASSESS_TYPE_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.refdata.RefDataRepository;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.idam.AuthTokenSubjectExtractor;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class ProcessCaseRetryAndRecoveryTest {

    private static final String USER_AUTH = "oauth2token";
    private static final String USER_AUTH_WITH_TYPE = "Bearer " + USER_AUTH;
    private static final String USER_AUTH2 = "oauth2token2";
    private static final String USER_AUTH2_WITH_TYPE = "Bearer " + USER_AUTH2;
    private static final String USER_AUTH3 = "oauth2token3";
    private static final String USER_AUTH3_WITH_TYPE = "Bearer " + USER_AUTH3;
    private static final String USER_ID = "16";
    private static final String SERVER_AUTH = "s2s token";
    private static final String SERVER_AUTH2 = "s2s token2";

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private AuthTokenSubjectExtractor authTokenSubjectExtractor;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private IdamApiClient idamApiClient;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private RefDataRepository refDataRepository;

    @MockBean
    private CcdCasesSender ccdCasesSender;

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

        given(authTokenSubjectExtractor.extract(USER_AUTH_WITH_TYPE)).willReturn(USER_ID);
        given(authTokenSubjectExtractor.extract(USER_AUTH2_WITH_TYPE)).willReturn(USER_ID);
        given(authTokenSubjectExtractor.extract(USER_AUTH3_WITH_TYPE)).willReturn(USER_ID);

        stub(refDataRepository.find(CASE_CODE, "1001", BEN_ASSESS_TYPE_ID)).toReturn("bat");
        stub(refDataRepository.find(BEN_ASSESS_TYPE, "bat", BAT_CODE)).toReturn("code");
        stub(refDataRepository.find(BAT_CODE_MAP, "code", BENEFIT_DESC)).toReturn("PIP");

        referenceDataService.setRefDataRepo(refDataRepository);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenCcdApiThrowsExceptionWhenFindingCaseByCaseRef_shouldRequestIdamTokensAndSucceed() {
        mockIdamApi();

        mockCcdApiToThrowExceptionWhenFindingCaseByRefIsCalled();
        mockCcdApiToReturnResultWhenCalled();

        doNothing().when(ccdCasesSender).sendUpdateCcdCases(any(CaseData.class), any(CaseDetails.class),
            any(IdamTokens.class));

        caseLoaderService.process();

        verifyFindCaseByCaseRefRetries3TimesIfFailureAndRecoverSuccessfully();
    }


    private void verifyFindCaseByCaseRefRetries3TimesIfFailureAndRecoverSuccessfully() {
        verify(coreCaseDataApi, times(3)).searchForCaseworker(
            eq(USER_AUTH_WITH_TYPE),
            eq(SERVER_AUTH),
            eq(USER_ID),
            anyString(),
            anyString(),
            any());

        verify(coreCaseDataApi, times(1)).searchForCaseworker(
            eq(USER_AUTH2_WITH_TYPE),
            eq(SERVER_AUTH2),
            eq(USER_ID),
            anyString(),
            anyString(),
            any());
    }

    private void mockCcdApiToReturnResultWhenCalled() {
        Map<String, Object> caseDataMap = new HashMap<>(1);
        Map<String, Object> evidenceMap = new LinkedHashMap<>();
        evidenceMap.put("documents", new ArrayList<HashMap<String, Object>>());
        caseDataMap.put("evidence", evidenceMap);

        when(coreCaseDataApi.searchForCaseworker(
            eq(USER_AUTH2_WITH_TYPE),
            eq(SERVER_AUTH2),
            eq(USER_ID),
            anyString(),
            anyString(),
            any()))
            .thenReturn(Collections.singletonList(CaseDetails.builder()
                .id(10L)
                .data(caseDataMap)
                .build()));
    }

    @SuppressWarnings("unchecked")
    private void mockCcdApiToThrowExceptionWhenFindingCaseByRefIsCalled() {
        when(coreCaseDataApi.searchForCaseworker(
            eq(USER_AUTH_WITH_TYPE),
            eq(SERVER_AUTH),
            eq(USER_ID),
            anyString(),
            anyString(),
            any()))
            .thenThrow(Exception.class)
            .thenThrow(Exception.class)
            .thenThrow(Exception.class);
    }

    private void mockIdamApi() {
        when(idamApiClient.authorizeCodeType(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(new Authorize("url", "code", ""));

        when(authTokenGenerator.generate())
            .thenReturn(SERVER_AUTH)
            .thenReturn(SERVER_AUTH2);

        when(idamApiClient.authorizeToken(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(new Authorize("", "", USER_AUTH))
            .thenReturn(new Authorize("", "", USER_AUTH2))
            .thenReturn(new Authorize("", "", USER_AUTH3));
    }

}
