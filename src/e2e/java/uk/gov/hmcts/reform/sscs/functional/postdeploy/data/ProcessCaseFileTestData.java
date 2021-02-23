package uk.gov.hmcts.reform.sscs.functional.postdeploy.data;

import com.jcraft.jsch.SftpException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

@Component
@Slf4j
public class ProcessCaseFileTestData extends AbstractCaseFileTestData {
    private static final String TEST_CASE_KEY = "ProcessCaseFileTest";

    @Override
    public String createTestData() throws IOException, SftpException {
        String ccdCaseId = createCcdCase();
        writeXmlToSftp(ccdCaseId, TEST_DATA_XML_PREFIX + "2018-07-09-12-34-56.xml");
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
