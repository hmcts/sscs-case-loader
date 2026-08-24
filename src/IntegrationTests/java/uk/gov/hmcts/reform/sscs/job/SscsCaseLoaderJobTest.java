package uk.gov.hmcts.reform.sscs.job;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@SpringBootTest
@RunWith(SpringRunner.class)
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.idam.client"
})

public class SscsCaseLoaderJobTest {

    @MockitoBean
    private SftpChannelAdapter channelAdapter;

    @MockitoBean
    private CaseLoaderService caseLoaderService;

    @Autowired
    private SscsCaseLoaderJob sscsCaseLoaderJob;

    @Test
    public void givenHostname_shouldRunTheProcessOnlyIfItIsProduction() {
        sscsCaseLoaderJob.run();
        verify(caseLoaderService, atLeastOnce()).process();

    }
}
