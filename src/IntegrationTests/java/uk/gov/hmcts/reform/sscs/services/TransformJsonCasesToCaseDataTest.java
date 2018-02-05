package uk.gov.hmcts.reform.sscs.services;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.hmcts.reform.sscs.TestCaseLoaderApp;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformJsonCasesToCaseData;


@RunWith(JUnitParamsRunner.class)
@SpringBootTest(classes = TestCaseLoaderApp.class)
public class TransformJsonCasesToCaseDataTest {

    @Autowired
    private TransformJsonCasesToCaseData transformJsonCasesToCaseData;

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Test
    @Parameters({
        "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json, "
            + "src/test/resources/CaseData.json",
        "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19_With_Dob.json, "
            + "src/test/resources/CaseDataWithDob.json"
    })
    public void givenJsonCases_shouldBeMappedIntoCaseData(String jsonCasesPath, String expectedCaseDataPath)
        throws IOException {
        // Given
        String jsonCases = FileUtils.readFileToString(new File(jsonCasesPath), StandardCharsets.UTF_8.name());

        // When
        CaseData caseData = transformJsonCasesToCaseData.transform(jsonCases);

        // Should
        String actualCaseDataString = transformCaseDataToCaseDataString(caseData);
        String expectedCaseDataString = FileUtils.readFileToString(new File(expectedCaseDataPath),
            StandardCharsets.UTF_8.name());
        assertJsonEquals(expectedCaseDataString, actualCaseDataString);
    }

    private String transformCaseDataToCaseDataString(CaseData caseData) throws JsonProcessingException {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .indentOutput(true)
            .build();
        return mapper.writeValueAsString(caseData);
    }
}
