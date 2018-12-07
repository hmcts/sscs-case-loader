package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.exception.CreateCcdCaseException;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.Authorize;
import uk.gov.hmcts.reform.sscs.idam.IdamApiClient;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class CreateCcdServiceTest {

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private IdamApiClient idamApiClient;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @Autowired
    private CcdService ccdService;

    private SscsCaseData caseData;
    private IdamTokens idamTokens;
    private Authorize authorize;

    @Before
    public void setUp() {
        authorize = Authorize.builder()
            .accessToken("accessToken")
            .code("code")
            .defaultUrl("defaultUrl")
            .build();
        caseData = SscsCaseData.builder()
            .caseReference("SC068/17/00004")
            .appeal(Appeal.builder()
                .benefitType(BenefitType.builder()
                    .code("pip")
                    .build())
                .build())
            .build();
        idamTokens = IdamTokens.builder()
            .idamOauth2Token("authToken")
            .serviceAuthorization("s2sToken")
            .userId("16")
            .build();
    }

    @Test(expected = CreateCcdCaseException.class)
    public void givenAnErrorOccursWhenStartCreatingCaseInCcd_shouldThrowException() {
        given(idamApiClient.authorizeCodeType(any(), any(), any(), any()))
            .willReturn(authorize);

        given(idamApiClient.authorizeToken(any(), any(), any(), any(), any()))
            .willReturn(authorize);

        given(coreCaseDataApi.startForCaseworker(any(), any(), any(), any(), any(), any()))
            .willThrow(new RuntimeException());

        ccdService.createCase(caseData, idamTokens);
    }

    @Test
    public void shouldCreateCaseInCcd() {
        given(idamApiClient.authorizeCodeType(any(), any(), any(), any()))
            .willReturn(authorize);

        given(idamApiClient.authorizeToken(any(), any(), any(), any(), any()))
            .willReturn(authorize);

        given(coreCaseDataApi.startForCaseworker(any(), any(), any(), any(), any(), any()))
            .willReturn(StartEventResponse.builder().build());

        given(coreCaseDataApi.submitForCaseworker(any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willReturn(CaseDetails.builder().build());

        SscsCaseDetails actualCase = ccdService.createCase(caseData, idamTokens);

        assertThat(actualCase, is(notNullValue()));
    }

    @Test(expected = CreateCcdCaseException.class)
    public void givenAnErrorOccursWhenSubmittingACaseInCcd_shouldThrowException() {
        given(idamApiClient.authorizeCodeType(any(), any(), any(), any()))
            .willReturn(authorize);

        given(idamApiClient.authorizeToken(any(), any(), any(), any(), any()))
            .willReturn(authorize);

        given(coreCaseDataApi.startForCaseworker(any(), any(), any(), any(), any(), any()))
            .willReturn(StartEventResponse.builder().build());

        given(coreCaseDataApi.submitForCaseworker(any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willThrow(new RuntimeException());

        ccdService.createCase(caseData, idamTokens);
    }

}
