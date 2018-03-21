package uk.gov.hmcts.reform.sscs.services;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.exceptions.GapsValidationException;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@RunWith(SpringRunner.class)
@SpringBootTest
public class XmlValidatorTest {

    @MockBean
    private SftpSshService sftpSshService;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @Autowired
    private XmlValidator validator;
    private Gaps2File deltaFile;
    private Gaps2File refFile;
    private Gaps2File invalidDelta;


    @Before
    public void setUp() {
        deltaFile = new Gaps2File("SSCS_Extract_Delta_2017-05-24-16-14-19.xml");
        refFile = new Gaps2File("SSCS_Extract_Reference_2017-05-24-16-14-19.xml");
        invalidDelta = new Gaps2File("SSCS_ExtractInvalid_Delta_2017-06-30-09-25-56.xml");
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(sftpSshService);
    }

    @Test
    public void shouldValidateContentGivenValidDeltaInputStream() {
        when(sftpSshService.readExtractFile(deltaFile)).thenReturn(getStream(deltaFile));

        validator.validateXml(deltaFile);

        verify(sftpSshService).readExtractFile(deltaFile);
    }

    @Test
    public void shouldPassValidatorGivenValidRefXmlInputStream() {
        when(sftpSshService.readExtractFile(refFile)).thenReturn(getStream(refFile));

        validator.validateXml(refFile);

        verify(sftpSshService).readExtractFile(refFile);
    }

    @Test
    public void shouldFailValidatorGivenInvalidRefXmlFile() {
        when(sftpSshService.readExtractFile(invalidDelta)).thenReturn(getStream(invalidDelta));

        try {
            validator.validateXml(invalidDelta);
            fail();
        } catch (GapsValidationException e) { //NOPMD

        }

        verify(sftpSshService).readExtractFile(invalidDelta);
        verify(sftpSshService).move(invalidDelta, false);
    }

    private InputStream getStream(Gaps2File file) {
        return getClass().getClassLoader().getResourceAsStream(file.getName());
    }
}
