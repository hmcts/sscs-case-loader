package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYesOrNo;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

@Service
class UpdateCcdAppellantData {
    boolean updateCcdAppellantData(SscsCaseData gapsCaseData,
                                   SscsCaseData existingCcdCaseData) {
        if (null == gapsCaseData.getAppeal().getAppellant()) {
            return false;
        }

        Appellant existingCcdAppellant = existingCcdCaseData.getAppeal().getAppellant();
        Appellant gapsAppellant = gapsCaseData.getAppeal().getAppellant();

        if (null == existingCcdAppellant) {
            existingCcdCaseData.getAppeal().setAppellant(Appellant.builder()
                .address(gapsAppellant.getAddress())
                .contact(gapsAppellant.getContact())
                .identity(gapsAppellant.getIdentity())
                .appointee(gapsAppellant.getAppointee() == null ? null : Appointee.builder()
                    .contact(gapsAppellant.getAppointee().getContact())
                    .identity(gapsAppellant.getAppointee().getIdentity())
                    .address(gapsAppellant.getAppointee().getAddress())
                    .build())
                .confidentialityRequired(gapsAppellant.getConfidentialityRequired())
                .isAddressSameAsAppointee(gapsAppellant.getIsAddressSameAsAppointee())
                .isAppointee(gapsAppellant.getIsAppointee())
                .role(gapsAppellant.getRole())
                .build());
            return true;
        }

        final boolean appellantContactChanged = updateCcdAppellantContact(gapsAppellant, existingCcdAppellant);
        final boolean appellantIdentityChanged = updateCcdAppellantIdentity(gapsCaseData, existingCcdCaseData);

        boolean appointeeChanged = updateCcdAppointee(gapsAppellant, existingCcdAppellant);

        YesNo hasAppointee = isYesOrNo(nonNull(gapsAppellant.getAppointee())
            && nonNull(gapsAppellant.getAppointee().getName())
            && nonNull(gapsAppellant.getAppointee().getName().getLastName()));

        if (appointeeChanged && !hasAppointee.equals(existingCcdAppellant.getIsAppointee())) {
            existingCcdAppellant.setIsAppointee(hasAppointee);
        }

        if (!isYes(hasAppointee) && appellantContactChanged) {
            final UpdateSubscription.SubscriptionUpdate appellantSubscriptionUpdate =
                new UpdateSubscription.SubscriptionUpdate() {
                @Override
                public Subscription getSubscription(Subscriptions subscriptions) {
                    return subscriptions.getAppellantSubscription();
                }

                @Override
                public Subscriptions updateExistingSubscriptions(Subscription subscription) {
                    return existingCcdCaseData.getSubscriptions().toBuilder()
                        .appellantSubscription(subscription).build();
                }
            };
            UpdateSubscription.updateSubscription(gapsCaseData, existingCcdCaseData, appellantSubscriptionUpdate);
        }

        if (appointeeChanged) {
            final UpdateSubscription.SubscriptionUpdate appointeeSubscriptionUpdate =
                new UpdateSubscription.SubscriptionUpdate() {
                @Override
                public Subscription getSubscription(Subscriptions subscriptions) {
                    return subscriptions.getAppointeeSubscription();
                }

                @Override
                public Subscriptions updateExistingSubscriptions(Subscription subscription) {
                    return existingCcdCaseData.getSubscriptions().toBuilder()
                        .appointeeSubscription(subscription).build();
                }
            };
            UpdateSubscription.updateSubscription(gapsCaseData, existingCcdCaseData, appointeeSubscriptionUpdate);
        }

        return appellantContactChanged || appellantIdentityChanged || appointeeChanged;
    }

    private boolean updateCcdAppellantIdentity(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {
        Appellant existingCcdAppellant = existingCcdCaseData.getAppeal().getAppellant();
        Appellant gapsAppellant = gapsCaseData.getAppeal().getAppellant();

        Identity gapsAppellantIdentity = gapsAppellant.getIdentity();
        Identity existingCcdAppellantIdentity = existingCcdAppellant.getIdentity();

        if (null == existingCcdAppellantIdentity) {
            existingCcdAppellant.setIdentity(gapsAppellantIdentity);
            return true;
        }

        if (null != gapsAppellantIdentity && StringUtils.isNotBlank(gapsAppellantIdentity.getNino())
            && !gapsAppellantIdentity.getNino().equals(existingCcdAppellantIdentity.getNino())) {
            existingCcdAppellantIdentity.setNino(gapsAppellantIdentity.getNino());
            return true;
        }

        return false;

    }

    private boolean updateCcdAppellantContact(Appellant gapsAppellant, Appellant existingCcdAppellant) {
        Contact gapsAppellantContact = gapsAppellant.getContact();
        Contact existingCcdAppellantContact = existingCcdAppellant.getContact();

        if (null == existingCcdAppellantContact) {
            existingCcdAppellant.setContact(gapsAppellantContact);
            return true;
        }

        if (null != gapsAppellantContact && StringUtils.isNotBlank(gapsAppellantContact.getEmail())
            && !gapsAppellantContact.getEmail().equals(existingCcdAppellantContact.getEmail())) {
            existingCcdAppellantContact.setEmail(gapsAppellantContact.getEmail());
            return true;
        }

        if (null != gapsAppellantContact && StringUtils.isNotBlank(gapsAppellantContact.getMobile())
            && !gapsAppellantContact.getMobile().equals(existingCcdAppellantContact.getMobile())) {
            existingCcdAppellantContact.setMobile(gapsAppellantContact.getMobile());
            return true;
        }

        if (null != gapsAppellantContact && StringUtils.isNotBlank(gapsAppellantContact.getPhone())
            && !gapsAppellantContact.getPhone().equals(existingCcdAppellantContact.getPhone())) {
            existingCcdAppellantContact.setPhone(gapsAppellantContact.getPhone());
            return true;
        }

        return false;
    }

    private boolean updateCcdAppointee(Appellant gapsAppellant, Appellant existingCcdAppellant) {
        if (null == gapsAppellant.getAppointee()) {
            return false;
        }

        if (null == existingCcdAppellant.getAppointee()) {
            existingCcdAppellant.setAppointee(Appointee.builder()
                .contact(gapsAppellant.getAppointee().getContact())
                .identity(gapsAppellant.getAppointee().getIdentity())
                .address(gapsAppellant.getAppointee().getAddress())
                .build());
            return true;
        }

        boolean appointeeContactChanged = updateCcdAppointeeContact(
            gapsAppellant.getAppointee(),
            existingCcdAppellant.getAppointee()
        );
        boolean appointeeIdentityChanged = updateCcdAppointeeIdentity(
            gapsAppellant.getAppointee(),
            existingCcdAppellant.getAppointee()
        );

        return appointeeContactChanged || appointeeIdentityChanged;
    }

    private boolean updateCcdAppointeeIdentity(Appointee gapsAppointee, Appointee existingCcdAppointee) {
        Identity gapsAppointeeIdentity = gapsAppointee == null || gapsAppointee.getIdentity() == null
            ? null : gapsAppointee.getIdentity();
        Identity existingCcdAppointeeIdentity =
            existingCcdAppointee == null || existingCcdAppointee.getIdentity() == null
                ? null : existingCcdAppointee.getIdentity();

        if (null == existingCcdAppointeeIdentity) {
            if (null == existingCcdAppointee) {
                existingCcdAppointee = Appointee.builder().build();
            }
            existingCcdAppointee.setIdentity(gapsAppointeeIdentity);
            return true;
        }

        if (null != gapsAppointeeIdentity && StringUtils.isNotBlank(gapsAppointeeIdentity.getNino())
            && !gapsAppointeeIdentity.getNino().equals(existingCcdAppointeeIdentity.getNino())) {
            existingCcdAppointeeIdentity.setNino(gapsAppointeeIdentity.getNino());
            return true;
        }
        return false;

    }

    private boolean updateCcdAppointeeContact(Appointee gapsAppointee, Appointee existingCcdAppointee) {
        Contact gapsAppointeeContact = gapsAppointee == null || gapsAppointee.getContact() == null
            ? null : gapsAppointee.getContact();
        Contact existingCcdAppointeeContact = existingCcdAppointee == null || existingCcdAppointee.getContact() == null
            ? null : existingCcdAppointee.getContact();

        if (null == existingCcdAppointeeContact) {
            if (null == existingCcdAppointee) {
                existingCcdAppointee = Appointee.builder().build();
            }
            existingCcdAppointee.setContact(gapsAppointeeContact);
            return true;
        }

        if (null != gapsAppointeeContact && StringUtils.isNotBlank(gapsAppointeeContact.getEmail())
            && !gapsAppointeeContact.getEmail().equals(existingCcdAppointeeContact.getEmail())) {
            existingCcdAppointeeContact.setEmail(gapsAppointeeContact.getEmail());
            return true;
        }

        if (null != gapsAppointeeContact && StringUtils.isNotBlank(gapsAppointeeContact.getMobile())
            && !gapsAppointeeContact.getMobile().equals(existingCcdAppointeeContact.getMobile())) {
            existingCcdAppointeeContact.setMobile(gapsAppointeeContact.getMobile());
            return true;
        }

        if (null != gapsAppointeeContact && StringUtils.isNotBlank(gapsAppointeeContact.getPhone())
            && !gapsAppointeeContact.getPhone().equals(existingCcdAppointeeContact.getPhone())) {
            existingCcdAppointeeContact.setPhone(gapsAppointeeContact.getPhone());
            return true;
        }

        return false;
    }
}
