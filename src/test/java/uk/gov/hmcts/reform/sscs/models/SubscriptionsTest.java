package uk.gov.hmcts.reform.sscs.models;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.AppellantSubscription;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;

public class SubscriptionsTest {
    @Test
    public void givenJsonSubscriptions_shouldDeserialize() {
        String jsonSubscription = "\"subscriptions\": {\n" +
            "      \"appellantSubscription\": {\n" +
            "        \"tya\": \"\",\n" +
            "        \"email\": \"\",\n" +
            "        \"mobile\": \"\",\n" +
            "        \"subscribeEmail\": \"yes/no\",\n" +
            "        \"subscribeSms\": \"yes/no\",\n" +
            "        \"reason\": \"\"\n" +
            "      },\n" +
            "      \"supporterSubscription\": {\n" +
            "        \"tya\": \"\",\n" +
            "        \"email\": \"\",\n" +
            "        \"mobile\": \"\",\n" +
            "        \"subscribeEmail\": \"\",\n" +
            "        \"subscribeSms\": \"\",\n" +
            "        \"reason\": \"\"\n" +
            "      }\n" +
            "    }";


        AppellantSubscription appellantSubscription = AppellantSubscription.builder()
            .tya("")
            .email("")
            .mobile("")
            .subscribeEmail("yes/no")
            .subscribeSms("yes/no")
            .reason("")
            .build();
        Subscriptions subscriptions = Subscriptions.builder()
            .appellantSubscription(appellantSubscription)
            .build();


        assertJsonEquals(jsonSubscription, subscriptions);

    }
}
