package uk.gov.hmcts.reform.sscs.services.ccd;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.models.UpdateType;

@Service
class UpdateCcdCaseData {
    UpdateType updateCcdRecordForChangesAndReturnUpdateType(SscsCaseData gapsCaseData,
                                                            SscsCaseData existingCcdCaseData) {
        boolean eventChanged = false;

        if (thereIsAnEventChange(gapsCaseData, existingCcdCaseData)) {
            eventChanged = true;
            existingCcdCaseData.setEvents(gapsCaseData.getEvents());
        }

        boolean dataChange = updateCcdData(gapsCaseData, existingCcdCaseData);
        updateGeneratedFields(existingCcdCaseData);

        if (null != gapsCaseData.getDwpTimeExtension() && !gapsCaseData.getDwpTimeExtension().isEmpty()) {
            existingCcdCaseData.setDwpTimeExtension(gapsCaseData.getDwpTimeExtension());
        }

        if (eventChanged) {
            return UpdateType.EVENT_UPDATE;
        } else if (dataChange) {
            return UpdateType.DATA_UPDATE;
        }

        return UpdateType.NO_UPDATE;
    }

    private boolean updateCcdData(SscsCaseData gapsCaseData, SscsCaseData existingCcdCaseData) {
        if (null != gapsCaseData.getAppeal()) {
            boolean updateParties = updateParties(gapsCaseData, existingCcdCaseData);
            boolean updateHearingOptions = updateHearingOptions(gapsCaseData, existingCcdCaseData);
            boolean updateHearingType = updateHearingType(gapsCaseData, existingCcdCaseData);
            return updateParties || updateHearingOptions || updateHearingType;
        }
        return false;
    }

    private void updateGeneratedFields(SscsCaseData existingCcdCaseData) {
        if (null == existingCcdCaseData.getAppeal() || null == existingCcdCaseData.getAppeal().getAppellant()) {
            return;
        }
        Appellant appellant = existingCcdCaseData.getAppeal().getAppellant();
        updateAppellantName(existingCcdCaseData, appellant);
        updateIdentity(existingCcdCaseData, appellant);
        updateContact(existingCcdCaseData, appellant);
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
                existingCcdCaseData.setGeneratedEmail(appellant.getContact().getMobile());
            }
        }
    }

    private void updateIdentity(SscsCaseData existingCcdCaseData, Appellant appellant) {
        if (null != appellant.getIdentity()) {
            if (StringUtils.isNotBlank(appellant.getIdentity().getDob())) {
                existingCcdCaseData.setGeneratedDob(appellant.getIdentity().getDob());
            }
            if (StringUtils.isNotBlank(appellant.getIdentity().getNino())) {
                existingCcdCaseData.setGeneratedDob(appellant.getIdentity().getNino());
            }
        }
    }

    private boolean updateHearingType(SscsCaseData gaps2CaseData,
                                      SscsCaseData existingCcdCaseData) {
        String gaps2HearingType = gaps2CaseData.getAppeal().getHearingType();
        if (StringUtils.isNotBlank(gaps2HearingType)) {
            String ccdHearingType = existingCcdCaseData.getAppeal().getHearingType();
            if (StringUtils.isNotBlank(ccdHearingType)) {
                if (!gaps2HearingType.equals(ccdHearingType)) {
                    existingCcdCaseData.getAppeal().setHearingType(gaps2HearingType);
                    return true;
                }
            } else {
                existingCcdCaseData.getAppeal().setHearingType(gaps2HearingType);
                return true;
            }
        }

        return false;
    }

    private boolean updateParties(SscsCaseData gapsCaseData,
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

    private boolean updateHearingOptions(SscsCaseData gaps2CaseData,
                                         SscsCaseData existingCcdCaseData) {
        String gaps2WantsToAttend = null;
        String ccdWantsToAttend = null;

        if (null != gaps2CaseData.getAppeal().getHearingOptions()
            && StringUtils.isNotBlank(gaps2CaseData.getAppeal().getHearingOptions().getWantsToAttend())) {
            gaps2WantsToAttend = gaps2CaseData.getAppeal().getHearingOptions().getWantsToAttend();
        }

        if (null != existingCcdCaseData.getAppeal().getHearingOptions()
            && StringUtils.isNotBlank(existingCcdCaseData.getAppeal().getHearingOptions().getWantsToAttend())) {
            ccdWantsToAttend = existingCcdCaseData.getAppeal().getHearingOptions().getWantsToAttend();
        }

        if (StringUtils.isNotBlank(gaps2WantsToAttend)) {
            if (StringUtils.isNotBlank(ccdWantsToAttend)) {
                if (!gaps2WantsToAttend.equals(ccdWantsToAttend)) {
                    existingCcdCaseData.getAppeal().getHearingOptions().setWantsToAttend(gaps2WantsToAttend);
                    return true;
                }
            } else {
                existingCcdCaseData.getAppeal().setHearingOptions(HearingOptions.builder()
                    .wantsToAttend(gaps2WantsToAttend)
                    .build());
                return true;
            }
        }
        return false;
    }

    private boolean thereIsAnEventChange(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        return existingCcdCaseData.getEvents() == null
            || caseData.getEvents().size() != existingCcdCaseData.getEvents().size();
    }

}
