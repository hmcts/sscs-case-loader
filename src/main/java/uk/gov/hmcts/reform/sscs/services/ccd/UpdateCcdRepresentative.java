package uk.gov.hmcts.reform.sscs.services.ccd;

import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;

final class UpdateCcdRepresentative {
    private static final String YES = "Yes";
    private static final String NO = "No";

    private UpdateCcdRepresentative() {
        // Empty
    }

    static boolean updateCcdRepresentative(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {
        boolean repUpdated = false;
        if (gapsCaseData.getAppeal() != null && gapsCaseData.getAppeal().getRep() != null) {
            Representative rep = gapsCaseData.getAppeal().getRep();
            if (existingCcdCaseData.getAppeal().getRep() == null
                || !existingCcdCaseData.getAppeal().getRep().getName().equals(rep.getName())
                || !existingCcdCaseData.getAppeal().getRep().getContact().equals(rep.getContact())
            ) {
                existingCcdCaseData.getAppeal().setRep(rep);
                updateRepresentativeSubscription(gapsCaseData, existingCcdCaseData);
                repUpdated = true;
            }
        }
        return repUpdated;
    }

    private static void updateRepresentativeSubscription(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {

        Subscription newRepSubscription = gapsCaseData.getSubscriptions() != null
            ? gapsCaseData.getSubscriptions().getRepresentativeSubscription() : Subscription.builder().build();

        Subscriptions existingSubscriptions = existingCcdCaseData.getSubscriptions() != null
            ? existingCcdCaseData.getSubscriptions() : Subscriptions.builder().build();

        Subscription existingRepSubscription = existingSubscriptions.getRepresentativeSubscription();

        Subscription updatedSubscription =
            keepExistingSubscribedSubscriptions(newRepSubscription, existingRepSubscription);

        existingSubscriptions = existingSubscriptions.toBuilder()
            .representativeSubscription(updatedSubscription).build();

        existingCcdCaseData.setSubscriptions(existingSubscriptions);
    }

    private static Subscription keepExistingSubscribedSubscriptions(Subscription newRepSubscription,
                                                     Subscription existingRepSubscription) {
        return newRepSubscription.toBuilder()
            .subscribeSms(existingRepSubscription != null && existingRepSubscription.isSmsSubscribed() ? YES : NO)
            .subscribeEmail(existingRepSubscription != null && existingRepSubscription.isEmailSubscribed() ? YES : NO)
            .build();
    }


}
