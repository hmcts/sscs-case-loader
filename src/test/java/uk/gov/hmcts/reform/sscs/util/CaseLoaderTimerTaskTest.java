package uk.gov.hmcts.reform.sscs.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;

@RunWith(MockitoJUnitRunner.class)
public class CaseLoaderTimerTaskTest {

    @Mock
    private SftpSshService sftpSshService;
    private CaseLoaderTimerTask caseLoaderTimerTask;

    @Before
    public void setUp() {
        caseLoaderTimerTask = new CaseLoaderTimerTask(sftpSshService);
    }

    @Test
    public void shouldRun() {
        caseLoaderTimerTask.run();
        Mockito.verify(sftpSshService).closeChannelAdapter();
    }

}
