package uk.gov.hmcts.reform.sscs.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.sscs.AppealUtils;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

public class AppealToJsonTest {
    @Test
    public void givenAnAppealObject_shouldBeTransformedToJsonString() throws Exception {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .indentOutput(true)
            .build();

        // given
        Appeal appeal = AppealUtils.buildAppeal();

        // when
        String actualAppealJson = mapper.writeValueAsString(appeal);

        // should
        String expectedAppealJson = new FileUtils().getResourceContentGivenResourceName("/Appeal.json");
        assertJsonEquals(expectedAppealJson, actualAppealJson);
    }
}
