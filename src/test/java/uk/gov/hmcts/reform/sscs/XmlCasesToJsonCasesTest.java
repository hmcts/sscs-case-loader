package uk.gov.hmcts.reform.sscs;


import org.json.JSONObject;
import org.json.XML;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class XmlCasesToJsonCasesTest {

    private String getFileContentGivenFilePath(String filePath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    @Test
    public void shouldTransformXmlToJsonTest() {
        JSONObject jsonOutput = XML.toJSONObject(
            getFileContentGivenFilePath("src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml"));
        String jsonOutputPrettyPrint = jsonOutput.toString(4);
        System.out.println(jsonOutputPrettyPrint);
    }
}
