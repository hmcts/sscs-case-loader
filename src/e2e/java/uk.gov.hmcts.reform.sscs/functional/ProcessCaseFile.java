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
import uk.gov.hmcts.reform.tools.GenerateXml;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class ProcessCaseFile {

    private final String caseloaderinstance = System.getenv("TEST_URL");
    String filename;
    private static final String outputdir = "src/test/resources/updates";
    private static final String processedReference = "/incoming/processed/SSCS_Extract_Reference_2018-02-13-20-09-33.xml";

    @Autowired
    private SftpChannelAdapter sftpChannelAdapter;


    @Before
    public void setup() throws ParserConfigurationException, TransformerException, IOException,
        ConfigurationException, SftpException {
        GenerateXml.generateXmlForAppeals();
        copy(outputdir, filename);
        ChannelSftp sftpChannel = sftpChannelAdapter.getSftpChannel();
        try {
            sftpChannel.lstat(processedReference);
            sftpChannel.rm(processedReference);
        } catch (SftpException e) {
            if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                throw e;
            }
        }
    }

    @After
    public void teardown() throws IOException, ParserConfigurationException {
        GenerateXml.cleanUpOldFiles();
    }

    public void copy(String outputdir, String filename) {
        ChannelSftp sftpChannel = sftpChannelAdapter.getSftpChannel();
        try {
            File folder = new File(outputdir);
            File[] files = folder.listFiles();
            for (File file : files) {
                sftpChannel.put(new FileInputStream(file), file.getName()); //NOPMD
            }
        } catch (SftpException e) {
            throw new SftpCustomException("Failed to copy/deleteO generated xml to sftp", filename, e);
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



