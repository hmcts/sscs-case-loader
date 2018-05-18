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
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCcdService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchCcdServiceTest {

    private static final String CASE_REF = "SC068/17/00013";

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private SearchCcdService searchCcdService;

    @Test
    public void givenCaseRef_shouldFindTheCaseInCcd() {
        given(coreCaseDataApi.searchForCaseworker(
            eq("idamOauth2Token"),
            eq("serviceAuthorization"),
            eq("sscs"),
            anyString(),
            anyString(),
            eq(ImmutableMap.of("case.caseReference", CASE_REF))
            )
        ).willReturn(Collections.singletonList(CaseDetails.builder().build()));

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .authenticationService("serviceAuthorization")
            .serviceUserId("sscs")
            .build();

        List<CaseDetails> cases = searchCcdService.findCaseByCaseRef(CASE_REF, idamTokens);

        verify(coreCaseDataApi).searchForCaseworker(
            eq("idamOauth2Token"),
            eq("serviceAuthorization"),
            eq("sscs"),
            anyString(),
            anyString(),
            eq(ImmutableMap.of("case.caseReference", CASE_REF))
        );

        assertEquals("expected one case only", 1, cases.size());
    }

}
