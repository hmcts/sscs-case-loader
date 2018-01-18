package uk.gov.hmcts.reform.sscs.models;

import org.json.JSONObject;
import org.junit.Test;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

public class JsonFilesTest {
    @Test
    public void shouldCreateJsonFilesInstance() {
        String jsonStringTest = "{\n"
            + "   \"name\":\"John\",\n"
            + "   \"age\":30,\n"
            + "   \"car\":1\n"
            + "}";
        JSONObject jsonObjTest = new JSONObject(jsonStringTest);

        JsonFiles actualJsonFiles = JsonFiles.builder().delta(jsonObjTest).ref(jsonObjTest).build();

        assertJsonEquals(jsonStringTest, actualJsonFiles.getDelta());
        assertJsonEquals(jsonStringTest, actualJsonFiles.getRef());
    }
}
