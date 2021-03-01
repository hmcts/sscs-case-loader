package uk.gov.hmcts.reform.sscs.functional.postdeploy;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.functional.postdeploy.data.PostcodeCaseFileTestData;


@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:config/application_e2e.yaml")
@SpringBootTest
@Slf4j
public class PostcodeCaseFileTest {
    /**
     * This is an example test case. Please follow the steps to add any new test scenario
     */

    //Add this bean to provide data for the test scenario. See more details in PostcodeCaseFileTestData class
    @Autowired
    private PostcodeCaseFileTestData postcodeCaseFileTestData;

    //Get the SscsCaseDetails from the data provider and verify expected results
    @Test
    public void processCaseFileAndVerifyCcd() throws IOException, ClassNotFoundException {
        SscsCaseDetails updatedCcdCase = postcodeCaseFileTestData.getTestCase();
        assertNotNull(updatedCcdCase);
    }
}
