package uk.gov.hmcts.reform.sscs.functional;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import io.restassured.RestAssured;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;
import uk.gov.hmcts.reform.tools.GenerateXml;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class ProcessCaseFile {

    private final String caseloaderinstance = System.getenv("TEST_URL");
    private String filename;
    private String outputdir = "src/test/resources/updates";

    @Autowired
    private SftpChannelAdapter sftpChannelAdapter;

    @Before
    public void setup() throws ParserConfigurationException, TransformerException, IOException {
        GenerateXml.generateXmlForAppeals();
        copy(outputdir, filename);
    }

    @After
    public void teardown() throws IOException, ParserConfigurationException {
        GenerateXml.cleanUpOldFiles();

    }

    private void copy(String outputdir, String filename) {
        ChannelSftp sftpChannel = sftpChannelAdapter.getSftpChannel();
        try {
            File folder = new File(outputdir);
            File[] files = folder.listFiles();
            for (File file : files) {
                sftpChannel.put(new FileInputStream(file), file.getName()); //NOPMD
            }
        } catch (SftpException e) {
            throw new SftpCustomException("Failed to copy generated xml to sftp", filename, e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void processCaseFileAndVerifyCcd() {
        RestAssured.baseURI = caseloaderinstance;

        RestAssured.useRelaxedHTTPSValidation();
        RestAssured
                .given()
                .when()
                .get("/functional-test")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract().body().asString();
    }
}



