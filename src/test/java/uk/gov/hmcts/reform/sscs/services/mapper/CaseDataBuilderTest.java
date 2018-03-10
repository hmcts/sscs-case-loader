package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;

public class CaseDataBuilderTest {
    @Test
    public void shouldBuildSubscriptionsWithAppealCaseNumber() {
        CaseDataBuilder caseDataBuilder = new CaseDataBuilder(null);
        Subscriptions subscriptions = caseDataBuilder.buildSubscriptions();
        assertNotNull("AppellantSubscription is null", subscriptions.getAppellantSubscription());
        assertNotNull("SupporterSubscription is null", subscriptions.getSupporterSubscription());
        String appealNumber = subscriptions.getAppellantSubscription().getTya();
        assertTrue("appealNumber is empty", !"".equals(appealNumber));
        assertTrue("appealNumber length is not 10 digits", appealNumber.length() == 10);
    }
}
