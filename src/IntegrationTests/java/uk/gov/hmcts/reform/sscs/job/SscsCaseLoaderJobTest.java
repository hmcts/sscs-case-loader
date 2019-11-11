package uk.gov.hmcts.reform.sscs.job;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(JUnitParamsRunner.class)
@SpringBootTest
public class SscsCaseLoaderJobTest {

    // Below rules are needed to use the junitParamsRunner together with SpringRunner
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private CaseLoaderService caseLoaderService;

    @Autowired
    private SscsCaseLoaderJob sscsCaseLoaderJob;

    @Test
    @Parameters({
        "STAGING, 0",
        "PRODUCTION, 1",
        "dev , 0"
    })
    public void givenHostname_shouldRunTheProcessOnlyIfItIsProduction(String host, int times) {
        sscsCaseLoaderJob.run();
        verify(caseLoaderService, times(times)).process();

    }
}
