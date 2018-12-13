package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.*;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

public class UpdateCcdRepresentativeTest {

    @Test
    public void givenARepChangeFromNullRep_willChangeDataAndReturnTrue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build()).build()
                ).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder().build()).build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);
        assertTrue("data has changed from a null rep", hasDataChanged);
        assertEquals(gapsCaseData.getAppeal().getRep(), existingCaseData.getAppeal().getRep());
        assertNotNull(existingCaseData.getSubscriptions().getRepresentativeSubscription());
    }

    @Test
    public void givenARepNameChange_willChangeDataAndReturnTrue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build()).build()
                ).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName("Superman").build()).build()
            ).build())
            .build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertTrue("rep name has changed", hasDataChanged);
        assertEquals(gapsCaseData.getAppeal().getRep(), existingCaseData.getAppeal().getRep());
        assertNotNull(existingCaseData.getSubscriptions().getRepresentativeSubscription());
    }

    @Test
    public void givenARepContactChange_willChangeDataAndReturnTrue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build())
                        .contact(Contact.builder().email("harry@potter.com").build()).build()
                ).build())
            .subscriptions(Subscriptions.builder().representativeSubscription(
                Subscription.builder().email("harry@potter.com").build()).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName("Potter").build())
                    .contact(Contact.builder().email("harry.potter@wizards.com").build()).build()
            ).build())
            .build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);
        assertTrue("rep contact has changed", hasDataChanged);
        assertEquals(gapsCaseData.getAppeal().getRep(), existingCaseData.getAppeal().getRep());
        assertEquals(gapsCaseData.getSubscriptions().getRepresentativeSubscription(),
            existingCaseData.getSubscriptions().getRepresentativeSubscription());
    }

    @Test
    public void givenNoRepChange_willReturnFalseAndNotModifyData() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build())
                        .contact(Contact.builder().email("harry@potter.com").build()).build()
                ).build())
            .build();

        SscsCaseData existingCaseData = gapsCaseData.toBuilder().build();
        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertFalse(hasDataChanged);
        assertEquals(existingCaseData, gapsCaseData);
    }

    @Test
    public void givenNullRepChange_willReturnFalseAndNotModifyData() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder().build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName("Potter").build()).build()
            ).build())
            .build();
        SscsCaseData originalExistingCaseData = existingCaseData.toBuilder().build();
        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertFalse(hasDataChanged);
        assertEquals(existingCaseData, originalExistingCaseData);
    }

    @Test
    public void givenNullAppeal_willReturnFalseAndNotModifyData() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName("Potter").build()).build()
            ).build())
            .build();
        SscsCaseData originalExistingCaseData = existingCaseData.toBuilder().build();
        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertFalse(hasDataChanged);
        assertEquals(existingCaseData, originalExistingCaseData);
    }

}
