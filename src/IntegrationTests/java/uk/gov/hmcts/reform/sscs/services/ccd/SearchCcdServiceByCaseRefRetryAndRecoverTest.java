package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class SearchCcdServiceByCaseRefRetryAndRecoverTest {

    private static final String CASE_REF = "SC068/17/00013";
    private static final String AUTHORIZATION = "authorization";
    private static final String SERVICE_AUTHORIZATION = "serviceAuthorization";
    private static final String USER_ID = "16";
    private static final String AUTHORIZATION_2 = "authorization2";
    private static final String SERVICE_AUTHORIZATION_2 = "serviceAuthorization2";

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private IdamService idamService;

    @Autowired
    private CcdService ccdService;

    @Test
    public void givenFindCaseByRefFails_shouldRetryAndRecover() {
        when(coreCaseDataApi.searchCases(
            eq(AUTHORIZATION),
            eq(SERVICE_AUTHORIZATION),
            anyString(),
            anyString()))
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException());

        when(coreCaseDataApi.searchCases(
            eq(AUTHORIZATION_2),
            eq(SERVICE_AUTHORIZATION_2),
            anyString(),
            anyString()))
            .thenReturn(SearchResult.builder().cases(Collections.singletonList(
                CaseDetails.builder().id(10L).build())).build());

        when(idamService.getIdamTokens())
            .thenReturn(IdamTokens.builder()
                .idamOauth2Token(AUTHORIZATION_2)
                .serviceAuthorization(SERVICE_AUTHORIZATION_2)
                .userId(USER_ID)
                .build());

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token(AUTHORIZATION)
            .serviceAuthorization(SERVICE_AUTHORIZATION)
            .userId(USER_ID)
            .build();


        List<SscsCaseDetails> result = ccdService.findCaseBy(
            "case.caseReference", CASE_REF, idamTokens);

        verify(coreCaseDataApi, times(3))
            .searchCases(
                eq(AUTHORIZATION),
                eq(SERVICE_AUTHORIZATION),
                anyString(),
                anyString());

        verify(coreCaseDataApi, times(1))
            .searchCases(
                eq(AUTHORIZATION_2),
                eq(SERVICE_AUTHORIZATION_2),
                anyString(),
                anyString());

        assertTrue(result.get(0).getId() == 10L);
    }
}
