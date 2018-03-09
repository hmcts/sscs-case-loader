package uk.gov.hmcts.reform.sscs.models;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.sscs.CaseDataUtils;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.AppellantSubscription;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.SupporterSubscription;

public class CaseDataContentToJsonTest {

    @Test
    public void givenACaseDataContent_ShouldBeTransformedToJson() throws Exception {
        // given
        CaseDataContent caseDataContent = getCaseDataContent();

        // should
        File caseDataContentFile = new File("src/test/resources/CaseDataContent.json");
        String expectedCaseDataContentJson = FileUtils.readFileToString(caseDataContentFile,
            StandardCharsets.UTF_8.name());
        assertJsonEquals(expectedCaseDataContentJson, caseDataContent);
    }

    private CaseDataContent getCaseDataContent() {
        return CaseDataContent.builder()
            .eventToken("user token")
            .event(Event.builder()
                .id("appealCreated")
                .summary("SSCS - appeal created event")
                .description("Created SSCS case with token")
                .build())
            .data(CaseDataUtils.buildCaseData("SC068/17/00013"))
            .build();
    }

    @Test
    public void givenJsonSubscriptions_shouldDeserialize() {
        String jsonSubscription = "{\n" +
            "    \"appellantSubscription\" : {\n" +
            "      \"tya\" : \"\",\n" +
            "      \"email\" : \"\",\n" +
            "      \"mobile\" : \"\",\n" +
            "      \"subscribeEmail\" : \"yes/no\",\n" +
            "      \"subscribeSms\" : \"yes/no\",\n" +
            "      \"reason\" : \"\"\n" +
            "    },\n" +
            "    \"supporterSubscription\" : {\n" +
            "      \"tya\" : \"\",\n" +
            "      \"email\" : \"\",\n" +
            "      \"mobile\" : \"\",\n" +
            "      \"subscribeEmail\" : \"\",\n" +
            "      \"subscribeSms\" : \"\",\n" +
            "      \"reason\" : \"\"\n" +
            "    }\n" +
            "  }";


        AppellantSubscription appellantSubscription = AppellantSubscription.builder()
            .tya("")
            .email("")
            .mobile("")
            .subscribeEmail("yes/no")
            .subscribeSms("yes/no")
            .reason("")
            .build();
        SupporterSubscription supporterSubscription = SupporterSubscription.builder()
            .tya("")
            .email("")
            .mobile("")
            .subscribeEmail("")
            .subscribeSms("")
            .reason("")
            .build();
        Subscriptions subscriptions = Subscriptions.builder()
            .appellantSubscription(appellantSubscription)
            .supporterSubscription(supporterSubscription)
            .build();

        CaseData caseData = CaseData.builder()
            .subscriptions(subscriptions)
            .build();

        assertThatJson(caseData).node("subscriptions").isEqualTo(jsonSubscription);
    }

}
