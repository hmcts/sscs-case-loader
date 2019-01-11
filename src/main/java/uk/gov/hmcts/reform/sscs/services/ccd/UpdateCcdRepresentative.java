package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.Contact;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;
import uk.gov.hmcts.reform.sscs.util.UkMobile;

@Slf4j
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
            Representative existingRepresentative = existingCcdCaseData.getAppeal().getRep();
            if (existingRepresentative == null || existingRepresentative.getName() == null
                || existingRepresentative.getContact() == null
                || !existingRepresentative.getName().equals(rep.getName())
                || !existingRepresentative.getContact().equals(rep.getContact())
            ) {
                updateReps(gapsCaseData, existingCcdCaseData, rep);
                updateRepresentativeSubscription(gapsCaseData, existingCcdCaseData);
                repUpdated = true;
            }
        }
        return repUpdated;
    }

    private static void updateReps(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData, Representative rep) {
        if (existingCcdCaseData.getAppeal().getRep() == null) {
            existingCcdCaseData.getAppeal().setRep(gapsCaseData.getAppeal().getRep());
        } else {
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
    }

    private static void updateContact(String caseRef, SscsCaseData existingCcdCaseData, Representative rep) {
        String mobileNumber;
        if (UkMobile.validate(rep.getContact().getMobile())) {
            mobileNumber = rep.getContact().getMobile();
        } else {
            log.info("Invalid Uk mobile no: {} for the case reference: {}",
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
