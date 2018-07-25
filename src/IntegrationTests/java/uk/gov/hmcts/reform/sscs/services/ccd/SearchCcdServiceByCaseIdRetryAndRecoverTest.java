package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class SearchCcdServiceByCaseIdRetryAndRecoverTest {

    private static final String CASE_ID = "1111222233334444";

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private IdamService idamService;

    @Autowired
    private SearchCcdServiceByCaseId searchCcdServiceByCaseId;

    @Test
    public void givenFindCaseByRefFails_shouldRetryAndRecover() {
        when(coreCaseDataApi.readForCaseWorker(
            eq("authorization"),
            eq("serviceAuthorization"),
            eq("16"),
            anyString(),
            anyString(),
            any()))
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException())
            .thenThrow(new RuntimeException());

        when(coreCaseDataApi.readForCaseWorker(
            eq("authorization2"),
            eq("serviceAuthorization2"),
            eq("16"),
            anyString(),
            anyString(),
            any()))
            .thenReturn(CaseDetails.builder().id(10L).build());

        when(idamService.getIdamOauth2Token()).thenReturn("authorization2");
        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuthorization2");

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("authorization")
            .serviceAuthorization("serviceAuthorization")
            .userId("16")
            .build();

        List<CaseDetails> result = searchCcdServiceByCaseId.findCaseByCaseId(CASE_ID, idamTokens);

        verify(coreCaseDataApi, times(3))
            .readForCaseWorker(
                eq("authorization"),
                eq("serviceAuthorization"),
                eq("16"),
                anyString(),
                anyString(),
                any());

        verify(coreCaseDataApi, times(1))
            .readForCaseWorker(
                eq("authorization2"),
                eq("serviceAuthorization2"),
                eq("16"),
                anyString(),
                anyString(),
                any());

        assertTrue(result.get(0).getId() == 10L);
    }
}
