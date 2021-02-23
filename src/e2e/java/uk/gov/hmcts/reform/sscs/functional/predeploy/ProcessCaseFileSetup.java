package uk.gov.hmcts.reform.sscs.functional.predeploy;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import java.io.*;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;
import uk.gov.hmcts.reform.tools.GenerateXml;
import uk.gov.hmcts.reform.tools.utils.CaseIdMapUtils;

import static uk.gov.hmcts.reform.sscs.functional.predeploy.PreDeployableTestData.TEST_DATA_XML_PREFIX;

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
    private List<PreDeployableTestData> preDeployable;

    @Test
    public void setup() throws ParserConfigurationException, TransformerException, IOException, SftpException {
        cleanSftpFiles();
        writeXmlToSftp();
        GenerateXml.generateXmlForAppeals();
        copy(outputdir);
    }

    private void writeXmlToSftp() throws IOException, SftpException {
        HashMap<String, String> dataKeys = new HashMap<>();
        for (PreDeployableTestData test: preDeployable) {
            String caseId = test.createTestData();
            dataKeys.put(test.getTestCaseKey(), caseId);
        }

        CaseIdMapUtils.write(dataKeys);
    }

    private void cleanSftpFiles() throws SftpException {

        ChannelSftp sftpChannel = sftpChannelAdapter.openConnectedChannel();
        try {
            sftpChannel.rm("/incoming/"+TEST_DATA_XML_PREFIX+"*.xml");
            sftpChannel.rm("/incoming/failed/"+TEST_DATA_XML_PREFIX+"*.xml");
            sftpChannel.rm("/incoming/processed/"+TEST_DATA_XML_PREFIX+"*.xml");

            sftpChannel.rm("/incoming/SSCS_CreateAppeals_Delta_*.xml");
            sftpChannel.rm("/incoming/failed/SSCS_CreateAppeals_Delta_*.xml");
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
