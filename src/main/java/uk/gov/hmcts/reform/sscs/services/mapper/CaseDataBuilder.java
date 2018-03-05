package uk.gov.hmcts.reform.sscs.services.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Address;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.BenefitType;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Contact;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Doc;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Documents;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.DwpTimeExtension;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.DwpTimeExtensionDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Evidence;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Hearing;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.HearingDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.HearingOptions;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Identity;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Name;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Venue;
import uk.gov.hmcts.reform.sscs.services.date.DateUtility;

@Service
public class CaseDataBuilder {
    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String Y = "Y";
    private final DateUtility dateUtility;

    @Autowired
    public CaseDataBuilder(DateUtility dateUtility) {
        this.dateUtility = dateUtility;
    }

    public List<Events> buildEvents(AppealCase appealCase) {

        List<Events> events = new ArrayList<>();
        for (MajorStatus majorStatus : appealCase.getMajorStatus()) {
            GapsEvent gapsEvent = GapsEvent.getGapsEventByStatus(majorStatus.getStatusId());
            if (gapsEvent != null) {
                Event event = Event.builder()
                    .type(gapsEvent.getType())
                    .description(gapsEvent.getDescription())
                    .date(majorStatus.getDateSet().toLocalDateTime().toString())
                    .build();

                events.add(Events.builder()
                    .value(event)
                    .build());
            }
        }
        events.sort(Collections.reverseOrder());
        return events;
    }

    public Identity buildIdentity(AppealCase appealCase) {
        return Identity.builder()
            .dob(dateUtility.getValidDate(appealCase.getParties().getDob()))
            .nino(appealCase.getAppealCaseNino())
            .build();
    }

    public Name buildName(AppealCase appealCase) {
        Parties parties = appealCase.getParties();
        return Name.builder()
            .title(parties.getTitle())
            .firstName(parties.getInitials())
            .lastName(parties.getSurname())
            .build();
    }

    public Address buildAddress(AppealCase appealCase) {
        return Address.builder()
            .postcode(appealCase.getParties().getPostCode())
            .build();
    }

    public Contact buildContact(AppealCase appealCase) {
        return Contact.builder()
            .email(appealCase.getParties().getEmail())
            .phone(appealCase.getParties().getPhone1())
            .mobile(appealCase.getParties().getPhone2())
            .build();
    }

    public BenefitType buildBenefitType(AppealCase appealCase) {
        return BenefitType.builder()
            .code(appealCase.getAppealCaseCaseCodeId())
            .build();
    }

    public HearingOptions buildHearingOptions(AppealCase appealCase) {
        return HearingOptions.builder()
            .other(Y.equals(appealCase.getParties().getDisabilityNeeds()) ? YES : NO)
            .build();
    }

    public List<Hearing> buildHearings(AppealCase appealCase) {
        List<Hearing> hearingsList = new ArrayList<>();
        HearingDetails hearings;

        if (appealCase.getHearing() != null) {
            hearings = HearingDetails.builder()
                .venue(Venue.builder().venueTown("Aberdeen").build())
                .hearingDate(dateUtility.getValidDate(appealCase.getHearing().getSessionDate()))
                .time(dateUtility.getValidTime(appealCase.getHearing().getAppealTime()))
                .adjourned(isAdjourned(appealCase.getMajorStatus()) ? YES : NO)
                .build();

            Hearing value = Hearing.builder()
                .value(hearings)
                .build();

            hearingsList.add(value);
        }

        return hearingsList;
    }

    public Evidence buildEvidence(AppealCase appealCase) {
        List<Documents> documentsList = new ArrayList<>();
        Doc doc;

        if (appealCase.getFurtherEvidence() != null) {
            doc = Doc.builder()
                .dateReceived(dateUtility.getValidDate(appealCase.getFurtherEvidence().getFeDateReceived()))
                .description(appealCase.getFurtherEvidence().getFeTypeofEvidenceId())
                .build();
            Documents documents = Documents.builder().value(doc).build();
            documentsList.add(documents);
        }

        return Evidence.builder()
            .documents(documentsList)
            .build();
    }

    public List<DwpTimeExtension> buildDwpTimeExtensions(AppealCase appealCase) {
        List<DwpTimeExtension> dwpTimeExtensionList = new ArrayList<>();
        List<PostponementRequests> postponementRequestsList = appealCase.getPostponementRequests();
        if (postponementRequestsList != null) {
            appealCase.getPostponementRequests().forEach(
                postponementRequests -> {
                    DwpTimeExtensionDetails dwpTimeExtensionDetails = DwpTimeExtensionDetails.builder()
                        .requested(postponementRequests.getPostponementReasonId() != null ? YES : NO)
                        .granted(Y.equals(postponementRequests.getPostponementGranted()) ? YES : NO)
                        .build();
                    DwpTimeExtension dwpTimeExtension = DwpTimeExtension.builder()
                        .value(dwpTimeExtensionDetails)
                        .build();
                    dwpTimeExtensionList.add(dwpTimeExtension);
                }
            );
        }

        return dwpTimeExtensionList;
    }

    private boolean isAdjourned(List<MajorStatus> majorStatusList) {
        return majorStatusList.stream().anyMatch(majorStatus -> "92".equals(majorStatus.getStatusId()));
    }

}
