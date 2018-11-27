package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Evidence;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.UpdateType;
import uk.gov.hmcts.reform.sscs.service.RegionalProcessingCenterService;


@Service
@Slf4j
public class CcdCasesSender {

    public static final String SSCS_APPEAL_UPDATED_EVENT = "SSCS - appeal updated event";
    public static final String UPDATED_SSCS = "Updated SSCS";
    @Value("${rpc.venue.id.enabled}")
    private boolean lookupRpcByVenueId;
    private final CcdService ccdService;
    private final UpdateCcdCaseService updateCcdCaseService;
    private final RegionalProcessingCenterService regionalProcessingCenterService;

    @Autowired
    CcdCasesSender(CcdService ccdService,
                   UpdateCcdCaseService updateCcdCaseService,
                   RegionalProcessingCenterService regionalProcessingCenterService) {
        this.updateCcdCaseService = updateCcdCaseService;
        this.regionalProcessingCenterService = regionalProcessingCenterService;
        this.ccdService = ccdService;
    }

    public void sendCreateCcdCases(SscsCaseData caseData, IdamTokens idamTokens) {
        if (!lookupRpcByVenueId) {
            addRegionalProcessingCenter(caseData);
        }
        ccdService.createCase(caseData, idamTokens);
    }

    public void sendUpdateCcdCases(SscsCaseData caseData, SscsCaseDetails existingCcdCase, IdamTokens idamTokens) {
        String latestEventType = caseData.getLatestEventType();
        if (latestEventType != null) {
            SscsCaseData existingCcdCaseData = existingCcdCase.getData();
            existingCcdCaseData.setCaseReference(caseData.getCaseReference());
            addMissingInfo(caseData, existingCcdCaseData);
            checkNewEvidenceReceived(caseData, existingCcdCase, idamTokens);
            ifThereIsChangesThenUpdateCase(caseData, existingCcdCaseData, existingCcdCase.getId(), idamTokens);
        }
    }

    private void addMissingInfo(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        if (!lookupRpcByVenueId) {
            addRegionalProcessingCenter(existingCcdCaseData);
        }
        addMissingExistingHearings(caseData, existingCcdCaseData);
    }

    private void ifThereIsChangesThenUpdateCase(SscsCaseData caseData, SscsCaseData existingCcdCaseData,
                                                Long existingCaseId, IdamTokens idamTokens) {

        UpdateType updateType = updateCcdRecordForChangesAndReturnUpdateType(caseData, existingCcdCaseData);

        if (UpdateType.EVENT_UPDATE == updateType) {
            updateCcdCaseService
                .updateCase(existingCcdCaseData, existingCaseId, caseData.getLatestEventType(),
                    SSCS_APPEAL_UPDATED_EVENT, UPDATED_SSCS, idamTokens);
        } else if (UpdateType.DATA_UPDATE == updateType) {
            updateCcdCaseService
                .updateCase(existingCcdCaseData, existingCaseId, "caseUpdated",
                    SSCS_APPEAL_UPDATED_EVENT, UPDATED_SSCS, idamTokens);
        } else {
            log.debug("*** case-loader *** No case update needed for case reference: {}", caseData.getCaseReference());
        }
    }

    private void addMissingExistingHearings(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        List<Hearing> gaps2Hearings = caseData.getHearings();
        List<Hearing> ccdCaseDataHearings = existingCcdCaseData.getHearings();
        ArrayList<Hearing> hearingArrayList = new ArrayList<>();

        if (null != ccdCaseDataHearings) {
            if (null != gaps2Hearings) {
                Set<String> gaps2HearingDateTime = gaps2Hearings
                    .stream()
                    .map(hearing -> getMissingHearingDateTime(hearing.getValue()))
                    .collect(toSet());

                List<Hearing> missingHearings = ccdCaseDataHearings
                    .stream()
                    .filter(hearing ->
                        !gaps2HearingDateTime.contains(getMissingHearingDateTime(hearing.getValue())))
                    .collect(toList());

                hearingArrayList.addAll(gaps2Hearings);
                hearingArrayList.addAll(missingHearings);
            } else {
                hearingArrayList.addAll(ccdCaseDataHearings);
            }

            existingCcdCaseData.setHearings(hearingArrayList);
        } else {
            existingCcdCaseData.setHearings(gaps2Hearings);
        }
    }

    private String getMissingHearingDateTime(HearingDetails details) {
        return details.getHearingDate() + details.getTime();
    }

    private boolean thereIsAnEventChange(SscsCaseData caseData, SscsCaseData existingCcdCaseData) {
        return existingCcdCaseData.getEvents() == null
            || caseData.getEvents().size() != existingCcdCaseData.getEvents().size();
    }

    private void checkNewEvidenceReceived(SscsCaseData caseData, SscsCaseDetails existingCase, IdamTokens idamTokens) {
        Evidence newEvidence = caseData.getEvidence();
        SscsCaseData existingCaseData = existingCase.getData();
        Evidence existingEvidence = existingCaseData.getEvidence();
        if (newEvidence != null && existingEvidence != null && !existingEvidence.equals(newEvidence)) {
            existingCaseData.setEvidence(newEvidence);
            updateCcdCaseService.updateCase(existingCaseData, existingCase.getId(), "evidenceReceived",
                SSCS_APPEAL_UPDATED_EVENT, UPDATED_SSCS, idamTokens);
        }
    }

    private void addRegionalProcessingCenter(SscsCaseData caseData) {
        RegionalProcessingCenter regionalProcessingCenter = regionalProcessingCenterService
            .getByScReferenceCode(caseData.getCaseReference());
        if (null != regionalProcessingCenter) {
            caseData.setRegion(regionalProcessingCenter.getName());
            caseData.setRegionalProcessingCenter(regionalProcessingCenter);
        }
    }

    private UpdateType updateCcdRecordForChangesAndReturnUpdateType(SscsCaseData gapsCaseData,
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

        Appellant gapsAppellant = gapsCaseData.getAppeal().getAppellant();
        Appeal existingAppeal = existingCcdCaseData.getAppeal();

        if (null == existingAppeal) {
            existingCcdCaseData.setAppeal(gapsCaseData.getAppeal());
            return true;
        }

        Appellant existingCcdAppellant = existingCcdCaseData.getAppeal().getAppellant();

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
}
