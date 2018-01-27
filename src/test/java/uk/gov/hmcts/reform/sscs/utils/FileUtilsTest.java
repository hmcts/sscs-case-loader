package uk.gov.hmcts.reform.sscs.utils;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.exceptions.FailedToReadResourceException;

import static org.junit.Assert.assertNotNull;

public class FileUtilsTest {
    @Test(expected = FailedToReadResourceException.class)
    public void givenNonExistingResourceName_shouldThrowException() {
        new FileUtils().getResourceContentGivenResourceName("/noExistingFile.xml");
    }

    @Test
    public void givenResourceName_shouldGetContent() {
        String content = new FileUtils()
            .getResourceContentGivenResourceName("/SSCS_Extract_Delta_2017-05-24-16-14-19.json");
        assertNotNull(content);
    }
}
