package uk.gov.hmcts.reform.sscs.services.ccd;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchCcdServiceByCaseIdTest {

    private static final String CASE_ID = "1111222233334444";

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private SearchCcdServiceByCaseId searchCcdServiceByCaseId;

    @Test
    public void givenCaseRef_shouldFindTheCaseInCcd() {
        given(coreCaseDataApi.readForCaseWorker(
            eq("idamOauth2Token"),
            eq("serviceAuthorization"),
            eq("1234"),
            anyString(),
            anyString(),
            eq(CASE_ID)
            )
        ).willReturn(CaseDetails.builder().build());

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .serviceAuthorization("serviceAuthorization")
            .userId("1234")
            .build();

        List<CaseDetails> cases = searchCcdServiceByCaseId.findCaseByCaseId(CASE_ID, idamTokens);

        verify(coreCaseDataApi).readForCaseWorker(
            eq("idamOauth2Token"),
            eq("serviceAuthorization"),
            eq("1234"),
            anyString(),
            anyString(),
            eq(CASE_ID)
        );

        assertEquals("expected one case only", 1, cases.size());
    }

    @Test
    public void givenCaseRef_shouldReturnEmptyListWhenCaseNotInCcd() {

        given(coreCaseDataApi.readForCaseWorker(
            eq("idamOauth2Token"),
            eq("serviceAuthorization"),
            eq("1234"),
            anyString(),
            anyString(),
            eq(CASE_ID)
            )
        ).willReturn(null);

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .serviceAuthorization("serviceAuthorization")
            .userId("1234")
            .build();

        List<CaseDetails> cases = searchCcdServiceByCaseId.findCaseByCaseId(CASE_ID, idamTokens);

        verify(coreCaseDataApi).readForCaseWorker(
            eq("idamOauth2Token"),
            eq("serviceAuthorization"),
            eq("1234"),
            anyString(),
            anyString(),
            eq(CASE_ID)
        );

        assertTrue("expected no cases", cases.isEmpty());
    }

}
