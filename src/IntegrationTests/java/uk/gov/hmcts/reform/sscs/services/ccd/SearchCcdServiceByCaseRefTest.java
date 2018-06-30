package uk.gov.hmcts.reform.sscs.services.ccd;

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
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchCcdServiceByCaseRefTest {

    private static final String CASE_REF = "SC068/17/00013";

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private SearchCcdServiceByCaseRef searchCcdServiceByCaseRef;

    @Test
    public void givenCaseRef_shouldFindTheCaseInCcd() {
        given(coreCaseDataApi.searchForCaseworker(
            eq("idamOauth2Token"),
            eq("serviceAuthorization"),
            eq("1234"),
            anyString(),
            anyString(),
            eq(ImmutableMap.of("case.caseReference", CASE_REF))
            )
        ).willReturn(Collections.singletonList(CaseDetails.builder().build()));

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .serviceAuthorization("serviceAuthorization")
            .userId("1234")
            .build();

        List<CaseDetails> cases = searchCcdServiceByCaseRef.findCaseByCaseRef(CASE_REF, idamTokens);

        verify(coreCaseDataApi).searchForCaseworker(
            eq("idamOauth2Token"),
            eq("serviceAuthorization"),
            eq("1234"),
            anyString(),
            anyString(),
            eq(ImmutableMap.of("case.caseReference", CASE_REF))
        );

        assertEquals("expected one case only", 1, cases.size());
    }

}
