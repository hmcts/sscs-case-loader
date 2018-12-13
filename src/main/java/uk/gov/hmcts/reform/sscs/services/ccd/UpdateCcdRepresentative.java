package uk.gov.hmcts.reform.sscs.services.ccd;

import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;

final class UpdateCcdRepresentative {
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
                Subscriptions existingSubscriptions = existingCcdCaseData.getSubscriptions() != null
                    ? existingCcdCaseData.getSubscriptions() : Subscriptions.builder().build();
                Subscription newRepSubscription = gapsCaseData.getSubscriptions() != null
                    ? gapsCaseData.getSubscriptions().getRepresentativeSubscription() : Subscription.builder().build();
                existingSubscriptions = existingSubscriptions.toBuilder()
                    .representativeSubscription(newRepSubscription).build();
                existingCcdCaseData.setSubscriptions(existingSubscriptions);
                repUpdated = true;
            }
        }
        return repUpdated;
    }


}
