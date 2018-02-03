package uk.gov.hmcts.reform.sscs;

import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;

/**
 * Class for testing purpose.
 * When running the CDD dockerized env we can use the method here to confirm
 * that we can save a Case into the CDD from the CaseLoader successfully.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessFileAndSaveIntoCcd {

    @Autowired
    private CaseLoaderService caseLoaderService;

    @Test
    @Ignore
    public void givenACase_shouldBeSaveIntoCcd() throws Exception {
        assertNotNull(caseLoaderService);
        caseLoaderService.process();
    }

}
