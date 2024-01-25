package uk.gov.hmcts.reform.sscs.services.ccd;

import static uk.gov.hmcts.reform.sscs.exceptions.FeignExceptionLogger.debugCaseLoaderException;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;
import uk.gov.hmcts.reform.sscs.util.UkMobile;

@Slf4j
class UpdateSubscription {

    private static final String YES = "Yes";
    private static final String NO = "No";

    private UpdateSubscription() {
        // Empty
    }

    static void updateSubscription(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData,
                                   SubscriptionUpdate subscriptionUpdate) {
        Subscription newSubscription = gapsCaseData.getSubscriptions() != null
            && subscriptionUpdate.getSubscription(gapsCaseData.getSubscriptions()) != null
            ? subscriptionUpdate.getSubscription(gapsCaseData.getSubscriptions()) : Subscription.builder().build();
        assert newSubscription != null;

        Subscriptions existingSubscriptions = existingCcdCaseData.getSubscriptions() != null
            ? existingCcdCaseData.getSubscriptions() : Subscriptions.builder().build();

        Subscription existingSubscription = subscriptionUpdate.getSubscription(existingSubscriptions);

        Subscription updatedSubscription =
            keepExistingSubscribedSubscriptions(newSubscription, existingSubscription,
                gapsCaseData.getCaseReference());

        existingSubscriptions = subscriptionUpdate.updateExistingSubscriptions(updatedSubscription);

        try {
            existingCcdCaseData.setSubscriptions(existingSubscriptions);
        } catch (FeignException e) {
            debugCaseLoaderException(log, e, "Could not update Subscription");
        }
    }

    private static Subscription keepExistingSubscribedSubscriptions(Subscription newSubscription,
                                                                    Subscription exisitingSubscription,
                                                                    String caseReference) {
        return newSubscription.toBuilder()
            .wantSmsNotifications(exisitingSubscription != null ? exisitingSubscription.getWantSmsNotifications() : NO)
            .subscribeSms(exisitingSubscription != null && exisitingSubscription.isSmsSubscribed() ? YES : NO)
            .subscribeEmail(exisitingSubscription != null && exisitingSubscription.isEmailSubscribed() ? YES : NO)
            .mobile(getValidMobileNumber(newSubscription, exisitingSubscription, caseReference))
            .email(exisitingSubscription != null ? exisitingSubscription.getEmail() : newSubscription.getEmail())
            .tya(exisitingSubscription != null ? exisitingSubscription.getTya() : newSubscription.getTya())
            .lastLoggedIntoMya(exisitingSubscription != null ? exisitingSubscription.getLastLoggedIntoMya() : null)
            .build();
    }

    private static String getValidMobileNumber(Subscription newSubscription, Subscription existingSubscription,
                                               String caseReference) {
        if (UkMobile.validate(newSubscription.getMobile())) {
            return newSubscription.getMobile();
        } else {
            log.info("Invalid Uk mobile no: {} in Delta Reps Subscription for the case reference: {}",
                newSubscription.getMobile(), caseReference);
            return (existingSubscription != null) ? existingSubscription.getMobile() : null;
        }
    }

    public interface SubscriptionUpdate {

        Subscription getSubscription(Subscriptions subscriptions);

        Subscriptions updateExistingSubscriptions(Subscription subscription);
    }
}
