package uk.gov.hmcts.reform.sscs.services.mapper;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Test;

public class TransformXmlFilesToJsonFilesTest {

    private static final String DELTA_XML = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
    private static final String REF_XML = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.xml";
    private static final String DELTA_JSON = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json";
    private static final String REF_JSON = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.json";

    private final TransformXmlFilesToJsonFiles transformXmlFilesToJsonFiles =
        new TransformXmlFilesToJsonFiles();

    @Test
    public void givenXmlFiles_shouldTransformToJsonFiles() throws IOException {
        //Given
        String deltaAsString = FileUtils.readFileToString(new File(DELTA_XML), StandardCharsets.UTF_8.name());
        String refAsString = FileUtils.readFileToString(new File(REF_XML), StandardCharsets.UTF_8.name());

        //When
        JSONObject actualDeltaJson = transformXmlFilesToJsonFiles.transform(deltaAsString);
        JSONObject actualRefJson = transformXmlFilesToJsonFiles.transform(refAsString);

        //Should
        String expectedDeltaJson = FileUtils.readFileToString(new File(DELTA_JSON), StandardCharsets.UTF_8.name());
        String expectedRefJson = FileUtils.readFileToString(new File(REF_JSON), StandardCharsets.UTF_8.name());

        assertJsonEquals(expectedDeltaJson, actualDeltaJson);
        assertJsonEquals(expectedRefJson, actualRefJson);
    }
}
