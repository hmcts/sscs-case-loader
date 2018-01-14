package uk.gov.hmcts.reform.sscs.services;

import org.json.JSONObject;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertTrue;

public class CaseLoaderServiceTest {

    private final CaseLoaderService caseLoaderService = new SftpCaseLoaderImpl();

    @Test
    public void shouldFetchXmlFilesWhenSentFromGaps2() {
        assertTrue(caseLoaderService.fetchXmlFilesFromGaps2());
    }

    @Test
    public void shouldValidateXmlFiles() {
        assertTrue(caseLoaderService.validateXmlFiles());

    }

    @Test
    public void shouldTransformXmlFilesToJsonFiles() {
        JSONObject actualJson = caseLoaderService.transformXmlFilesToJson(
            "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml");
        String expectedJson = FileUtils.getFileContentGivenFilePath(
            "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json");
        assertJsonEquals(expectedJson, actualJson);
    }

}
