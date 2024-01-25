package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static uk.gov.hmcts.reform.sscs.exceptions.FeignExceptionLogger.debugCaseLoaderException;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

@Slf4j
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

        boolean hasAppointee = gapsAppellant.getAppointee() != null
            && gapsAppellant.getAppointee().getName() != null
            && gapsAppellant.getAppointee().getName().getLastName() != null;

        if (appointeeChanged) {
            final String hasAppointeeYesOrNo = hasAppointee ? "Yes" : "No";
            if (!equalsIgnoreCase(existingCcdAppellant.getIsAppointee(), hasAppointeeYesOrNo)) {
                existingCcdAppellant.setIsAppointee(hasAppointeeYesOrNo);
            }
        }

        if (!hasAppointee && appellantContactChanged) {
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
            try {
                existingCcdAppellant.setIdentity(gapsAppellantIdentity);
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update appellant identity");
            }
            return true;
        }

        if (null != gapsAppellantIdentity && StringUtils.isNotBlank(gapsAppellantIdentity.getNino())
            && !gapsAppellantIdentity.getNino().equals(existingCcdAppellantIdentity.getNino())) {
            try {
                existingCcdAppellantIdentity.setNino(gapsAppellantIdentity.getNino());
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update appellant Nino");
            }
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
            try {
                existingCcdAppellantContact.setEmail(gapsAppellantContact.getEmail());
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update appellant email");
            }
            return true;
        }

        if (null != gapsAppellantContact && StringUtils.isNotBlank(gapsAppellantContact.getMobile())
            && !gapsAppellantContact.getMobile().equals(existingCcdAppellantContact.getMobile())) {
            try {
                existingCcdAppellantContact.setMobile(gapsAppellantContact.getMobile());
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update appellant mobile");
            }
            return true;
        }

        if (null != gapsAppellantContact && StringUtils.isNotBlank(gapsAppellantContact.getPhone())
            && !gapsAppellantContact.getPhone().equals(existingCcdAppellantContact.getPhone())) {
            try {
                existingCcdAppellantContact.setPhone(gapsAppellantContact.getPhone());
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update appellant phone number");
            }
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
            try {
                existingCcdAppointee.setIdentity(gapsAppointeeIdentity);
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update appointee identity");
            }
            return true;
        }

        if (null != gapsAppointeeIdentity && StringUtils.isNotBlank(gapsAppointeeIdentity.getNino())
            && !gapsAppointeeIdentity.getNino().equals(existingCcdAppointeeIdentity.getNino())) {
            try {
                existingCcdAppointeeIdentity.setNino(gapsAppointeeIdentity.getNino());
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update appointee Nino");
            }
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
            try {
                existingCcdAppointeeContact.setEmail(gapsAppointeeContact.getEmail());
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update appointee email");
            }

            return true;
        }

        if (null != gapsAppointeeContact && StringUtils.isNotBlank(gapsAppointeeContact.getMobile())
            && !gapsAppointeeContact.getMobile().equals(existingCcdAppointeeContact.getMobile())) {
            try {
                existingCcdAppointeeContact.setMobile(gapsAppointeeContact.getMobile());
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update appointee mobile");
            }
            return true;
        }

        if (null != gapsAppointeeContact && StringUtils.isNotBlank(gapsAppointeeContact.getPhone())
            && !gapsAppointeeContact.getPhone().equals(existingCcdAppointeeContact.getPhone())) {
            try {
                existingCcdAppointeeContact.setPhone(gapsAppointeeContact.getPhone());
            } catch (FeignException e) {
                debugCaseLoaderException(log, e, "Could not update appointee phone number");
            }
            return true;
        }

        return false;
    }
}
