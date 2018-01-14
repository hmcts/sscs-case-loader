package uk.gov.hmcts.reform.sscs.utils;

import org.junit.Test;

public class FileUtilsTest {
    @Test(expected = RuntimeException.class)
    public void shouldGetFileContent() {
        FileUtils.getFileContentGivenFilePath("noExistingFile.xml");
    }

}
