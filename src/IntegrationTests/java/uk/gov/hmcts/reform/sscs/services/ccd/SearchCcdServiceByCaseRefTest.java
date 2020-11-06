package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsQueryBuilder;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
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
    private CcdService ccdService;

    @Test
    public void givenCaseRef_shouldFindTheCaseInCcd() {
        SearchSourceBuilder query = SscsQueryBuilder.findCaseBySingleField("data.caseReference", CASE_REF);

        given(coreCaseDataApi.searchCases(
            eq("idamOauth2Token"),
            eq("serviceAuthorization"),
            anyString(),
            eq(query.toString())
        )).willReturn(SearchResult.builder().cases(Collections.singletonList(CaseDetails.builder().build())).build());

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .serviceAuthorization("serviceAuthorization")
            .userId("1234")
            .build();

        List<SscsCaseDetails> cases = ccdService
            .findCaseBy("data.caseReference", CASE_REF, idamTokens);

        verify(coreCaseDataApi).searchCases(
            eq("idamOauth2Token"),
            eq("serviceAuthorization"),
            anyString(),
            eq(query.toString())
        );

        assertEquals("expected one case only", 1, cases.size());
    }

}
