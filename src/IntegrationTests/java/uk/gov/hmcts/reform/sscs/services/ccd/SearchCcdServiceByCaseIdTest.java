package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.ccd.util.CaseDataUtils;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class SearchCcdServiceByCaseIdTest {

    private static final String CASE_ID = "1111222233334444";
    public static final String IDAM_OAUTH_2_TOKEN = "idamOauth2Token";
    public static final String SERVICE_AUTHORIZATION = "serviceAuthorization";
    public static final String USER_ID = "1234";

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private IdamService idamService;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private CcdService ccdService;

    @Test
    @Ignore
    public void givenCaseRef_shouldFindTheCaseInCcd() {
        given(coreCaseDataApi.readForCaseWorker(
            eq(IDAM_OAUTH_2_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq(USER_ID),
            anyString(),
            anyString(),
            eq(CASE_ID)
            )
        ).willReturn(CaseDataUtils.buildCaseDetails());

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token(IDAM_OAUTH_2_TOKEN)
            .serviceAuthorization(SERVICE_AUTHORIZATION)
            .userId(USER_ID)
            .build();

        SscsCaseDetails cases = ccdService.getByCaseId(Long.parseLong(CASE_ID), idamTokens);

        verify(coreCaseDataApi).readForCaseWorker(
            eq(IDAM_OAUTH_2_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq(USER_ID),
            anyString(),
            anyString(),
            eq(CASE_ID)
        );

        assertNotNull(cases);
    }

    @Test
    @Ignore
    public void givenCaseRef_shouldReturnEmptyListWhenCaseNotInCcd() {

        given(coreCaseDataApi.readForCaseWorker(
            eq(IDAM_OAUTH_2_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq(USER_ID),
            anyString(),
            anyString(),
            eq(CASE_ID)
            )
        ).willReturn(null);

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token(IDAM_OAUTH_2_TOKEN)
            .serviceAuthorization(SERVICE_AUTHORIZATION)
            .userId(USER_ID)
            .build();

        SscsCaseDetails cases = ccdService.getByCaseId(Long.parseLong(CASE_ID), idamTokens);

        verify(coreCaseDataApi).readForCaseWorker(
            eq(IDAM_OAUTH_2_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq(USER_ID),
            anyString(),
            anyString(),
            eq(CASE_ID)
        );

        assertNull("expected no cases", cases);
    }

}
