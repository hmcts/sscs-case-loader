package uk.gov.hmcts.reform.sscs.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SscsCaseLoaderSchedulerTest {

    @MockBean
    SftpChannelAdapter channelAdapter;

    @MockBean
    private CaseLoaderService caseLoaderService;

    @Autowired
    private SscsCaseLoaderScheduler sscsCaseLoaderScheduler;

    @Test
    public void givenHttpPostIsStaging_shouldNotRunTheProcess() {
        sscsCaseLoaderScheduler.run();
        verify(caseLoaderService, times(0)).process();
    }
}
