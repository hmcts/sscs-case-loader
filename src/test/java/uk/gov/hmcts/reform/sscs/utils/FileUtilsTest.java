package uk.gov.hmcts.reform.sscs.utils;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.exceptions.FailedToReadFromFileException;

public class FileUtilsTest {
    @Test(expected = FailedToReadFromFileException.class)
    public void givenNonExistingResourceName_shouldThrowException() {
        FileUtils.getFileContentGivenFilePath("noExistingFile.xml");
    }

    @Test
    public void givenResourceName_shouldGetContent() {
        String content = FileUtils.getFileContentGivenFilePath(
            "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml");
        assertNotNull(content);
    }
}
