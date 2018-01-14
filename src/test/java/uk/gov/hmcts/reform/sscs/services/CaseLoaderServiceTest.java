package uk.gov.hmcts.reform.sscs.services;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.JsonFiles;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import java.util.Optional;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CaseLoaderServiceTest {

    private static final String DELTA_XML = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
    private static final String REF_XML = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.xml";
    private static final String DELTA_JSON = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json";
    private static final String REF_JSON = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.json";

    private final XmlFiles xmlFiles = XmlFiles.builder().delta(DELTA_XML).ref(REF_XML).build();

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
        JsonFiles actualJsonFiles = caseLoaderService.transformXmlFilesToJsonFiles(xmlFiles);

        String expectedDeltaJson = FileUtils.getFileContentGivenFilePath(DELTA_JSON);
        String expectedRefJson = FileUtils.getFileContentGivenFilePath(REF_JSON);

        assertJsonEquals(expectedDeltaJson, actualJsonFiles.getDelta());
        assertJsonEquals(expectedRefJson, actualJsonFiles.getRef());
    }

}
