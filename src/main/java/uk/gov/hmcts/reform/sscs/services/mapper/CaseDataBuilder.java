package uk.gov.hmcts.reform.sscs.services.mapper;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.FurtherEvidence;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
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
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscription;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;
import uk.gov.hmcts.reform.sscs.services.date.DateHelper;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@Service
@Slf4j
public class CaseDataBuilder {

    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String Y = "Y";

    private final ReferenceDataService referenceDataService;

    @Autowired
    CaseDataBuilder(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    public List<Events> buildEvent(AppealCase appealCase) {
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

    public Identity buildIdentity(Parties party, AppealCase appealCase) {
        return Identity.builder()
            .dob(DateHelper.getValidDateOrTime(party.getDob(), true))
            .nino(appealCase.getAppealCaseNino())
            .build();
    }

    public Name buildName(Parties party) {
        return Name.builder()
            .title(party.getTitle())
            .firstName(party.getInitials())
            .lastName(party.getSurname())
            .build();
    }

    public Contact buildContact(Parties party) {
        return Contact.builder()
            .email(party.getEmail())
            .phone(party.getPhone1())
            .mobile(party.getPhone2())
            .build();
    }

    public BenefitType buildBenefitType(AppealCase appealCase) {
        String benefitType = referenceDataService.getBenefitType(appealCase.getAppealCaseCaseCodeId());
        return BenefitType.builder()
            .code(benefitType)
            .build();
    }

    public HearingOptions buildHearingOptions(Parties party) {
        return HearingOptions.builder()
            .other(Y.equals(party.getDisabilityNeeds()) ? YES : NO)
            .build();
    }

    public List<Hearing> buildHearings(AppealCase appealCase) {
        List<Hearing> hearingsList = new ArrayList<>();
        HearingDetails hearings;

        if (appealCase.getHearing() != null) {
            for (uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing hearing : appealCase.getHearing()) {

                VenueDetails venueDetails = referenceDataService.getVenueDetails(hearing.getVenueId());

                if (venueDetails != null) {
                    Address address = Address.builder()
                        .line1(venueDetails.getVenAddressLine1())
                        .line2(venueDetails.getVenAddressLine2())
                        .town(venueDetails.getVenAddressTown())
                        .county(venueDetails.getVenAddressCounty())
                        .postcode(venueDetails.getVenAddressPostcode())
                        .build();

                    Venue venue = Venue.builder()
                        .name(venueDetails.getVenName())
                        .address(address)
                        .googleMapLink(venueDetails.getUrl())
                        .build();

                    hearings = HearingDetails.builder()
                        .venue(venue)
                        .hearingDate(hearing.getSessionDate().withZoneSameInstant(
                            ZoneId.of("Europe/London")).toLocalDate().toString())
                        .time(DateHelper.getValidDateOrTime(hearing.getAppealTime(), false))
                        .adjourned(isAdjourned(appealCase.getMajorStatus()) ? YES : NO)
                        .build();

                    hearingsList.add(Hearing.builder().value(hearings).build());
                } else {
                    log.info("*** case-loader *** venue missing: " + appealCase.getHearing().get(0).getVenueId());
                    return Collections.emptyList();
                }
            }
        }

        return hearingsList;
    }

    public Evidence buildEvidence(AppealCase appealCase) {
        List<Documents> documentsList = new ArrayList<>();
        Doc doc;

        if (appealCase.getFurtherEvidence() != null) {
            for (FurtherEvidence furtherEvidence : appealCase.getFurtherEvidence()) {
                doc = Doc.builder()
                    .dateReceived(DateHelper.getValidDateOrTime(furtherEvidence.getFeDateReceived(), true))
                    .evidenceType(referenceDataService.getEvidenceType(furtherEvidence.getFeTypeofEvidenceId()))
                    .evidenceProvidedBy(referenceDataService.getRoleType(furtherEvidence.getFeRoleId()))
                    .build();
                Documents documents = Documents.builder().value(doc).build();
                documentsList.add(documents);
            }
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

    public Subscriptions buildSubscriptions() {
        Subscription appellantSubscription = Subscription.builder()
            .email("")
            .mobile("")
            .reason("")
            .subscribeEmail("")
            .subscribeSms("")
            .tya(generateAppealNumber())
            .build();
        Subscription supporterSubscription = Subscription.builder()
            .email("")
            .mobile("")
            .reason("")
            .subscribeEmail("")
            .subscribeSms("")
            .tya("")
            .build();
        return Subscriptions.builder()
            .appellantSubscription(appellantSubscription)
            .supporterSubscription(supporterSubscription)
            .build();
    }

    private String generateAppealNumber() {
        SecureRandom random = new SecureRandom();
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS).usingRandom(random::nextInt)
            .build();
        return generator.generate(10);
    }
}
