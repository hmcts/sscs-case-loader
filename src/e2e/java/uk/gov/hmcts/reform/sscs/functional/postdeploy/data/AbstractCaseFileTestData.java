package uk.gov.hmcts.reform.sscs.functional.postdeploy.data;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sscs.ccd.domain.Identity;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.ccd.util.CaseDataUtils;
import uk.gov.hmcts.reform.sscs.functional.predeploy.PreDeployableTestData;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;
import uk.gov.hmcts.reform.tools.utils.CaseIdMapUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Slf4j
public abstract class AbstractCaseFileTestData implements PreDeployableTestData {

    private static final String CASE_REF_TEST_1 = "SC001/19/";
    private static final String CASE_REF_TEST_2 = "SC001/20/";

    @Autowired
    private IdamService idamService;
    @Autowired
    private SftpChannelAdapter sftpChannelAdapter;
    @Autowired
    private CcdService ccdService;

    public abstract SscsCaseDetails getTestCase() throws IOException, ClassNotFoundException;



    protected void writeXmlToSftp(String ccdCaseId, String fileName) throws SftpException, IOException {
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile();
        String ccdCasesXml = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8.name());
        ccdCasesXml = ccdCasesXml.replace("1_CCD_ID_REPLACED_BY_TEST", ccdCaseId);

        ChannelSftp sftpChannel = sftpChannelAdapter.openConnectedChannel();
        sftpChannel.put(new ByteArrayInputStream(ccdCasesXml.getBytes()),"/incoming/" + fileName);
    }

    protected String createCcdCase(){
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
            "caseloader test description", getIdamTocken());

        log.info("Building minimal case2 data...");
        SscsCaseData caseDataCase2 = CaseDataUtils.buildMinimalCaseData();
        caseDataCase2.setCaseReference(CASE_REF_TEST_2 + randomNumber);

        log.info("Creating CCD case2...");
        SscsCaseDetails caseDetailsCase2 = ccdService.createCase(caseDataCase2,
            "appealCreated", "caseloader test summary",
            "caseloader test description", getIdamTocken());

        String ccdCaseId = String.valueOf(caseDetailsCase2.getId());
        log.info("Created test ccd case with id {}", ccdCaseId);
        return ccdCaseId;
    }

    protected SscsCaseDetails getCaseByKey(String caseKey) throws IOException, ClassNotFoundException {
        Map<String, String> idStore = CaseIdMapUtils.read();
        String ccdCaseId = idStore.get(caseKey);
        log.info("Test case ID is {}", ccdCaseId);
        return ccdService.getByCaseId(Long.parseLong(ccdCaseId), getIdamTocken());
    }

    private IdamTokens getIdamTocken(){
        log.info("Getting oAuth2 token...");
        String oauth2Token = idamService.getIdamOauth2Token();

        log.info("Building IDAM tokens...");
        return IdamTokens.builder()
            .idamOauth2Token(oauth2Token)
            .serviceAuthorization(idamService.generateServiceAuthorization())
            .userId(idamService.getUserId(oauth2Token))
            .build();
    }
}
