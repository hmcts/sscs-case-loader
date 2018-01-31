package uk.gov.hmcts.reform.sscs.services.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.models.CaseData;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import java.io.IOException;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransformJsonCasesToCaseDataTest {

    private static final String DELTA_JSON = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json";

    @Autowired
    private TransformJsonCasesToCaseData transformJsonCasesToCaseData;

    @Test
    public void givenJsonCases_shouldBeMappedIntoCaseData() throws IOException {
        // Given
        String jsonCases = FileUtils.getFileContentGivenFilePath(DELTA_JSON);

        // When
        CaseData caseData = transformJsonCasesToCaseData.transform(jsonCases);

        // Should
        String actualCaseDataString = transformCaseDataToCaseDataString(caseData);
        String expectedCaseDataString = FileUtils.getFileContentGivenFilePath("src/test/resources/CaseData.json");
        assertJsonEquals(expectedCaseDataString, actualCaseDataString);
    }

    private String transformCaseDataToCaseDataString(CaseData caseData) throws JsonProcessingException {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .indentOutput(true)
            .build();
        return mapper.writeValueAsString(caseData);
    }
}
