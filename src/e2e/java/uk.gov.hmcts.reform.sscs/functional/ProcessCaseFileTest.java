package uk.gov.hmcts.reform.sscs.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import io.restassured.RestAssured;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.ccd.util.CaseDataUtils;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;
import uk.gov.hmcts.reform.tools.GenerateXml;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:config/application_e2e.yaml")
@SpringBootTest
@Slf4j
public class ProcessCaseFileTest {

    @Value("${test.url}")
    private String testUrl;

    private static final String outputdir = "src/test/resources/updates";

    @Autowired
    private SftpChannelAdapter sftpChannelAdapter;
    @Autowired
    private CcdService ccdService;
    @Autowired
    private IdamService idamService;

    private String ccdCaseId;
    private IdamTokens idamTokens;

    @Before
    public void setup() throws ParserConfigurationException, TransformerException, IOException, SftpException {

        String oauth2Token = idamService.getIdamOauth2Token();
        idamTokens = IdamTokens.builder()
            .idamOauth2Token(oauth2Token)
            .serviceAuthorization(idamService.generateServiceAuthorization())
            .userId(idamService.getUserId(oauth2Token))
            .build();

        SscsCaseData caseData = CaseDataUtils.buildMinimalCaseData();
        SscsCaseDetails caseDetails = ccdService.createCase(caseData, "appealCreated", "caseloader test summary",
            "caseloader test description", idamTokens);
        ccdCaseId = String.valueOf(caseDetails.getId());
        log.info("Created test ccd case with id {}", ccdCaseId);

        String path = Objects.requireNonNull(getClass().getClassLoader()
            .getResource("SSCS_CcdCases_Delta_2018-07-09-12-34-56.xml")).getFile();
        String ccdCasesXml = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8.name());
        ccdCasesXml = ccdCasesXml.replace("1_CCD_ID_REPLACED_BY_TEST", ccdCaseId);

        cleanSftpFiles();
        writeXmlToSftp(ccdCasesXml);
        GenerateXml.generateXmlForAppeals();
        copy(outputdir);
    }

    @After
    public void teardown() throws IOException, ParserConfigurationException, SftpException {
        cleanSftpFiles();
        GenerateXml.cleanUpOldFiles();
    }

    private void cleanSftpFiles() throws SftpException {

        ChannelSftp sftpChannel = sftpChannelAdapter.getSftpChannel();
        try {
            sftpChannel.rm("/incoming/SSCS_CcdCases_Delta_*.xml");
            sftpChannel.rm("/incoming/SSCS_CreateAppeals_Delta_*.xml");
            sftpChannel.rm("/incoming/failed/SSCS_CcdCases_Delta_*.xml");
            sftpChannel.rm("/incoming/failed/SSCS_CreateAppeals_Delta_*.xml");
            sftpChannel.rm("/incoming/processed/SSCS_CcdCases_Delta_*.xml");
            sftpChannel.rm("/incoming/processed/SSCS_CreateAppeals_Delta_*.xml");
            sftpChannel.rm("/incoming/processed/SSCS_Extract_Reference_2018-02-13-20-09-33.xml");
            sftpChannel.rm("/incoming/processed/SSCS_Extract_Delta_2018-05-24-16-14-19.xml");
            sftpChannel.rm("/incoming/processed/SSCS_Extract_Delta_SmokeTest_2018-03-15-16-14-19.xml");
            sftpChannel.rm("/incoming/processed/SSCS_Extract_Delta_No_Parties_2018-11-30-23-34-59.xml");
        } catch (SftpException e) {
            if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                throw e;
            }
        }
    }

    private void writeXmlToSftp(String xml) throws SftpException {
        ChannelSftp sftpChannel = sftpChannelAdapter.getSftpChannel();
        sftpChannel.put(new ByteArrayInputStream(xml.getBytes()),
            "/incoming/" + "SSCS_CcdCases_Delta_2018-07-09-12-34-56.xml");
    }

    public void copy(String outputdir) {
        ChannelSftp sftpChannel = sftpChannelAdapter.getSftpChannel();
        try {
            File folder = new File(outputdir);
            File[] files = folder.listFiles();
            for (File file : Objects.requireNonNull(files)) {
                sftpChannel.put(new FileInputStream(file), file.getName()); //NOPMD
            }
        } catch (SftpException e) {
            throw new SftpCustomException("Failed to copy/delete generated xml to sftp", outputdir, e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void processCaseFileAndVerifyCcd() {

        RestAssured.baseURI = testUrl;

        RestAssured.useRelaxedHTTPSValidation();
        RestAssured
            .given()
            .when()
            .get("/functional-test")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();

        SscsCaseDetails updatedCcdCase = ccdService.getByCaseId(Long.parseLong(ccdCaseId), idamTokens);
        assertNotNull(updatedCcdCase);

        SscsCaseData updatedCcdCaseData = updatedCcdCase.getData();

        assertEquals("XYZ", updatedCcdCaseData.getAppeal().getAppellant().getName().getFirstName());
        assertEquals(3, updatedCcdCaseData.getEvents().size());
        assertEquals("appealCreated", updatedCcdCase.getState());
    }
}
