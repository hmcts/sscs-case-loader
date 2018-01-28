package uk.gov.hmcts.reform.sscs.services;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.CaseDataUtils;

import static org.junit.Assert.assertNotNull;

/**
 * class for testing purpose.
 * When running the CDD dockerized env we can use the method here to confirm
 * that we can save a Case into the CDD from the CaseLoader successfully.
 */
//TODO: Move this class to our End to End tests project
@RunWith(SpringRunner.class)
@SpringBootTest
public class SaveSimpleCaseIntoCcd {

    @Autowired
    private CoreCaseDataService coreCaseDataService;

    @Test
    @Ignore
    public void givenACase_shouldBeSaveIntoCcd() {
        assertNotNull(coreCaseDataService);
        coreCaseDataService.startEventAndSaveGivenCase(CaseDataUtils.buildCaseData());
    }

}
