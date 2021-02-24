package uk.gov.hmcts.reform.sscs.functional.postdeploy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.functional.postdeploy.data.ProcessCaseFileTestData;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:config/application_e2e.yaml")
@SpringBootTest
@Slf4j
public class ProcessCaseFileTest {

    @Autowired
    private ProcessCaseFileTestData processCaseFileTestData;

    @SuppressWarnings("unchecked")
    @Test
    public void processCaseFileAndVerifyCcd() throws IOException, ClassNotFoundException {
        SscsCaseDetails updatedCcdCase = processCaseFileTestData.getTestCase();
        assertNotNull(updatedCcdCase);

        SscsCaseData updatedCcdCaseData = updatedCcdCase.getData();

        log.info("CCD CASE DATA is {}", updatedCcdCaseData.toString());

        assertEquals("User", updatedCcdCaseData.getAppeal().getAppellant().getName().getFirstName());
        assertEquals(1, updatedCcdCaseData.getEvents().size());
        assertEquals("hearing", updatedCcdCase.getState());
    }
}
