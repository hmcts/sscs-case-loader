package uk.gov.hmcts.reform.sscs.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CaseLoaderTimerTaskTest {

    private final CaseLoaderTimerTask caseLoaderTimerTask = new CaseLoaderTimerTask();

    @Test
    public void shouldRun() {
        caseLoaderTimerTask.run();
        assertTrue(true);
    }

}
