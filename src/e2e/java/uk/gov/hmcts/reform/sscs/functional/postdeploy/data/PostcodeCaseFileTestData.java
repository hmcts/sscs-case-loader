package uk.gov.hmcts.reform.sscs.functional.postdeploy.data;

import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

import java.io.IOException;
@Component
@Slf4j
public class PostcodeCaseFileTestData extends AbstractCaseFileTestData {
    private static final String TEST_CASE_KEY = "PostCodeCheckCaseFileTest";

    @Override
    public String createTestData() throws IOException, SftpException {
        String ccdCaseId = createCcdCase();
        writeXmlToSftp(ccdCaseId, TEST_DATA_XML_PREFIX + "2021-02-23-12-00-00.xml");
        return ccdCaseId;
    }

    @Override
    public String getTestCaseKey() {
        return TEST_CASE_KEY;
    }

    @Override
    public SscsCaseDetails getTestCase() throws IOException, ClassNotFoundException {
        SscsCaseDetails updatedCcdCase = getCaseByKey(TEST_CASE_KEY);
        return updatedCcdCase;
    }
}
