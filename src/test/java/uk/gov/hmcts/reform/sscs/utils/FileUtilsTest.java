package uk.gov.hmcts.reform.sscs.utils;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.exceptions.FailedToReadFromFileException;

public class FileUtilsTest {
    @Test(expected = FailedToReadFromFileException.class)
    public void givenFilePath_shouldGetFileContent() {
        FileUtils.getFileContentGivenFilePath("noExistingFile.xml");
    }

}
