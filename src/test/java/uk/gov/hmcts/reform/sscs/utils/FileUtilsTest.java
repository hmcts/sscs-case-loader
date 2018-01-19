package uk.gov.hmcts.reform.sscs.utils;

import org.junit.Test;

public class FileUtilsTest {
    @Test(expected = RuntimeException.class)
    public void givenFilePath_shouldGetFileContent() {
        FileUtils.getFileContentGivenFilePath("noExistingFile.xml");
    }

}
