package uk.gov.hmcts.reform.sscs.services.ccd;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Service
class UpdateGeneratedFields {
    void updateGeneratedFields(SscsCaseData existingCcdCaseData) {
        if (validData(existingCcdCaseData)) {
            Appellant appellant = existingCcdCaseData.getAppeal().getAppellant();
            updateAppellantName(existingCcdCaseData, appellant);
            updateIdentity(existingCcdCaseData, appellant);
            updateContact(existingCcdCaseData, appellant);
        }
    }

    private boolean validData(SscsCaseData existingCcdCaseData) {
        return null != existingCcdCaseData && null != existingCcdCaseData.getAppeal()
            && null != existingCcdCaseData.getAppeal().getAppellant();
    }

    private void updateAppellantName(SscsCaseData existingCcdCaseData, Appellant appellant) {
        if (null != appellant.getName() && StringUtils.isNotBlank(appellant.getName().getLastName())) {
            existingCcdCaseData.setGeneratedSurname(appellant.getName().getLastName());
        }
    }

    private void updateContact(SscsCaseData existingCcdCaseData, Appellant appellant) {
        if (null != appellant.getContact()) {
            if (StringUtils.isNotBlank(appellant.getContact().getEmail())) {
                existingCcdCaseData.setGeneratedEmail(appellant.getContact().getEmail());
            }
            if (StringUtils.isNotBlank(appellant.getContact().getMobile())) {
                existingCcdCaseData.setGeneratedMobile(appellant.getContact().getMobile());
            }
        }
    }

    private void updateIdentity(SscsCaseData existingCcdCaseData, Appellant appellant) {
        if (null != appellant.getIdentity()) {
            if (StringUtils.isNotBlank(appellant.getIdentity().getDob())) {
                existingCcdCaseData.setGeneratedDob(appellant.getIdentity().getDob());
            }
            if (StringUtils.isNotBlank(appellant.getIdentity().getNino())) {
                existingCcdCaseData.setGeneratedNino(appellant.getIdentity().getNino());
            }
        }
    }
}
