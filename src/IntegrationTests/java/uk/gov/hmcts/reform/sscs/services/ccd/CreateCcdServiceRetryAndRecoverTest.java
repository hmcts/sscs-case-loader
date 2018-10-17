package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class CreateCcdServiceRetryAndRecoverTest {

    public static final String IDAM_OAUTH_2_TOKEN = "authorization";
    public static final String USER_ID = "16";
    public static final String SERVICE_AUTHORIZATION = "serviceAuthorization";
    public static final String AUTHORIZATION_2 = "authorization2";
    public static final String SERVICE_AUTHORIZATION_2 = "serviceAuthorization2";

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private IdamService idamService;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @Autowired
    private CcdService ccdService;

    private SscsCaseData caseData;
    private IdamTokens idamTokens;

    @Before
    public void setUp() {
        caseData = SscsCaseData.builder()
            .caseReference("SC068/17/00004")
            .build();
        idamTokens = IdamTokens.builder()
            .idamOauth2Token(IDAM_OAUTH_2_TOKEN)
            .serviceAuthorization(SERVICE_AUTHORIZATION)
            .userId(USER_ID)
            .build();
    }

    @Test
    public void givenCreateCcdApiFailsWhenStartCaseWorker_shouldRetryAndRecover() {
        when(coreCaseDataApi.startForCaseworker(
            eq(IDAM_OAUTH_2_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq(USER_ID),
            anyString(),
            anyString(),
            anyString()))
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException());

        when(idamService.getIdamTokens())
            .thenReturn(IdamTokens.builder()
                .idamOauth2Token(AUTHORIZATION_2)
                .serviceAuthorization(SERVICE_AUTHORIZATION_2)
                .userId(USER_ID)
                .build());

        when(coreCaseDataApi.startForCaseworker(
            eq(AUTHORIZATION_2),
            eq(SERVICE_AUTHORIZATION_2),
            eq(USER_ID),
            anyString(),
            anyString(),
            anyString()))
            .thenReturn(StartEventResponse.builder().build());

        when(coreCaseDataApi.submitForCaseworker(
            eq(AUTHORIZATION_2),
            eq(SERVICE_AUTHORIZATION_2),
            eq(USER_ID),
            anyString(),
            anyString(),
            eq(true),
            any(CaseDataContent.class)))
            .thenReturn(CaseDetails.builder().build());

        ccdService.createCase(caseData, idamTokens);

        verify(coreCaseDataApi, times(0)).submitForCaseworker(
            eq(IDAM_OAUTH_2_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq(USER_ID),
            anyString(),
            anyString(),
            eq(true),
            any(CaseDataContent.class));
    }

    @Test
    public void givenCreateCcdApiFailsWhenSubmittingForCaseWorker_shouldRetryAndRecover() {
        when(coreCaseDataApi.startForCaseworker(
            eq(IDAM_OAUTH_2_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq(USER_ID),
            anyString(),
            anyString(),
            anyString()))
            .thenReturn(StartEventResponse.builder().build());

        when(coreCaseDataApi.submitForCaseworker(
            eq(IDAM_OAUTH_2_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq(USER_ID),
            anyString(),
            anyString(),
            eq(true),
            any(CaseDataContent.class)))
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException());

        when(idamService.getIdamTokens())
            .thenReturn(IdamTokens.builder()
                .idamOauth2Token(AUTHORIZATION_2)
                .serviceAuthorization(SERVICE_AUTHORIZATION_2)
                .userId(USER_ID)
                .build());

        when(coreCaseDataApi.startForCaseworker(
            eq(AUTHORIZATION_2),
            eq(SERVICE_AUTHORIZATION_2),
            eq(USER_ID),
            anyString(),
            anyString(),
            anyString()))
            .thenReturn(StartEventResponse.builder().build());

        when(coreCaseDataApi.submitForCaseworker(
            eq(AUTHORIZATION_2),
            eq(SERVICE_AUTHORIZATION_2),
            eq(USER_ID),
            anyString(),
            anyString(),
            eq(true),
            any(CaseDataContent.class)))
            .thenReturn(CaseDetails.builder().build());

        ccdService.createCase(caseData, idamTokens);
    }
}
