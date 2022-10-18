package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

public class UpdateSubscriptionTest {

    private final Name name = Name.builder().lastName("Potter").build();

    private SscsCaseData gapsCaseData = SscsCaseData.builder()
        .appeal(Appeal.builder()
            .rep(Representative.builder().name(name).build())
            .appellant(Appellant.builder().name(name)
                .appointee(Appointee.builder().name(name).build())
                .build())
            .build())
        .subscriptions(Subscriptions.builder()
            .representativeSubscription(Subscription.builder()
                .subscribeSms("No").subscribeEmail("No").email("harry.potter@mail.com").build())
            .appointeeSubscription(Subscription.builder()
                .email("appointee.new@email.com").subscribeEmail("No").build())
            .build())
        .build();

    private final SscsCaseData existingAppointeeCase = gapsCaseData.toBuilder()
        .appeal(gapsCaseData.getAppeal().toBuilder()
            .appellant(Appellant.builder().name(name)
                .appointee(Appointee.builder().name(name).build()).build())
            .build())
        .subscriptions(Subscriptions.builder()
            .representativeSubscription(Subscription.builder()
                .subscribeSms("Yes").mobile("0777").subscribeEmail("Yes").email("harry.potter@mail.com")
                    .lastLoggedIntoMya("2020-01-24T00:00:00+01:00").build())
            .appointeeSubscription(Subscription.builder()
                .wantSmsNotifications("Yes")
                .subscribeSms("Yes").mobile("0777").email("appointee.old@email.com").subscribeEmail("Yes").build())
            .build()
        ).build();

    private final SscsCaseData existingAppellantCase = gapsCaseData.toBuilder()
        .appeal(gapsCaseData.getAppeal().toBuilder()
            .appellant(Appellant.builder().name(name).build())
            .build())
        .subscriptions(Subscriptions.builder()
            .appellantSubscription(Subscription.builder()
                .wantSmsNotifications("Yes")
                .subscribeSms("Yes").mobile("0777").subscribeEmail("Yes").email("appellant.old@email.com")
                .build())
            .representativeSubscription(Subscription.builder()
                .subscribeSms("Yes").mobile("0777").subscribeEmail("Yes").email("harry.potter@mail.com")
                .lastLoggedIntoMya("2020-01-24T00:00:00+01:00").build())
            .build()
        ).build();

    @Test
    public void givenAChangeInAppointee_shouldNotUnsubscribeAppointeeOrRep() {
        final Subscription expectedRepSubscription = Subscription.builder().email("harry.potter@mail.com")
                .lastLoggedIntoMya("2020-01-24T00:00:00+01:00").subscribeSms("Yes").mobile("0777")
                .subscribeEmail("Yes").build();

        final Subscription expectedAppointeeSubscription = Subscription.builder().email("appointee.old@email.com")
            .wantSmsNotifications("Yes").subscribeSms("Yes").mobile("0777").subscribeEmail("Yes").build();

        UpdateSubscription.SubscriptionUpdate appointeeSubscriptionUpdate =
            new UpdateSubscription.SubscriptionUpdate() {
                @Override
                public Subscription getSubscription(Subscriptions subscriptions) {
                    return subscriptions.getAppointeeSubscription();
                }

                @Override
                public Subscriptions updateExistingSubscriptions(Subscription subscription) {
                    return existingAppointeeCase.getSubscriptions().toBuilder()
                        .appointeeSubscription(subscription).build();
                }
            };
        UpdateSubscription.updateSubscription(gapsCaseData, existingAppointeeCase, appointeeSubscriptionUpdate);

        assertEquals(expectedRepSubscription, existingAppointeeCase.getSubscriptions().getRepresentativeSubscription());
        assertEquals(expectedAppointeeSubscription,
            existingAppointeeCase.getSubscriptions().getAppointeeSubscription());
        assertNull(existingAppointeeCase.getSubscriptions().getAppellantSubscription());
    }

    @Test
    public void givenAChangeInAppellantEmailIsNull_shouldNotUnsubscribeAppellantOrRep() {
        gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .rep(Representative.builder().name(name).build())
                .appellant(Appellant.builder().name(name).build())
                .build())
            .subscriptions(Subscriptions.builder()
                .appellantSubscription(Subscription.builder()
                    .subscribeSms("Yes").mobile("0777").subscribeEmail("Yes").email(null)
                    .lastLoggedIntoMya("2020-01-24T00:00:00+01:00").build())
                .representativeSubscription(Subscription.builder()
                    .subscribeSms("No").subscribeEmail("No").email("harry.potter@mail.com").build())
                .build()
            ).build();

        final Subscription expectedRepSubscription = Subscription.builder().email("harry.potter@mail.com")
            .lastLoggedIntoMya("2020-01-24T00:00:00+01:00").subscribeSms("Yes").mobile("0777")
            .subscribeEmail("Yes").build();

        final Subscription expectedAppellantSubscription = Subscription.builder().email("appellant.old@email.com")
            .wantSmsNotifications("Yes").subscribeSms("Yes").mobile("0777").subscribeEmail("Yes").build();

        UpdateSubscription.SubscriptionUpdate appellantSubscriptionUpdate =
            new UpdateSubscription.SubscriptionUpdate() {
                @Override
                public Subscription getSubscription(Subscriptions subscriptions) {
                    return subscriptions.getAppellantSubscription();
                }

                @Override
                public Subscriptions updateExistingSubscriptions(Subscription subscription) {
                    return existingAppellantCase.getSubscriptions().toBuilder()
                        .appellantSubscription(subscription).build();
                }
            };
        UpdateSubscription.updateSubscription(gapsCaseData, existingAppellantCase, appellantSubscriptionUpdate);

        assertEquals(expectedRepSubscription, existingAppellantCase.getSubscriptions().getRepresentativeSubscription());
        assertEquals(expectedAppellantSubscription,
            existingAppellantCase.getSubscriptions().getAppellantSubscription());
        assertNull(existingAppellantCase.getSubscriptions().getAppointeeSubscription());
    }
}
