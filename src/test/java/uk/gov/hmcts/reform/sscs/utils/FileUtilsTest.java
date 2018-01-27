package uk.gov.hmcts.reform.sscs.utils;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.exceptions.FailedToReadResourceException;

public class FileUtilsTest {
    @Test(expected = FailedToReadResourceException.class)
    public void givenFilePath_shouldGetFileContent() {
        FileUtils.getFileContentGivenFilePath("noExistingFile.xml");
    }

}
