package uk.gov.hmcts.reform.sscs.services.xml;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.exceptions.GapsValidationException;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;

@RunWith(MockitoJUnitRunner.class)
public class XmlValidatorTest {

    @Mock
    private SftpSshService sftpSshService;

    private XmlValidator validator;

    private Gaps2File deltaFile;
    private Gaps2File refFile;
    private Gaps2File invalidDelta;

    @Before
    public void setUp() {
        deltaFile = new Gaps2File("SSCS_Extract_Delta_2017-05-24-16-14-19.xml", 10L);
        refFile = new Gaps2File("SSCS_Extract_Reference_2017-05-24-16-14-19.xml", 10L);
        invalidDelta = new Gaps2File("SSCS_ExtractInvalid_Delta_2017-06-30-09-25-56.xml", 10L);

        validator = new XmlValidator(sftpSshService);
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
