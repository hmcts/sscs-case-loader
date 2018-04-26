package uk.gov.hmcts.reform.sscs.functional;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import io.restassured.RestAssured;
import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.configuration.ConfigurationException;
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
import uk.gov.hmcts.reform.tools.generatexml;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class ProcessCaseFile {

    private final String caseloaderinstance = System.getenv("TEST_URL");
    String filename;
    String OUTPUT_DIR="src/test/resources/updates";


    @Autowired
    private SftpChannelAdapter sftpChannelAdapter;


    @Before
    public void setup() throws ParserConfigurationException, TransformerException, IOException, ConfigurationException {
        generatexml.generatexmlforappeals();
        copy(OUTPUT_DIR, filename);
    }
    @After
    public void teardown() throws IOException, ParserConfigurationException {
        generatexml.cleanUpOldFiles();

    }

    public void copy(String OUTPUT_DIR, String filename) {
        ChannelSftp sftpChannel = sftpChannelAdapter.getSftpChannel();
        try {
            File folder = new File(OUTPUT_DIR);
            File[] files = folder.listFiles();
            InputStream ins = null;
            for (File file : files) {
                filename = file.getName();
                ins = new FileInputStream(file);

            }
            sftpChannel.put(ins, filename);
        } catch (SftpException e) {
            throw new SftpCustomException("Failed to copy generated xml to sftp", filename, e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void processCaseFileandVerifyCcd() {
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



