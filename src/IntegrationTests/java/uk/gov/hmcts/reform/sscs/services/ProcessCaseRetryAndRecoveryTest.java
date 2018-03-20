package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.refdata.RefDataRepository;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class ProcessCaseRetryAndRecoveryTest {

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

        String deltaFilename = "SSCS_Extract_Delta_2018-05-01-01-01-01.xml";

        stub(channelAdapter.listFailed()).toReturn(newArrayList());
        stub(channelAdapter.listProcessed()).toReturn(newArrayList());
        stub(channelAdapter.listIncoming()).toReturn(newArrayList(new Gaps2File(deltaFilename)));

        stub(channelAdapter.getInputStream(deltaFilename)).toAnswer(x ->
            getClass().getClassLoader().getResourceAsStream("process_case_test_delta.xml"));

        stub(idamApiClient.authorizeCodeType(anyString(), anyString(), anyString(), anyString()))
            .toReturn(new Authorize("url", "code", ""));

        given(authTokenGenerator.generate()).willReturn("s2s token");

        stub(idamApiClient.authorizeToken(anyString(), anyString(), anyString(), anyString(), anyString()))
            .toReturn(new Authorize("", "", "accessToken"));

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

    private void verifyCcdApiRetries3TimesWhenAFailureUpdatingACaseAndRecoverSuccessfully() {
        verify(coreCaseDataApi, times(3))
            .submitEventForCaseWorker(
                eq("Bearer accessToken2"),
                eq("s2s token2"),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyBoolean(),
                any(CaseDataContent.class));

        verify(coreCaseDataApi, times(1))
            .submitEventForCaseWorker(
                eq("Bearer accessToken3"),
                eq("s2s token3"),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyBoolean(),
                any(CaseDataContent.class));
    }

    private void verifyFindCaseByCaseRefRetries3TimesIfFailureAndRecoverSuccessfully() {
        verify(coreCaseDataApi, times(3)).searchForCaseworker(
            eq("Bearer accessToken"),
            eq("s2s token"),
            anyString(),
            anyString(),
            anyString(),
            any());

        verify(coreCaseDataApi, times(1)).searchForCaseworker(
            eq("Bearer accessToken2"),
            eq("s2s token2"),
            anyString(),
            anyString(),
            anyString(),
            any());
    }

    private void mockCcdApiToSucceedWhenUpdatingACase() {
        when(coreCaseDataApi.submitEventForCaseWorker(
            eq("Bearer accessToken3"),
            eq("s2s token3"),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyBoolean(),
            any(CaseDataContent.class)))
            .thenReturn(CaseDetails.builder().build());
    }

    @SuppressWarnings("unchecked")
    private void mockCcdApiToThrowExceptionWhenUpdatingACase() {
        when(coreCaseDataApi.submitEventForCaseWorker(
            eq("Bearer accessToken2"),
            eq("s2s token2"),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyBoolean(),
            any(CaseDataContent.class)))
            .thenThrow(Exception.class)
            .thenThrow(Exception.class)
            .thenThrow(Exception.class);
    }

    private void mockCcdApiToReturnResultWhenCalled() {
        Map<String, Object> caseDataMap = new HashMap<>(1);
        Map<String, Object> evidenceMap = new LinkedHashMap<>();
        evidenceMap.put("documents", new ArrayList<HashMap<String, Object>>());
        caseDataMap.put("evidence", evidenceMap);


        when(coreCaseDataApi.searchForCaseworker(eq("Bearer accessToken2"), eq("s2s token2"),
            anyString(), anyString(), anyString(), any()))
            .thenReturn(Collections.singletonList(CaseDetails.builder()
                .id(10L)
                .data(caseDataMap)
                .build()));
    }

    @SuppressWarnings("unchecked")
    private void mockCcdApiToThrowExceptionWhenFindingCaseByRefIsCalled() {
        when(coreCaseDataApi.searchForCaseworker(eq("Bearer accessToken"), eq("s2s token"),
            anyString(), anyString(), anyString(), any()))
            .thenThrow(Exception.class)
            .thenThrow(Exception.class)
            .thenThrow(Exception.class);
    }

    private void mockIdamApi() {
        when(idamApiClient.authorizeCodeType(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(new Authorize("url", "code", ""));

        when(authTokenGenerator.generate())
            .thenReturn("s2s token")
            .thenReturn("s2s token2");

        when(idamApiClient.authorizeToken(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(new Authorize("", "", "accessToken"))
            .thenReturn(new Authorize("", "", "accessToken2"))
            .thenReturn(new Authorize("", "", "accessToken3"));
    }
}
