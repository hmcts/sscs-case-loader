package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class SearchCcdServiceByCaseRefRetryAndRecoverTest {

    private static final String CASE_REF = "SC068/17/00013";
    public static final String AUTHORIZATION = "authorization";
    public static final String SERVICE_AUTHORIZATION = "serviceAuthorization";
    public static final String VALUE = "16";
    public static final String AUTHORIZATION_2 = "authorization2";
    public static final String SERVICE_AUTHORIZATION_2 = "serviceAuthorization2";

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
        when(coreCaseDataApi.searchForCaseworker(
            eq(AUTHORIZATION),
            eq(SERVICE_AUTHORIZATION),
            eq(VALUE),
            anyString(),
            anyString(),
            any()))
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException());

        when(coreCaseDataApi.searchForCaseworker(
            eq(AUTHORIZATION_2),
            eq(SERVICE_AUTHORIZATION_2),
            eq(VALUE),
            anyString(),
            anyString(),
            any()))
            .thenReturn(Collections.singletonList(
                CaseDetails.builder().id(10L).build()
            ));

        when(idamService.getIdamTokens())
            .thenReturn(IdamTokens.builder()
                .idamOauth2Token(AUTHORIZATION_2)
                .serviceAuthorization(SERVICE_AUTHORIZATION_2)
                .userId(VALUE)
                .build());

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token(AUTHORIZATION)
            .serviceAuthorization(SERVICE_AUTHORIZATION)
            .userId(VALUE)
            .build();



        List<SscsCaseDetails> result = ccdService
            .findCaseBy(ImmutableMap.of("case.caseReference", CASE_REF), idamTokens);

        verify(coreCaseDataApi, times(3))
            .searchForCaseworker(
                eq(AUTHORIZATION),
                eq(SERVICE_AUTHORIZATION),
                eq(VALUE),
                anyString(),
                anyString(),
                any());

        verify(coreCaseDataApi, times(1))
            .searchForCaseworker(
                eq(AUTHORIZATION_2),
                eq(SERVICE_AUTHORIZATION_2),
                eq(VALUE),
                anyString(),
                anyString(),
                any());

        assertTrue(result.get(0).getId() == 10L);
    }
}
