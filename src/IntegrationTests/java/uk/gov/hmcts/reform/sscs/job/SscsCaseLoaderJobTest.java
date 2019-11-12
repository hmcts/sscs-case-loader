package uk.gov.hmcts.reform.sscs.job;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SscsCaseLoaderJobTest {

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private CaseLoaderService caseLoaderService;

    @Autowired
    private SscsCaseLoaderJob sscsCaseLoaderJob;

    @Test
    @Ignore
    public void givenHostname_shouldRunTheProcessOnlyIfItIsProduction() {
        sscsCaseLoaderJob.run();
        verify(caseLoaderService, times(2)).process();

    }
}
