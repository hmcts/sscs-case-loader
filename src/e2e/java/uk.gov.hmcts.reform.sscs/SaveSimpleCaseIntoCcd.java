package uk.gov.hmcts.reform.sscs;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.services.ccd.CoreCaseDataService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestCaseLoaderApp.class)
public class SaveSimpleCaseIntoCcd {

    @Autowired
    private CoreCaseDataService coreCaseDataService;

    @Test
    public void givenACase_shouldBeSaveIntoCcd() {
        CaseDetails caseDetails = coreCaseDataService.startEventAndSaveGivenCase(CaseDataUtils.buildCaseData());
        assertNotNull(caseDetails);
    }

}
