package uk.gov.hmcts.reform.sscs.services;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
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
            + "src/test/resources/CaseDataArray.json",
        "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19_With_Optionals.json, "
            + "src/test/resources/CaseDataArrayWithOptionals.json"
    })
    public void givenJsonCases_shouldBeMappedIntoCaseData(String jsonCasesPath, String expectedCaseDataPath)
        throws IOException {
        // Given
        String jsonCases = FileUtils.readFileToString(new File(jsonCasesPath), StandardCharsets.UTF_8.name());

        // When
        List<CaseData> caseDataList = transformJsonCasesToCaseData.transform(jsonCases);

        // Should
        String actualCaseDataString = transformCasesToString(caseDataList);
        String expectedCaseDataString = FileUtils.readFileToString(new File(expectedCaseDataPath),
            StandardCharsets.UTF_8.name());

        assertJsonEquals(expectedCaseDataString, actualCaseDataString);
    }

    private String transformCasesToString(List<CaseData> caseDataList) throws JsonProcessingException {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .indentOutput(true)
            .build();
        return mapper.writeValueAsString(caseDataList);
    }

    @Test(expected = TransformException.class)
    public void givenTheMapperReaderFails_shouldThrowAnException() throws Exception {
        String invalidFileName = "src/test/resources/SSCS_ExtractInvalid_Delta_2017-06-30-09-25-56.xml";
        String jsonCases = FileUtils.readFileToString(new File(invalidFileName), StandardCharsets.UTF_8.name());
        transformJsonCasesToCaseData.transform(jsonCases);

    }
}
