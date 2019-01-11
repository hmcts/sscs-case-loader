package uk.gov.hmcts.reform.sscs.services.ccd;

import lombok.extern.slf4j.Slf4j;
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
                if (rep.getContact() != null) {
                    if (rep.getContact().getMobile() != null) {
                        rep.getContact()
                            .setMobile(checkValidUkMobileNumberElseReturnExisting(
                                rep, existingRepresentative, gapsCaseData.getCaseReference()));
                    }
                    existingCcdCaseData.getAppeal().setRep(rep);
                }
                if (rep.getName() != null) {
                    existingCcdCaseData.getAppeal().setRep(rep);
                }
                if (rep.getAddress() != null) {
                    existingCcdCaseData.getAppeal().setRep(rep);
                }
                updateRepresentativeSubscription(gapsCaseData, existingCcdCaseData);
                repUpdated = true;
            }
        }
        return repUpdated;
    }

    private static String checkValidUkMobileNumberElseReturnExisting(Representative rep,
                                                                     Representative existingRepresentative,
                                                                     String caseReference) {
        String validMobileNumber = null;
        boolean isValidUkMobile = UkMobile.check(rep.getContact().getMobile());
        if (isValidUkMobile) {
            validMobileNumber = rep.getContact().getMobile();
        } else if (existingRepresentative != null && existingRepresentative.getContact() != null) {
            validMobileNumber = existingRepresentative.getContact().getMobile();
        }

        if (!isValidUkMobile) {
            log.info("Invalid Uk mobile no: {} for the case reference: {}", caseReference, rep.getContact().getMobile());
        }
        return validMobileNumber;
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
