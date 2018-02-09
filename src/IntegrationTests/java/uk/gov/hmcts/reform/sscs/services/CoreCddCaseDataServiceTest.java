package uk.gov.hmcts.reform.sscs.services;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

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
import uk.gov.hmcts.reform.sscs.CaseDataUtils;
import uk.gov.hmcts.reform.sscs.TestCaseLoaderApp;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestCaseLoaderApp.class)
public class CoreCddCaseDataServiceTest {

    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private CoreCaseDataApi coreCaseDataApi;
    @MockBean
    private IdamApiClient idamApiClient;
    @Autowired
    private CoreCaseDataService coreCaseDataService;

    @Test
    public void givenACase_shouldSaveItIntoCdd() {
        given(authTokenGenerator.generate()).willReturn("s2sToken");

        given(coreCaseDataApi.startForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
            )
        ).willReturn(StartEventResponse.builder().build());

        given(coreCaseDataApi.submitForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            eq(true),
            any(CaseDataContent.class)
        )).willReturn(CaseDetails.builder().build());

        given(idamApiClient.authorizeCodeType(
            anyString(),
            anyString(),
            anyString(),
            anyString())
        ).willReturn(new Authorize("url", "code", ""));

        given(idamApiClient.authorizeToken(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString())
        ).willReturn(new Authorize("", "", "accessToken"));

        coreCaseDataService.startEventAndSaveGivenCase(CaseDataUtils.buildCaseData());

        verify(coreCaseDataApi).startForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
        );

        verify(coreCaseDataApi).submitForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            eq(true),
            any(CaseDataContent.class)
        );

    }
}
