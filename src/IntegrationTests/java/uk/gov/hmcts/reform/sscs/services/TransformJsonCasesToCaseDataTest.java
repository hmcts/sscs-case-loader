package uk.gov.hmcts.reform.sscs.services;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.hmcts.reform.sscs.TestCaseLoaderApp;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
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

    private static final String JSON_CASES_PATH = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json";

    @Test
    @Parameters({
        "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json, "
            + "src/test/resources/CaseDataArray.json",
        "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19_With_Optional_Fields.json, "
            + "src/test/resources/CaseDataArrayWithOptionalFields.json"
    })
    public void givenJsonCases_shouldBeMappedIntoCaseData(String jsonCasesPath, String expectedCaseDataPath)
        throws IOException {
        // Given
        String jsonCases = FileUtils.readFileToString(new File(jsonCasesPath), StandardCharsets.UTF_8.name());

        // When
        List<CaseData> caseDataList = transformJsonCasesToCaseData.transform(jsonCases);

        // Should
        String expectedCaseDataString = FileUtils.readFileToString(new File(expectedCaseDataPath),
            StandardCharsets.UTF_8.name());

        assertJsonEquals(expectedCaseDataString, caseDataList);
    }

    @Test(expected = TransformException.class)
    public void givenTheMapperReaderFails_shouldThrowAnException() throws Exception {
        String invalidFileName = "src/test/resources/SSCS_ExtractInvalid_Delta_2017-06-30-09-25-56.xml";
        String jsonCases = FileUtils.readFileToString(new File(invalidFileName), StandardCharsets.UTF_8.name());
        transformJsonCasesToCaseData.transform(jsonCases);
    }

    @Test
    public void givenJsonCases_shouldBeTransformedOnlyCasesWithStatusEqual3() throws Exception {
        //Given
        String jsonCases = FileUtils.readFileToString(new File(JSON_CASES_PATH), StandardCharsets.UTF_8.name());
        List<CaseData> caseDataList = transformJsonCasesToCaseData.transform(jsonCases);
        //Should
        int expectedNumberOfCasesWithStatusEqual3 = 2;
        assertTrue(caseDataList.size() == expectedNumberOfCasesWithStatusEqual3);
    }

    @Test
    public void givenJsonCasesAreTransformedToCaseData_shouldIncludeTheEventsJsonField() throws Exception {
        // Given
        String jsonCases = FileUtils.readFileToString(new File(JSON_CASES_PATH), StandardCharsets.UTF_8.name());
        List<CaseData> caseDataList = transformJsonCasesToCaseData.transform(jsonCases);
        //Should
        Event event = caseDataList.get(0).getEvents().get(0);
        assertEquals(event.getValue().getType(), "appealCreated");
    }
}
