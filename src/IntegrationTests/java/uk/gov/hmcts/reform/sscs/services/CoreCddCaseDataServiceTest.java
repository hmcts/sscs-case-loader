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
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CoreCddCaseDataServiceTest {

    private static final String USER_TOKEN = "Bearer userToken";
    private static final String S2S_TOKEN = "s2sToken";
    private static final String USER_ID = "19";
    private static final String JURISDICTION_ID = "SSCS";
    private static final String CASE_TYPE_ID = "Benefit";
    private static final String EVENT_ID = "appealCreated";

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
            eq(USER_TOKEN),
            eq(S2S_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(EVENT_ID)
            )
        ).willReturn(StartEventResponse.builder().build());

        given(coreCaseDataApi.submitForCaseworker(
            eq(USER_TOKEN),
            eq(S2S_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        )).willReturn(CaseDetails.builder().build());

        given(idamApiClient.authorize(anyString()))
            .willReturn(new Authorize("url", "userToken"));

        coreCaseDataService.startEventAndSaveGivenCase(CaseDataUtils.buildCaseData());

        verify(coreCaseDataApi)
            .startForCaseworker(
                eq(USER_TOKEN),
                eq(S2S_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(EVENT_ID)
            );

        verify(coreCaseDataApi)
            .submitForCaseworker(
                eq(USER_TOKEN),
                eq(S2S_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(true),
                any(CaseDataContent.class)
            );

        verify(idamApiClient).authorize(anyString());
    }
}
