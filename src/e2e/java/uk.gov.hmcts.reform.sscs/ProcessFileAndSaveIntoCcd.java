package uk.gov.hmcts.reform.sscs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessFileAndSaveIntoCcd {

    @Autowired
    private CaseLoaderService caseLoaderService;

    @Test
    public void givenACase_shouldBeSaveIntoCcd() {
        caseLoaderService.process();
    }

}
