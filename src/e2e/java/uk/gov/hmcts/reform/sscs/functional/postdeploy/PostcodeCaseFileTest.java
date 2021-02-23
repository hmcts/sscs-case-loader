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

    @Autowired
    private PostcodeCaseFileTestData postcodeCaseFileTestData;

    @Test
    public void processCaseFileAndVerifyCcd() throws IOException, ClassNotFoundException {
        SscsCaseDetails updatedCcdCase = postcodeCaseFileTestData.getTestCase();
        assertNotNull(updatedCcdCase);
    }
}
