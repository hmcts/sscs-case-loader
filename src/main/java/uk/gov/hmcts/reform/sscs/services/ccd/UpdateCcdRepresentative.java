package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

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
        if (gapsCaseData.getAppeal() != null) {
            if (gapsCaseData.getAppeal().getRep() != null) {
                Representative rep = gapsCaseData.getAppeal().getRep();
                Representative existingRepresentative = existingCcdCaseData.getAppeal().getRep();
                if (hasRepChanged(rep, existingRepresentative)) {
                    updateReps(gapsCaseData, existingCcdCaseData, rep);
                    UpdateSubscription.updateSubscription(
                        gapsCaseData,
                        existingCcdCaseData,
                        getRepSubscriptionUpdate(existingCcdCaseData)
                    );
                    repUpdated = true;
                }
            } else if (hasExistingRepSubscription(existingCcdCaseData)) {
                // No rep, clear down rep subscription
                UpdateSubscription.updateSubscription(
                    gapsCaseData,
                    existingCcdCaseData,
                    getClearedRepSubscriptionUpdate(existingCcdCaseData)
                );
                repUpdated = true;
            }
            if (repUpdated && nonNull(existingCcdCaseData.getAppeal().getRep())) {
                boolean hasRep = ((nonNull(existingCcdCaseData.getAppeal().getRep().getName())
                    && nonNull(existingCcdCaseData.getAppeal().getRep().getName().getLastName()))
                    || nonNull(existingCcdCaseData.getAppeal().getRep().getOrganisation()));
                String hasRepYesOrNo = hasRep ? "Yes" : "No";
                if (!equalsIgnoreCase(existingCcdCaseData.getAppeal().getRep().getHasRepresentative(), hasRepYesOrNo)) {
                    existingCcdCaseData.getAppeal().getRep().setHasRepresentative(hasRepYesOrNo);
                }
            }
        }
        return repUpdated;
    }

    private static boolean hasExistingRepSubscription(SscsCaseData existingCcdCaseData) {
        return existingCcdCaseData != null
            && existingCcdCaseData.getSubscriptions() != null
            && existingCcdCaseData.getSubscriptions().getRepresentativeSubscription() != null
            && existingCcdCaseData.getSubscriptions().getRepresentativeSubscription().getSubscribeSms() != null
            && existingCcdCaseData.getSubscriptions().getRepresentativeSubscription().getSubscribeEmail() != null;
    }

    private static boolean hasRepChanged(Representative rep, Representative existingRepresentative) {
        return existingRepresentative == null
            || existingRepresentative.getContact() == null
            || !existingRepresentative.getContact().equals(rep.getContact());
    }

    private static UpdateSubscription.SubscriptionUpdate getClearedRepSubscriptionUpdate(SscsCaseData caseData) {
        return new UpdateSubscription.SubscriptionUpdate() {
            @Override
            public Subscription getSubscription(Subscriptions subscriptions) {
                return subscriptions.getRepresentativeSubscription();
            }

            @Override
            public Subscriptions updateExistingSubscriptions(Subscription subscription) {
                Subscription clearedSubscription = subscription.toBuilder()
                    .wantSmsNotifications("No")
                    .mobile("")
                    .subscribeSms("No")
                    .email("")
                    .subscribeEmail("No")
                    .build();

                return caseData.getSubscriptions().toBuilder()
                    .representativeSubscription(clearedSubscription).build();
            }
        };
    }

    private static UpdateSubscription.SubscriptionUpdate getRepSubscriptionUpdate(SscsCaseData caseData) {
        return new UpdateSubscription.SubscriptionUpdate() {
            @Override
            public Subscription getSubscription(Subscriptions subscriptions) {
                return subscriptions.getRepresentativeSubscription();
            }

            @Override
            public Subscriptions updateExistingSubscriptions(Subscription subscription) {
                return caseData.getSubscriptions().toBuilder()
                    .representativeSubscription(subscription).build();
            }
        };
    }

    private static void updateReps(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData, Representative rep) {
        if (existingCcdCaseData.getAppeal().getRep() == null) {
            existingCcdCaseData.getAppeal().setRep(Representative.builder().build());
        }
        if (rep.getContact() != null) {
            updateContact(gapsCaseData.getCaseReference(), existingCcdCaseData, rep);
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
