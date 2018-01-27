package uk.gov.hmcts.reform.sscs.services;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.AppealUtils;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SaveSimpleCaseIntoCcd {

    @Autowired
    private CoreCaseDataService coreCaseDataService;

    @Test@Ignore
    public void givenACase_shouldBeSaveIntoCcd() {
        assertNotNull(coreCaseDataService);
        coreCaseDataService.startEventAndSaveGivenCase(AppealUtils.buildAppeal());
    }

}
