package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.util.UkMobile;

@Slf4j
final class UpdateCcdRepresentative {

    private UpdateCcdRepresentative() {
        // Empty
    }

    static boolean updateCcdRepresentative(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {
        boolean repUpdated = false;
        if (gapsCaseData.getAppeal() != null && gapsCaseData.getAppeal().getRep() != null) {
            Representative rep = gapsCaseData.getAppeal().getRep();
            Representative existingRepresentative = existingCcdCaseData.getAppeal().getRep();
            if (existingRepresentative == null || existingRepresentative.getName() == null
                || existingRepresentative.getContact() == null
                || !existingRepresentative.getName().equals(rep.getName())
                || !existingRepresentative.getContact().equals(rep.getContact())
            ) {
                updateReps(gapsCaseData, existingCcdCaseData, rep);
                final UpdateSubscription.SubscriptionUpdate subscriptionUpdate =
                    new UpdateSubscription.SubscriptionUpdate() {
                    @Override
                    public Subscription getSubscription(Subscriptions subscriptions) {
                        return subscriptions.getRepresentativeSubscription();
                    }

                    @Override
                    public Subscriptions updateExistingSubscriptions(Subscription subscription) {
                        return existingCcdCaseData.getSubscriptions().toBuilder()
                            .representativeSubscription(subscription).build();
                    }
                };
                UpdateSubscription.updateSubscription(gapsCaseData, existingCcdCaseData, subscriptionUpdate);
                repUpdated = true;
            }
        }
        return repUpdated;
    }

    private static void updateReps(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData, Representative rep) {
        if (existingCcdCaseData.getAppeal().getRep() == null) {
            existingCcdCaseData.getAppeal().setRep(Representative.builder().build());
        }
        if (rep.getContact() != null) {
            updateContact(gapsCaseData.getCaseReference(), existingCcdCaseData, rep);
        }
        if (rep.getName() != null) {
            existingCcdCaseData.getAppeal().getRep().setName(rep.getName());
        }
        if (rep.getAddress() != null) {
            existingCcdCaseData.getAppeal().getRep().setAddress(rep.getAddress());
        }
    }

    private static void updateContact(String caseRef, SscsCaseData existingCcdCaseData, Representative rep) {
        String mobileNumber;
        if (UkMobile.validate(rep.getContact().getMobile())) {
            mobileNumber = rep.getContact().getMobile();
        } else {
            log.info("Invalid Uk mobile no: {} In Reps Contact Details for the case reference: {}",
                rep.getContact().getMobile(), caseRef);
            Contact existingRepsContact = existingCcdCaseData.getAppeal().getRep().getContact();
            mobileNumber = existingRepsContact != null ? existingRepsContact.getMobile() : null;
        }
        Contact contact = Contact.builder()
            .email(rep.getContact().getEmail())
            .phone(rep.getContact().getPhone())
            .mobile(mobileNumber)
            .build();

        existingCcdCaseData.getAppeal().getRep().setContact(contact);
    }

}
