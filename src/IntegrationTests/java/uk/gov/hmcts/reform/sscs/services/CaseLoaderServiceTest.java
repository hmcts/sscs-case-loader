package uk.gov.hmcts.reform.sscs.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformJsonCasesToCaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformXmlFilesToJsonFiles;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CaseLoaderServiceTest {

    @MockBean
    private SftpSshService sftpSshService;
    @SpyBean
    private XmlValidator xmlValidator;
    @SpyBean
    private TransformXmlFilesToJsonFiles transformXmlFilesToJsonFiles;
    @SpyBean
    private TransformJsonCasesToCaseData transformJsonCasesToCaseData;
    @SpyBean
    private CoreCaseDataService coreCaseDataService;
    @Autowired
    private CaseLoaderService caseLoaderService;

    private static final String DELTA_XML = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";

    @Test
    public void givenDeltaXmlInSftp_shouldBeSavedIntoCcd() throws IOException {
        List<InputStream> inputStreamList = new ArrayList<>(1);
        InputStream deltaAsStream = FileUtils.openInputStream(new File(DELTA_XML));
        inputStreamList.add(deltaAsStream);

        given(sftpSshService.readExtractFiles()).willReturn(inputStreamList);

        caseLoaderService.process();

        assertTrue(true);
    }
}
