package uk.gov.hmcts.reform.sscs.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.sscs.CaseDataUtils;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

public class CaseDataContentToJsonTest {

    @Test
    public void givenACaseDataContent_ShouldBeTransformedToJson() throws Exception {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .indentOutput(true)
            .build();

        // given
        CaseDataContent caseDataContent = getCaseDataContent();

        // when
        String actualCaseDataContentJson = mapper.writeValueAsString(caseDataContent);

        // should
        String expectedCaseDataContentJson = FileUtils.getFileContentGivenFilePath(
            "src/test/resources//CaseDataContent.json");
        assertJsonEquals(expectedCaseDataContentJson, actualCaseDataContentJson);
    }

    private CaseDataContent getCaseDataContent() {
        return CaseDataContent.builder()
            .eventToken("user token")
            .event(Event.builder()
                .id("appealCreated")
                .summary("SSCS - appeal created event")
                .description("Created SSCS case with token")
                .build())
            .data(CaseDataUtils.buildCaseData())
            .build();
    }
}
