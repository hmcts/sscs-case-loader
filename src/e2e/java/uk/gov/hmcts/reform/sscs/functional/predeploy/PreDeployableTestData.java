package uk.gov.hmcts.reform.sscs.functional.predeploy;

import com.jcraft.jsch.SftpException;

import java.io.IOException;

public interface PreDeployableTestData {
    String TEST_DATA_XML_PREFIX = "SSCS_CcdCases_Delta_";

    String createTestData() throws IOException, SftpException;

    String getTestCaseKey();
}
