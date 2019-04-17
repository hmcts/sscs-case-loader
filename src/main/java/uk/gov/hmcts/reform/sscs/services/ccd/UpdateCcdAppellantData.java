package uk.gov.hmcts.reform.sscs.services.ccd;

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
            existingCcdCaseData.getAppeal().setAppellant(gapsAppellant);
            return true;
        }

        boolean appellantNameChanged = updateCcdAppellantName(gapsAppellant, existingCcdAppellant);
        boolean appellantContactChanged = updateCcdAppellantContact(gapsAppellant, existingCcdAppellant);
        boolean appellantIdentityChanged = updateCcdAppellantIdentity(gapsAppellant, existingCcdAppellant);

        boolean appointeeChanged = updateCcdAppointee(gapsAppellant, existingCcdAppellant);

        return appellantNameChanged || appellantContactChanged || appellantIdentityChanged || appointeeChanged;
    }

    private boolean updateCcdAppellantIdentity(Appellant gapsAppellant, Appellant existingCcdAppellant) {
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

    private boolean updateCcdAppellantName(Appellant gapsAppellant, Appellant existingCcdAppellant) {
        Name gapsAppellantName = gapsAppellant.getName();
        Name existingCcdAppellantName = existingCcdAppellant.getName();

        if (null == existingCcdAppellantName) {
            existingCcdAppellant.setName(gapsAppellantName);
            return true;
        }
        boolean dataChanged = false;
        if (null != gapsAppellantName) {
            if (StringUtils.isNotBlank(gapsAppellantName.getFirstName())
                && !gapsAppellantName.getFirstName().equals(existingCcdAppellantName.getFirstName())) {
                existingCcdAppellantName.setFirstName(gapsAppellantName.getFirstName());
                dataChanged = true;
            }
            if (StringUtils.isNotBlank(gapsAppellantName.getLastName())
                && !gapsAppellantName.getLastName().equals(existingCcdAppellantName.getLastName())) {
                existingCcdAppellantName.setLastName(gapsAppellantName.getLastName());
                dataChanged = true;
            }
        }
        return dataChanged;
    }

    private boolean updateCcdAppointee(Appellant gapsAppellant, Appellant existingCcdAppellant) {
        if (null == gapsAppellant.getAppointee()) {
            return false;
        }

        if (null == existingCcdAppellant.getAppointee()) {
            existingCcdAppellant.setAppointee(gapsAppellant.getAppointee());
            return true;
        }

        boolean appointeeNameChanged = updateCcdAppointeeName(
            gapsAppellant.getAppointee(),
            existingCcdAppellant.getAppointee()
        );
        boolean appointeeContactChanged = updateCcdAppointeeContact(
            gapsAppellant.getAppointee(),
            existingCcdAppellant.getAppointee()
        );
        boolean appointeeIdentityChanged = updateCcdAppointeeIdentity(
            gapsAppellant.getAppointee(),
            existingCcdAppellant.getAppointee()
        );

        return appointeeNameChanged || appointeeContactChanged || appointeeIdentityChanged;
    }

    private boolean updateCcdAppointeeIdentity(Appointee gapsAppointee, Appointee existingCcdAppointee) {
        Identity gapsAppointeeIdentity = gapsAppointee == null || gapsAppointee.getIdentity() == null
            ? null : gapsAppointee.getIdentity();
        Identity existingCcdAppointeeIdentity =
            existingCcdAppointee == null || existingCcdAppointee.getIdentity() == null
            ? null : existingCcdAppointee.getIdentity();

        if (null == existingCcdAppointeeIdentity) {
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
        Contact gapsAppellantContact = gapsAppointee == null || gapsAppointee.getContact() == null
            ? null : gapsAppointee.getContact();
        Contact existingCcdAppellantContact = existingCcdAppointee == null || existingCcdAppointee.getContact() == null
            ? null : existingCcdAppointee.getContact();

        if (null == existingCcdAppellantContact) {
            existingCcdAppointee.setContact(gapsAppellantContact);
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

    private boolean updateCcdAppointeeName(Appointee gapsAppointee, Appointee existingCcdAppointee) {
        Name gapsAppellantName = gapsAppointee == null || gapsAppointee.getName() == null
            ? null : gapsAppointee.getName();
        Name existingCcdAppellantName = existingCcdAppointee == null || existingCcdAppointee.getName() == null
            ? null : existingCcdAppointee.getName();

        if (null == existingCcdAppellantName) {
            existingCcdAppointee.setName(gapsAppellantName);
            return true;
        }
        boolean dataChanged = false;
        if (null != gapsAppellantName) {
            if (StringUtils.isNotBlank(gapsAppellantName.getFirstName())
                && !gapsAppellantName.getFirstName().equals(existingCcdAppellantName.getFirstName())) {
                existingCcdAppellantName.setFirstName(gapsAppellantName.getFirstName());
                dataChanged = true;
            }
            if (StringUtils.isNotBlank(gapsAppellantName.getLastName())
                && !gapsAppellantName.getLastName().equals(existingCcdAppellantName.getLastName())) {
                existingCcdAppellantName.setLastName(gapsAppellantName.getLastName());
                dataChanged = true;
            }
        }

        return dataChanged;
    }
}
