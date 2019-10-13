package uk.gov.hmcts.reform.sscs.functional.postdeploy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Scanner;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:config/application_e2e.yaml")
@SpringBootTest
@Slf4j
public class ProcessCaseFileTest {

    @Autowired
    private CcdService ccdService;
    @Autowired
    private IdamService idamService;

    private String ccdCaseId;
    private IdamTokens idamTokens;

    @SuppressWarnings("unchecked")
    @Test
    public void processCaseFileAndVerifyCcd() throws FileNotFoundException {

        log.info("Getting oAuth2 token...");
        String oauth2Token = idamService.getIdamOauth2Token();

        log.info("Building IDAM tokens...");
        idamTokens = IdamTokens.builder()
            .idamOauth2Token(oauth2Token)
            .serviceAuthorization(idamService.generateServiceAuthorization())
            .userId(idamService.getUserId(oauth2Token))
            .build();

        String tmpFileName = System.getProperty("java.io.tmpdir") + "/ccdCaseId.tmp";
        String s = new Scanner(new File(tmpFileName)).useDelimiter("\\Z").next();

        ccdCaseId = s.trim();

        SscsCaseDetails updatedCcdCase = ccdService.getByCaseId(Long.parseLong(ccdCaseId), idamTokens);
        assertNotNull(updatedCcdCase);

        SscsCaseData updatedCcdCaseData = updatedCcdCase.getData();

        assertEquals("XYZ", updatedCcdCaseData.getAppeal().getAppellant().getName().getFirstName());
        assertEquals(3, updatedCcdCaseData.getEvents().size());
        assertEquals("appealCreated", updatedCcdCase.getState());
    }
}
