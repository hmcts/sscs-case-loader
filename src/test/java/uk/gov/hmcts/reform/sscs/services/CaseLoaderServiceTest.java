package uk.gov.hmcts.reform.sscs.services;

import org.json.JSONObject;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import java.util.Optional;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CaseLoaderServiceTest {

    private static final String DELTA_XML = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
    private final XmlFiles xmlFiles = XmlFiles.builder().delta(DELTA_XML).build();
    private final CaseLoaderService caseLoaderService = new SftpCaseLoaderImpl();

    @Test
    public void shouldFetchXmlFilesWhenSentFromGaps2() {
        Optional<XmlFiles> optionalXmlFiles = caseLoaderService.fetchXmlFilesFromGaps2();
        assertFalse(optionalXmlFiles.isPresent());
    }

    @Test
    public void shouldValidateXmlFiles() {
        assertTrue(caseLoaderService.validateXmlFiles(xmlFiles));
    }

    @Test
    public void shouldTransformXmlFilesToJsonFiles() {
        JSONObject actualJson = caseLoaderService.transformXmlFilesToJson(xmlFiles);
        String deltaJson = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json";
        String expectedJson = FileUtils.getFileContentGivenFilePath(deltaJson);
        assertJsonEquals(expectedJson, actualJson);
    }

}
