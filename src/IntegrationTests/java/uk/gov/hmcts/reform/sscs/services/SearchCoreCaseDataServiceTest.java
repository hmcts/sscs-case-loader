package uk.gov.hmcts.reform.sscs.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchCoreCaseDataServiceTest {

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;
    @MockBean
    private IdamApiClient idamApiClient;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private SearchCoreCaseDataService searchCoreCaseDataService;

    @Test
    public void givenCaseRef_shouldFindTheCaseInCcd() {
        given(coreCaseDataApi.searchForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            eq(ImmutableMap.of("case.caseReference", "SC068/17/00013"))
            )
        ).willReturn(Collections.singletonList(CaseDetails.builder().build()));

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

        given(authTokenGenerator.generate()).willReturn("s2sToken");

        List<CaseDetails> cases = searchCoreCaseDataService.findCaseByCaseRef("SC068/17/00013");

        verify(coreCaseDataApi).searchForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            eq(ImmutableMap.of("case.caseReference", "SC068/17/00013"))
        );

        assertEquals("expected one case only", 1, cases.size());
    }
}
