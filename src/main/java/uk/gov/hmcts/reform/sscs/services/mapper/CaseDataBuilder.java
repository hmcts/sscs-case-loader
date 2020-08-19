package uk.gov.hmcts.reform.sscs.services.mapper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Address;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitType;
import uk.gov.hmcts.reform.sscs.ccd.domain.Contact;
import uk.gov.hmcts.reform.sscs.ccd.domain.Document;
import uk.gov.hmcts.reform.sscs.ccd.domain.DocumentDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.DwpTimeExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.DwpTimeExtensionDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Event;
import uk.gov.hmcts.reform.sscs.ccd.domain.Evidence;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.Identity;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.Venue;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.FurtherEvidence;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;
import uk.gov.hmcts.reform.sscs.service.RegionalProcessingCenterService;
import uk.gov.hmcts.reform.sscs.services.date.DateHelper;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;
import uk.gov.hmcts.reform.sscs.util.UkMobile;

@Service
@Slf4j
class CaseDataBuilder {

    private static final String YES = "Yes";
    static final String NO = "No";
    private static final String DISABILITY_NEEDS = "Y";
    private static final String POSTPONEMENT_GRANTED = "Y";

    private final ReferenceDataService referenceDataService;
    private final CaseDataEventBuilder caseDataEventBuilder;
    private final RegionalProcessingCenterService regionalProcessingCenterService;

    @Autowired
    CaseDataBuilder(ReferenceDataService referenceDataService,
                    CaseDataEventBuilder caseDataEventBuilder,
                    RegionalProcessingCenterService regionalProcessingCenterService
    ) {
        this.referenceDataService = referenceDataService;
        this.caseDataEventBuilder = caseDataEventBuilder;
        this.regionalProcessingCenterService = regionalProcessingCenterService;
    }

    List<Event> buildEvent(AppealCase appealCase) {
        List<Event> events = caseDataEventBuilder.buildMajorStatusEvents(appealCase);
        events.addAll(caseDataEventBuilder.buildPostponedEvent(appealCase));
        events.addAll(caseDataEventBuilder.buildAdjournedEvents(appealCase));
        events.sort(Collections.reverseOrder());
        return events;
    }

    Identity buildIdentity(Parties party, AppealCase appealCase) {
        return Identity.builder()
            .dob(DateHelper.getValidDateOrTime(party.getDob(), true))
            .nino(appealCase.getAppealCaseNino())
            .build();
    }

    Name buildName(Parties party) {
        return Name.builder()
            .title(party.getTitle())
            .firstName(party.getInitials())
            .lastName(party.getSurname())
            .build();
    }

    Contact buildContact(Parties party) {
        return Contact.builder()
            .email(party.getEmail())
            .phone(party.getLandline())
            .mobile(party.getMobile())
            .build();
    }

    BenefitType buildBenefitType(AppealCase appealCase) {
        String benefitType = referenceDataService.getBenefitType(appealCase.getAppealCaseCaseCodeId());
        return BenefitType.builder()
            .code(benefitType)
            .build();
    }

    HearingOptions buildHearingOptions(Parties party, String tribunalsTypeId) {

        String wantsToAttend = null;

        if (null != tribunalsTypeId
            && (tribunalsTypeId.equals("1")
            || tribunalsTypeId.equals("2")
            || tribunalsTypeId.equals("3"))) {
            wantsToAttend = getWantsToAttend(tribunalsTypeId);
        }

        return HearingOptions.builder()
            .other(DISABILITY_NEEDS.equals(party.getDisabilityNeeds()) ? YES : NO)
            .wantsToAttend(wantsToAttend)
            .build();

    }

    private String getWantsToAttend(String tribunalsTypeId) {
        String tbtCode = referenceDataService.getTbtCode(tribunalsTypeId);
        if ("O".equals(tbtCode)) {
            return YES;
        }
        return NO;
    }

    RegionalProcessingCenter buildRegionalProcessingCentre(AppealCase appealCase, Parties appellantParty) {
        List<Hearing> hearings = appealCase.getHearing() != null ? appealCase.getHearing() : Collections.emptyList();

        if (hearings.size() > 0) {
            log.info("Building RPC for Gaps case data based on last hearing venue for case id {}",
                appealCase.getAppealCaseId());
        } else {
            log.info("Building RPC for Gaps case data based on postcode for case id {}", appealCase.getAppealCaseId());
        }

        return hearings.stream()
            .reduce(getLast())
            .map(hearing -> regionalProcessingCenterService.getByVenueId(hearing.getVenueId()))
            .orElse(regionalProcessingCenterService.getByPostcode(appellantParty.getPostCode()));
    }

    private <T> BinaryOperator<T> getLast() {
        return (first, second) -> second;
    }

    List<uk.gov.hmcts.reform.sscs.ccd.domain.Hearing> buildHearings(AppealCase appealCase) {
        List<uk.gov.hmcts.reform.sscs.ccd.domain.Hearing> hearingsList = new ArrayList<>();
        HearingDetails hearings;

        if (appealCase.getHearing() != null) {
            for (Hearing hearing : appealCase.getHearing()) {

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

                    String appealTime = hearing.getAppealTime();
                    String activeInActive = getActiveInActiveVenueInfo(venueDetails);
                    log.info("Hearing booked for case {} on {} at {} venue {}",
                        appealCase.getAdditionalRef(), hearing.getSessionDate(), activeInActive,
                        venueDetails.getVenueId());
                    hearings = HearingDetails.builder()
                        .venue(venue)
                        .hearingDate(hearing.getSessionDate().substring(0, 10))
                        .time((appealTime == null) ? "00:00:00" : appealTime.substring(11, 19))
                        .adjourned(isAdjourned(appealCase.getMajorStatus(), hearing) ? YES : NO)
                        .hearingId(hearing.getHearingId())
                        .build();

                    hearingsList.add(uk.gov.hmcts.reform.sscs.ccd.domain.Hearing.builder().value(hearings).build());
                } else {
                    log.info("*** case-loader *** venue missing: {} for CCD ID {} / SC number {} "
                            + "and hearing session date {}",
                        appealCase.getHearing().get(0).getVenueId(),
                        appealCase.getAppealCaseId(),
                        appealCase.getAppealCaseRefNum(),
                        hearing.getSessionDate());
                }
            }
        }

        return hearingsList;
    }

    private String getActiveInActiveVenueInfo(VenueDetails venueDetails) {
        return "Yes".equalsIgnoreCase(venueDetails.getActive()) ? "active" : "inactive";
    }

    Evidence buildEvidence(AppealCase appealCase) {
        List<Document> documentsList = new ArrayList<>();
        DocumentDetails doc;

        if (appealCase.getFurtherEvidence() != null) {
            for (FurtherEvidence furtherEvidence : appealCase.getFurtherEvidence()) {
                doc = DocumentDetails.builder()
                    .dateReceived(DateHelper.getValidDateOrTime(furtherEvidence.getFeDateReceived(), true))
                    .evidenceType(referenceDataService.getEvidenceType(furtherEvidence.getFeTypeofEvidenceId()))
                    .evidenceProvidedBy(referenceDataService.getRoleType(furtherEvidence.getFeRoleId()))
                    .build();
                Document documents = Document.builder().value(doc).build();
                documentsList.add(documents);
            }
        }

        return Evidence.builder()
            .documents(documentsList)
            .build();
    }

    List<DwpTimeExtension> buildDwpTimeExtensions(AppealCase appealCase) {
        List<DwpTimeExtension> dwpTimeExtensionList = new ArrayList<>();
        List<PostponementRequests> postponementRequestsList = appealCase.getPostponementRequests();
        if (postponementRequestsList != null) {
            appealCase.getPostponementRequests().forEach(
                postponementRequests -> {
                    DwpTimeExtensionDetails dwpTimeExtensionDetails = DwpTimeExtensionDetails.builder()
                        .requested(postponementRequests.getPostponementReasonId() != null ? YES : NO)
                        .granted(POSTPONEMENT_GRANTED.equals(postponementRequests.getPostponementGranted()) ? YES : NO)
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

    private boolean isAdjourned(List<MajorStatus> majorStatusList, Hearing hearing) {
        return majorStatusList.stream().anyMatch(majorStatus -> "92".equals(majorStatus.getStatusId()))
            || isAdjournedBasedOnOutcomeId(hearing);
    }

    private boolean isAdjournedBasedOnOutcomeId(Hearing hearing) {
        if (null == hearing || StringUtils.isBlank(hearing.getOutcomeId())) {
            return false;
        }
        int outcomeIdAsNumber = getOutcomeIdAsNumber(hearing.getOutcomeId());
        return outcomeIdAsNumber >= 110 && outcomeIdAsNumber <= 126;
    }

    private int getOutcomeIdAsNumber(String outcomeId) {
        try {
            return Integer.parseInt(outcomeId);
        } catch (NumberFormatException e) {
            String msg = String.format("Wrong outcomeId(%s) format was found. Expected format is a number.", outcomeId);
            throw new NumberFormatException(msg);
        }
    }

    Subscriptions buildSubscriptions(final Optional<Parties> appellantParty,
                                     final Optional<Parties> representativeParty,
                                     final Optional<Parties> appointeeParty,
                                     String appealCaseRefNum) {
        Subscription appellantSubscription = buildSubscriptionWithDefaults(
            appellantParty,
            appealCaseRefNum,
            generateAppealNumber()
        );

        Subscription representativeSubscription = buildSubscriptionWithDefaults(
            representativeParty,
            appealCaseRefNum,
            representativeParty.isPresent() ? generateAppealNumber() : StringUtils.EMPTY
        );

        Subscription appointeeSubscription = buildSubscriptionWithDefaults(
            appointeeParty,
            appealCaseRefNum,
            appointeeParty.isPresent() ? generateAppealNumber() : StringUtils.EMPTY
        );

        return Subscriptions.builder()
            .appellantSubscription(appellantSubscription)
            .representativeSubscription(representativeSubscription)
            .appointeeSubscription(appointeeSubscription)
            .build();
    }

    protected static Subscription buildSubscriptionWithDefaults(Optional<Parties> party, String appealCaseRefNum,
                                                                String appealNumber) {
        return Subscription.builder()
            .email(party.map(Parties::getEmail).orElse(StringUtils.EMPTY))
            .mobile(validateMobile(party, appealCaseRefNum))
            .reason(StringUtils.EMPTY)
            .subscribeEmail(NO)
            .subscribeSms(NO)
            .tya(appealNumber)
            .build();
    }

    private static String validateMobile(Optional<Parties> party, String appealCaseRefNum) {
        if (party.isPresent() && StringUtils.isNotBlank(party.get().getMobile())) {
            String mobileNumber = party.get().getMobile();
            if (UkMobile.validate(mobileNumber)) {
                return mobileNumber;
            } else {
                log.info("Invalid Uk mobile no: {} In party Contact Details for the case reference: {}",
                    mobileNumber, appealCaseRefNum);
            }
        }
        return StringUtils.EMPTY;
    }

    private static String generateAppealNumber() {
        SecureRandom random = new SecureRandom();
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS).usingRandom(random::nextInt)
            .build();
        return generator.generate(10);
    }
}
