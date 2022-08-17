package uk.gov.hmcts.reform.sscs.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SpringBootTest
@RunWith(SpringRunner.class)
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.idam.client"
})

public class SscsCaseLoaderJobTest {

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private CaseLoaderService caseLoaderService;

    @Autowired
    private SscsCaseLoaderJob sscsCaseLoaderJob;

    @Test
    public void givenHostname_shouldRunTheProcessOnlyIfItIsProduction() {
        sscsCaseLoaderJob.run();
        verify(caseLoaderService, atLeastOnce()).process();

    }
}
