package uk.gov.hmcts.reform.sscs.utils;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.exceptions.FailedToReadFromFileException;

public class FileUtilsTest {

    public static final String DELTA_XML = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";

    @Test(expected = FailedToReadFromFileException.class)
    public void givenNonExistingResourceName_shouldThrowException() {
        FileUtils.getFileContentGivenFilePath("noExistingFile.xml");
    }

    @Test
    public void givenResourceName_shouldGetContent() {
        String content = FileUtils.getFileContentGivenFilePath(
            DELTA_XML);
        assertNotNull(content);
    }

    @Test
    public void givenStringPath_shouldReturnInputStream_() {
        InputStream is = FileUtils.getInputStreamGivenFilePath(DELTA_XML);
        assertNotNull(is);
    }
}
