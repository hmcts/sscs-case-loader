package uk.gov.hmcts.reform.sscs.functional;

import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import io.restassured.RestAssured;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.util.CaseDataUtils;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.ccd.CreateCcdService;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCcdServiceByCaseId;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;
import uk.gov.hmcts.reform.tools.GenerateXml;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class ProcessCaseFile {

    private static final org.slf4j.Logger LOG = getLogger(ProcessCaseFile.class);

    private static final String caseloaderinstance = System.getenv("TEST_URL");
    private static final String localInstance = "http://localhost:8082";

    String filename;
    private static final String outputdir = "src/test/resources/updates";

    @Autowired
    private SftpChannelAdapter sftpChannelAdapter;
    @Autowired
    private CreateCcdService createCcdService;
    @Autowired
    private SearchCcdServiceByCaseId searchCcdServiceByCaseId;
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
        CaseDetails caseDetails = createCcdService.create(caseData, idamTokens);
        ccdCaseId = String.valueOf(caseDetails.getId());
        LOG.info("Created test ccd case with id {}", ccdCaseId);

        String path = getClass().getClassLoader().getResource("SSCS_CcdCases_Delta_2018-07-09-12-34-56.xml").getFile();
        String ccdCasesXml = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8.name());
        ccdCasesXml = ccdCasesXml.replace("1_CCD_ID_REPLACED_BY_TEST", ccdCaseId);

        cleanSftpFiles();
        writeXmlToSftp(ccdCasesXml, "SSCS_CcdCases_Delta_2018-07-09-12-34-56.xml");
        GenerateXml.generateXmlForAppeals();
        copy(outputdir, filename);
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
        } catch (SftpException e) {
            if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                throw e;
            }
        }
    }

    private void writeXmlToSftp(String xml, String filename) throws SftpException {
        ChannelSftp sftpChannel = sftpChannelAdapter.getSftpChannel();
        sftpChannel.put(new ByteArrayInputStream(xml.getBytes()), "/incoming/" + filename);
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
            throw new SftpCustomException("Failed to copy/delete generated xml to sftp", filename, e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void processCaseFileAndVerifyCcd() {

        RestAssured.baseURI =
            StringUtils.isBlank(caseloaderinstance) ? localInstance : caseloaderinstance;

        RestAssured.useRelaxedHTTPSValidation();
        RestAssured
            .given()
            .when()
            .get("/functional-test")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();

        List<CaseDetails> updatedCcdCase = searchCcdServiceByCaseId.findCaseByCaseId(ccdCaseId, idamTokens);
        assertEquals(1, updatedCcdCase.size());

        Map<String, Object> updatedCaseData = updatedCcdCase.get(0).getData();
        Map<String, Object> appealData = (Map<String, Object>) updatedCaseData.get("appeal");
        Map<String, Object> appellantData = (Map<String, Object>) appealData.get("appellant");
        Map<String, Object> appellantNameData = (Map<String, Object>) appellantData.get("name");

        assertEquals("XYZ", appellantNameData.get("firstName"));
        assertEquals(3, ((ArrayList) updatedCaseData.get("events")).size());
    }
}
