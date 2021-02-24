package uk.gov.hmcts.reform.sscs.functional.postdeploy.data;

import com.jcraft.jsch.SftpException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

@Component
@Slf4j
public class PostcodeCaseFileTestData extends AbstractCaseFileTestData {
    //Each test should have a unique key to store case id against it
    private static final String TEST_CASE_KEY = "PostCodeCheckCaseFileTest";



    /**
     * In this method, you can create any SscsCaseData you want and
     * put Gaps2File in ftp location for case loader service to process.
     * @return ccdCaseId ccd case id created for the test
     * @throws IOException error in storing case id
     * @throws SftpException error in creating Gaps2File
     */
    @Override
    public String createTestData() throws IOException, SftpException {
        String ccdCaseId = createCcdCase();
        writeXmlToSftp(ccdCaseId, TEST_DATA_XML_PREFIX + "2021-02-23-12-00-00.xml");
        return ccdCaseId;
    }

    /**
     * Returns unique key for the test case.
      */
    @Override
    public String getTestCaseKey() {
        return TEST_CASE_KEY;
    }

    /**
     * Find the SscsCaseDetails created in data set up step. Case id is stored against the TEST_CASE_KEY
     * @return SscsCaseDetails
     * @throws IOException error in reading case id store
     * @throws ClassNotFoundException error in deserializing id store
     */
    @Override
    public SscsCaseDetails getTestCase() throws IOException, ClassNotFoundException {
        SscsCaseDetails updatedCcdCase = getCaseByKey(TEST_CASE_KEY);
        return updatedCcdCase;
    }
}
