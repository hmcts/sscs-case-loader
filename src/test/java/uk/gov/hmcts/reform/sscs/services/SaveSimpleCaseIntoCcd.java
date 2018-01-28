package uk.gov.hmcts.reform.sscs.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.AppealUtils;
import uk.gov.hmcts.reform.sscs.models.CaseData;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SaveSimpleCaseIntoCcd {

    @Autowired
    private CoreCaseDataService coreCaseDataService;

    @Test
    @Ignore
    public void givenACase_shouldBeSaveIntoCcd() {
        assertNotNull(coreCaseDataService);
        coreCaseDataService.startEventAndSaveGivenCase(AppealUtils.buildAppeal());
    }

    @Test
    @Ignore
    public void givenCaseData_shouldBeTransformed() throws Exception {
        CaseData caseData = CaseData.builder()
            .appeal(AppealUtils.buildAppeal())
            .build();

        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .indentOutput(true)
            .build();

        System.out.println(mapper.writeValueAsString(caseData));
    }
}
