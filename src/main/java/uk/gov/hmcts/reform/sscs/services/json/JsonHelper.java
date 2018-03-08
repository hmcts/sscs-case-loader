package uk.gov.hmcts.reform.sscs.services.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.sscs.exceptions.ApplicationErrorException;

public final class JsonHelper {

    private JsonHelper() {
    }

    public static String printCaseDetailsInJson(Object object) {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .indentOutput(true)
            .build();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ApplicationErrorException("Oops...something went wrong...", e);
        }
    }
}
