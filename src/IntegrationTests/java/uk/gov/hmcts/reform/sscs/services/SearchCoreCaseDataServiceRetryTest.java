package uk.gov.hmcts.reform.sscs.services;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCoreCaseDataService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchCoreCaseDataServiceRetryTest {

    @Autowired
    private SearchCoreCaseDataService searchCoreCaseDataService;

    @Test
    @Ignore
    public void givenFindCaseByCaseRefThrowsException_shouldRetryPolicyBeInPlace() {
        List<CaseDetails> result = searchCoreCaseDataService.findCaseByCaseRef("caseRef",
            "idamOauth2Token", "serviceAuthorization");
        verify(searchCoreCaseDataService, times(3))
            .findCaseByCaseRef(anyString(), anyString(), anyString());
        assertNotNull(result);
    }

    @Configuration
    @EnableRetry
    public static class SpringConfig {

        @Bean
        public SearchCoreCaseDataService searchCoreCaseDataService() throws Exception {
            SearchCoreCaseDataService searchCoreCaseDataService = mock(SearchCoreCaseDataService.class);
            when(searchCoreCaseDataService.findCaseByCaseRef(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Remote Exception 1"))
                .thenThrow(new RuntimeException("Remote Exception 2"))
                .thenReturn(Collections.singletonList(CaseDetails.builder().build()));
            return searchCoreCaseDataService;
        }
    }

}
