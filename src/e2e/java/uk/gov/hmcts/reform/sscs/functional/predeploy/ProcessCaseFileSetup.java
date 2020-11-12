package uk.gov.hmcts.reform.sscs.functional.predeploy;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.Identity;
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
public class ProcessCaseFileSetup {

    private static final String outputdir = "src/test/resources/updates";
    private static final String CASE_REF_TEST_1 = "SC001/19/";
    private static final String CASE_REF_TEST_2 = "SC001/20/";

    @Autowired
    private SftpChannelAdapter sftpChannelAdapter;
    @Autowired
    private CcdService ccdService;
    @Autowired
    private IdamService idamService;

    private String ccdCaseId;
    private IdamTokens idamTokens;

    @Test
    public void setup() throws ParserConfigurationException, TransformerException, IOException, SftpException {

        log.info("Getting oAuth2 token...");
        String oauth2Token = idamService.getIdamOauth2Token();

        log.info("Building IDAM tokens...");
        idamTokens = IdamTokens.builder()
            .idamOauth2Token(oauth2Token)
            .serviceAuthorization(idamService.generateServiceAuthorization())
            .userId(idamService.getUserId(oauth2Token))
            .build();

        int randomNumber = (int) (Math.random() * 1000000);
        // Case 1 is created to cater for the scenarios of elastic search issue which was returning multiple cases
        // on case reference search. For more details see https://tools.hmcts.net/jira/browse/SSCS-8383
        // Also, make sure case 1 does not overwrite case 2 and case 2 updates successfully
        log.info("Building minimal case1 data...");
        SscsCaseData caseDataCase1 = CaseDataUtils.buildMinimalCaseData();
        caseDataCase1.getAppeal().getAppellant().setIdentity(Identity.builder()
            .nino("AB 77 88 88 B").dob("1904-03-10").build());
        caseDataCase1.setCaseReference(CASE_REF_TEST_1 + randomNumber);

        log.info("Creating CCD case1...");
        ccdService.createCase(caseDataCase1, "appealCreated", "caseloader test summary",
            "caseloader test description", idamTokens);

        log.info("Building minimal case2 data...");
        SscsCaseData caseDataCase2 = CaseDataUtils.buildMinimalCaseData();
        caseDataCase2.setCaseReference(CASE_REF_TEST_2 + randomNumber);

        log.info("Creating CCD case2...");
        SscsCaseDetails caseDetailsCase2 = ccdService.createCase(caseDataCase2,
            "appealCreated", "caseloader test summary",
            "caseloader test description", idamTokens);

        ccdCaseId = String.valueOf(caseDetailsCase2.getId());
        log.info("Created test ccd case with id {}", ccdCaseId);

        String tmpFileName = "ccdCaseId.tmp";

        Files.write(Paths.get(tmpFileName), ccdCaseId.getBytes());

        String path = Objects.requireNonNull(getClass().getClassLoader()
            .getResource("SSCS_CcdCases_Delta_2018-07-09-12-34-56.xml")).getFile();
        String ccdCasesXml = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8.name());
        ccdCasesXml = ccdCasesXml.replace("1_CCD_ID_REPLACED_BY_TEST", ccdCaseId);
        ccdCasesXml = ccdCasesXml.replace("1_CCD_REF_REPLACED_BY_TEST", CASE_REF_TEST_2 + randomNumber);

        cleanSftpFiles();
        writeXmlToSftp(ccdCasesXml);
        GenerateXml.generateXmlForAppeals();
        copy(outputdir);
    }

    private void cleanSftpFiles() throws SftpException {

        ChannelSftp sftpChannel = sftpChannelAdapter.openConnectedChannel();
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
        ChannelSftp sftpChannel = sftpChannelAdapter.openConnectedChannel();
        sftpChannel.put(new ByteArrayInputStream(xml.getBytes()),
            "/incoming/" + "SSCS_CcdCases_Delta_2018-07-09-12-34-56.xml");
    }

    public void copy(String outputdir) {
        ChannelSftp sftpChannel = sftpChannelAdapter.openConnectedChannel();
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

}
