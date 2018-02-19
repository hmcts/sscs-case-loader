package uk.gov.hmcts.reform.sscs.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;


public class SscsCaseLoaderSchedulerTest {

    @Mock
    CaseLoaderService caseLoaderService;

    private SscsCaseLoaderScheduler sscsCaseLoaderScheduler;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        sscsCaseLoaderScheduler = new SscsCaseLoaderScheduler(caseLoaderService);
    }

    @Test
    public void shouldRunCaseLoader() {
        sscsCaseLoaderScheduler.run();
        verify(caseLoaderService).process();
    }
}
