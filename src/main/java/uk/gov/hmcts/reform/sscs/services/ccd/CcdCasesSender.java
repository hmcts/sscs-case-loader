package uk.gov.hmcts.reform.sscs.services.ccd;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
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
                    SSCS_APPEAL_UPDATED_EVENT, UPDATED_SSCS,idamTokens);
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
                existingCcdCaseData.setHearings(hearingArrayList);

            } else {
                hearingArrayList.addAll(ccdCaseDataHearings);
                existingCcdCaseData.setHearings(hearingArrayList);

            }

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

    private UpdateType updateCcdRecordForChangesAndReturnUpdateType(SscsCaseData gaps2CaseData,
                                                                    SscsCaseData existingCcdCaseData) {
        boolean eventChanged = false;
        boolean dataChange = false;

        if (thereIsAnEventChange(gaps2CaseData, existingCcdCaseData)) {
            eventChanged = true;
            existingCcdCaseData.setEvents(gaps2CaseData.getEvents());
        }

        dataChange = updateParties(gaps2CaseData, existingCcdCaseData, dataChange);

        dataChange = updateHearingOptions(gaps2CaseData, existingCcdCaseData, dataChange);

        dataChange = updateHearingType(gaps2CaseData, existingCcdCaseData, dataChange);

        updateGeneratedFields(existingCcdCaseData);

        if (null != gaps2CaseData.getDwpTimeExtension() && !gaps2CaseData.getDwpTimeExtension().isEmpty()) {
            existingCcdCaseData.setDwpTimeExtension(gaps2CaseData.getDwpTimeExtension());
        }

        if (eventChanged) {
            return UpdateType.EVENT_UPDATE;
        } else if (dataChange) {
            return UpdateType.DATA_UPDATE;
        }

        return UpdateType.NO_UPDATE;
    }

    private void updateGeneratedFields(SscsCaseData existingCcdCaseData) {
        existingCcdCaseData
            .setGeneratedSurname(existingCcdCaseData.getAppeal().getAppellant().getName().getLastName());
        existingCcdCaseData
            .setGeneratedDob(existingCcdCaseData.getAppeal().getAppellant().getIdentity().getDob());

        existingCcdCaseData
            .setGeneratedNino(existingCcdCaseData.getAppeal().getAppellant().getIdentity().getNino());

        if (null != existingCcdCaseData.getAppeal().getAppellant().getContact()) {
            existingCcdCaseData
                .setGeneratedEmail(existingCcdCaseData.getAppeal().getAppellant().getContact().getEmail());
            existingCcdCaseData
                .setGeneratedMobile(existingCcdCaseData.getAppeal().getAppellant().getContact().getMobile());
        }
    }

    private boolean updateHearingType(SscsCaseData gaps2CaseData,
                                      SscsCaseData existingCcdCaseData,
                                      boolean dataChange) {
        if (StringUtils.isNotBlank(gaps2CaseData.getAppeal().getHearingType())) {
            if (StringUtils.isNotBlank(existingCcdCaseData.getAppeal().getHearingType())) {
                if (!gaps2CaseData.getAppeal().getHearingType()
                    .equals(existingCcdCaseData.getAppeal().getHearingType())) {
                    dataChange = true;
                    existingCcdCaseData.getAppeal().setHearingType(gaps2CaseData.getAppeal().getHearingType());
                }
            } else {
                dataChange = true;
                existingCcdCaseData.getAppeal().setHearingType(gaps2CaseData.getAppeal().getHearingType());
            }
        }
        return dataChange;
    }

    private boolean updateParties(SscsCaseData gaps2CaseData,
                                    SscsCaseData existingCcdCaseData,
                                    boolean dataChange) {

        Appeal gaps2Appeal = gaps2CaseData.getAppeal();

        if (null == gaps2Appeal) {
            return dataChange;
        }

        Appellant gaps2Appellant = gaps2Appeal.getAppellant();

        if (null == gaps2Appellant) {
            return dataChange;
        }

        Appeal existingAppeal = existingCcdCaseData.getAppeal();
        Appellant existingAppellant = existingAppeal.getAppellant();
        Name gaps2Name = gaps2Appellant.getName();
        Name ccdName = existingAppellant.getName();

        if (!gaps2Name.equals(ccdName)) {
            dataChange = true;
            existingAppellant.setName(gaps2Name);
        }

        Contact gaps2Contact = gaps2Appellant.getContact();
        Contact ccdContact = existingAppellant.getContact();

        if (!gaps2Contact.equals(ccdContact)) {
            dataChange = true;
            existingAppellant.setContact(gaps2Contact);
        }

        Identity gaps2Identity = gaps2Appellant.getIdentity();
        Identity ccdIdentity = existingAppellant.getIdentity();

        if (!gaps2Identity.equals(ccdIdentity)) {
            dataChange = true;
            existingAppellant.setIdentity(gaps2Identity);
        }

        return dataChange;
    }

    private boolean updateHearingOptions(SscsCaseData gaps2CaseData,
                                         SscsCaseData existingCcdCaseData,
                                         boolean dataChange) {
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
                    dataChange = true;
                    existingCcdCaseData.getAppeal().getHearingOptions().setWantsToAttend(gaps2WantsToAttend);
                }
            } else {
                dataChange = true;
                existingCcdCaseData.getAppeal()
                    .setHearingOptions(HearingOptions.builder().wantsToAttend(gaps2WantsToAttend).build());
            }
        }

        return dataChange;
    }
}
