package uk.gov.hmcts.reform.sscs.models;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.sscs.CaseDataUtils;

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
        File caseDataContentFile = new File("src/test/resources/CaseDataContent.json");
        String expectedCaseDataContentJson = FileUtils.readFileToString(caseDataContentFile,
            StandardCharsets.UTF_8.name());
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
