package uk.gov.hmcts.reform.sscs.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SscsCaseLoaderSchedulerTest {

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private CaseLoaderService caseLoaderService;

    @Autowired
    private SscsCaseLoaderScheduler sscsCaseLoaderScheduler;

    @Test
    public void givenHttpPostIsStaging_shouldNotRunTheProcess() {
        ReflectionTestUtils.setField(sscsCaseLoaderScheduler, "httpHost",
            "sscs-case-loader-prod-staging.scm.service.core-compute-prod.internal");
        sscsCaseLoaderScheduler.run();
        verify(caseLoaderService, times(0)).process();

    }

    @Test
    public void givenHttpPostIsNotStaging_shouldRunTheProcess() {
        ReflectionTestUtils.setField(sscsCaseLoaderScheduler, "httpHost",
            "sscs-case-loader-prod.scm.service.core-compute-prod.internal");
        sscsCaseLoaderScheduler.run();
        verify(caseLoaderService, times(1)).process();
    }
}
