package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.*;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

@RunWith(JUnitParamsRunner.class)
public class UpdateCcdRepresentativeTest {
    private static final String YES = "Yes";
    private static final String NO = "No";

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
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isEmailSubscribed());
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isSmsSubscribed());
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
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isEmailSubscribed());
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isSmsSubscribed());
    }

    @Test
    public void givenARepNameChangeWhenExistingNameIsNull_willChangeDataAndReturnTrue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .rep(
                                Representative.builder().name(Name.builder().lastName("Potter").build()).build()
                        ).build())
                .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
                .rep(
                        Representative.builder().name(null).build()
                ).build())
                .build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertTrue("rep name has changed", hasDataChanged);
        assertEquals(gapsCaseData.getAppeal().getRep(), existingCaseData.getAppeal().getRep());
        assertNotNull(existingCaseData.getSubscriptions().getRepresentativeSubscription());
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isEmailSubscribed());
        assertFalse(existingCaseData.getSubscriptions().getRepresentativeSubscription().isSmsSubscribed());
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
                Subscription.builder().email("harry@potter.com").subscribeEmail(NO).subscribeSms(NO).build()).build())
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
    public void givenARepContactChangeWhenExistingContactIsNull_willChangeDataAndReturnTrue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .rep(
                                Representative.builder().name(Name.builder().lastName("Potter").build())
                                        .contact(Contact.builder().email("harry@potter.com").build()).build()
                        ).build())
                .subscriptions(Subscriptions.builder().representativeSubscription(
                        Subscription.builder().email("harry@potter.com").subscribeEmail(NO).subscribeSms(NO).build()).build())
                .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
                .rep(
                        Representative.builder().name(Name.builder().lastName("Potter").build())
                                .contact(null).build()
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

    @Test
    @Parameters({"Yes", "No"})
    public void givenARepChange_willKeepExistingEmailAndSmsSubscriptions(String subscribed) {
        String oppositeOfSubscribed = YES.equals(subscribed) ? NO : YES;
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(
                    Representative.builder().name(Name.builder().lastName("Potter").build()).build()
                ).build())
            .subscriptions(Subscriptions.builder().representativeSubscription(Subscription.builder()
                .subscribeSms(oppositeOfSubscribed)
                .subscribeEmail(oppositeOfSubscribed)
                .mobile("0770")
                .email("update@email.com")
                .build()).build())
            .build();

        SscsCaseData existingCaseData = SscsCaseData.builder().appeal(Appeal.builder()
            .rep(
                Representative.builder().name(Name.builder().lastName("Superman").build()).build()
            ).build())
            .subscriptions(Subscriptions.builder().representativeSubscription(Subscription.builder()
                .subscribeSms(subscribed)
                .subscribeEmail(subscribed)
                .mobile("0999")
                .email("rep@mail.com")
                .build()).build())
            .build();

        boolean hasDataChanged = UpdateCcdRepresentative.updateCcdRepresentative(gapsCaseData, existingCaseData);

        assertTrue("representative has changed", hasDataChanged);
        Subscription updatedRepSubscription = existingCaseData.getSubscriptions().getRepresentativeSubscription();
        assertEquals(subscribed, updatedRepSubscription.getSubscribeEmail());
        assertEquals(subscribed, updatedRepSubscription.getSubscribeSms());
        assertEquals("update@email.com", updatedRepSubscription.getEmail());
        assertEquals("0770", updatedRepSubscription.getMobile());
    }
}
