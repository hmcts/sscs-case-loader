package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;

public class SearchCcdServiceTest {

    private final SearchCcdServiceByCaseRef searchCcdServiceByCaseRef = mock(SearchCcdServiceByCaseRef.class);
    private final SearchCcdServiceByCaseId searchCcdServiceByCaseId = mock(SearchCcdServiceByCaseId.class);

    private final SearchCcdService searchCcdService = new SearchCcdService(
        searchCcdServiceByCaseRef,
        searchCcdServiceByCaseId
    );

    @Test
    public void givenACaseSearchByCaseRef_shouldDelegateSearch() {

        IdamTokens idamTokens = mock(IdamTokens.class);

        searchCcdService.findCaseByCaseRef("ref", idamTokens);

        verify(searchCcdServiceByCaseRef).findCaseByCaseRef(
            eq("ref"),
            eq(idamTokens)
        );
    }

    @Test
    public void givenACaseSearchByCaseId_shouldDelegateSearch() {

        IdamTokens idamTokens = mock(IdamTokens.class);

        searchCcdService.findCaseByCaseId("id", idamTokens);

        verify(searchCcdServiceByCaseId).findCaseByCaseId(
            eq("id"),
            eq(idamTokens)
        );
    }

}
