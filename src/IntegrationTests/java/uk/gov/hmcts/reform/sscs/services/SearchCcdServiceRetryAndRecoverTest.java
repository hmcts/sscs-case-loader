package uk.gov.hmcts.reform.sscs.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCcdService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class SearchCcdServiceRetryAndRecoverTest {

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private IdamService idamService;

    @Autowired
    private SearchCcdService searchCcdService;

    @Test
    public void givenFindCaseByRefFails_shouldRetryAndRecover() {
        when(coreCaseDataApi.searchForCaseworker(
            eq("authorization"),
            eq("serviceAuthorization"),
            eq("16"),
            anyString(),
            anyString(),
            any()))
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException());

        when(coreCaseDataApi.searchForCaseworker(
            eq("authorization2"),
            eq("serviceAuthorization2"),
            eq("16"),
            anyString(),
            anyString(),
            any()))
            .thenReturn(Collections.singletonList(CaseDetails.builder()
                .id(10L)
                .build()));

        when(idamService.getIdamOauth2Token()).thenReturn("authorization2");
        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuthorization2");

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("authorization")
            .serviceAuthorization("serviceAuthorization")
            .userId("16")
            .build();

        List<CaseDetails> result = searchCcdService.findCaseByCaseRef("caseRef", idamTokens);

        verify(coreCaseDataApi, times(3))
            .searchForCaseworker(
                eq("authorization"),
                eq("serviceAuthorization"),
                eq("16"),
                anyString(),
                anyString(),
                any());

        verify(coreCaseDataApi, times(1))
            .searchForCaseworker(
                eq("authorization2"),
                eq("serviceAuthorization2"),
                eq("16"),
                anyString(),
                anyString(),
                any());

        assertTrue(result.get(0).getId() == 10L);
    }
}
