package uk.gov.hmcts.reform.sscs.services.ccd;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Contact;
import uk.gov.hmcts.reform.sscs.ccd.domain.Identity;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Service
class UpdateCcdAppellantData {
    boolean updateCcdAppellantData(SscsCaseData gapsCaseData,
                                   SscsCaseData existingCcdCaseData) {
        if (null == gapsCaseData.getAppeal().getAppellant()) {
            return false;
        }

        Appeal existingAppeal = existingCcdCaseData.getAppeal();

        if (null == existingAppeal) {
            existingCcdCaseData.setAppeal(gapsCaseData.getAppeal());
            return true;
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

        return appellantNameChanged || appellantContactChanged || appellantIdentityChanged;
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
}
