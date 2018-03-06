package uk.gov.hmcts.reform.sscs.services.mapper;

import static uk.gov.hmcts.reform.sscs.models.GapsEvent.APPEAL_RECEIVED;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.*;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.*;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Hearing;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@Service
@Slf4j
public class TransformJsonCasesToCaseData {

    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String Y = "Y";

    private final ReferenceDataService referenceDataService;

    @Autowired
    public TransformJsonCasesToCaseData(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    public List<CaseData> transformCreateCases(String json) {
        List<AppealCase> appealCases = fromJsonToGapsExtract(json).getAppealCases().getAppealCaseList();
        return findCasesToCreate(appealCases);
    }

    public List<CaseData> transformUpdateCases(String json) {
        List<AppealCase> appealCases = fromJsonToGapsExtract(json).getAppealCases().getAppealCaseList();
        return findCasesToUpdate(appealCases);
    }

    private List<CaseData> findCasesToCreate(List<AppealCase> appealCaseList) {
        return appealCaseList.stream()
            .filter(this::isAwaitResponse)
            .map(this::fromAppealCaseToCaseData).collect(Collectors.toList());
    }

    private List<CaseData> findCasesToUpdate(List<AppealCase> appealCaseList) {
        return appealCaseList.stream()
            .filter(appealCase -> !isAwaitResponse(appealCase))
            .map(this::fromAppealCaseToCaseData).collect(Collectors.toList());
    }

    private boolean isAwaitResponse(AppealCase appealCase) {
        return appealCase.getAppealCaseMajorId().equals(APPEAL_RECEIVED.getStatus());
    }

    private CaseData fromAppealCaseToCaseData(AppealCase appealCase) {
        List<Parties> parties = appealCase.getParties();
        Optional<Parties> party = parties.stream().filter(f -> f.getRoleId().intValue() == 4).findFirst();

        Name name = null;
        Contact contact = null;
        Identity identity = null;
        HearingOptions hearingOptions = null;
        String generatedNino = "";
        String generatedSurname = "";
        String generatedEmail = "";
        String generatedMobile = "";

        if (party.isPresent()) {
            name = getName(party.get());
            contact = getContact(party.get());
            identity = getIdentity(party.get(), appealCase);
            hearingOptions = getHearingOptions(party.get());
            generatedNino = identity.getNino();
            generatedSurname = name.getLastName();
            generatedEmail = contact.getEmail();
            generatedMobile = contact.getMobile();
        }

        Appellant appellant = Appellant.builder()
            .name(name)
            .contact(contact)
            .identity(identity)
            .build();

        BenefitType benefitType = getBenefitType(appealCase);

        Appeal appeal = Appeal.builder()
            .appellant(appellant)
            .benefitType(benefitType)
            .hearingOptions(hearingOptions)
            .build();

        List<Hearing> hearingsList = getHearings(appealCase);

        Evidence evidence = getEvidence(appealCase);

        List<DwpTimeExtension> dwpTimeExtensionList = getDwpTimeExtensions(appealCase);

        return CaseData.builder()
            .caseReference(appealCase.getAppealCaseRefNum())
            .appeal(appeal)
            .hearings(hearingsList)
            .evidence(evidence)
            .dwpTimeExtension(dwpTimeExtensionList)
            .events(buildEvent(appealCase))
            .generatedNino(generatedNino)
            .generatedSurname(generatedSurname)
            .generatedEmail(generatedEmail)
            .generatedMobile(generatedMobile)
            .build();
    }

    private List<Events> buildEvent(AppealCase appealCase) {

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
        Collections.sort(events, Collections.reverseOrder());
        return events;
    }

    private Identity getIdentity(Parties party, AppealCase appealCase) {
        return Identity.builder()
            .dob(getValidDate(party.getDob()))
            .nino(appealCase.getAppealCaseNino())
            .build();
    }

    private Name getName(Parties party) {
        return Name.builder()
            .title(party.getTitle())
            .firstName(party.getInitials())
            .lastName(party.getSurname())
            .build();
    }

    private Contact getContact(Parties party) {
        return Contact.builder()
            .email(party.getEmail())
            .phone(party.getPhone1())
            .mobile(party.getPhone2())
            .build();
    }

    private BenefitType getBenefitType(AppealCase appealCase) {
        return BenefitType.builder()
            .code(appealCase.getAppealCaseCaseCodeId())
            .build();
    }

    private HearingOptions getHearingOptions(Parties party) {
        return HearingOptions.builder()
            .other(Y.equals(party.getDisabilityNeeds()) ? YES : NO)
            .build();
    }

    private List<Hearing> getHearings(AppealCase appealCase) {
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
                        .time(getValidTime(hearing.getAppealTime()))
                        .adjourned(isAdjourned(appealCase.getMajorStatus()) ? YES : NO)
                        .build();

                    hearingsList.add(Hearing.builder().value(hearings).build());
                } else {
                    log.info("*** case-loader *** venue missing: " + appealCase.getHearing().get(0).getVenueId());
                    return null;
                }
            }
        }

        return hearingsList;
    }

    private Evidence getEvidence(AppealCase appealCase) {
        List<Documents> documentsList = new ArrayList<>();
        Doc doc;

        if (appealCase.getFurtherEvidence() != null) {
            for (FurtherEvidence furtherEvidence : appealCase.getFurtherEvidence()) {
                doc = Doc.builder()
                    .dateReceived(getValidDate(furtherEvidence.getFeDateReceived()))
                    .description(furtherEvidence.getFeTypeofEvidenceId())
                    .build();
                Documents documents = Documents.builder().value(doc).build();
                documentsList.add(documents);
            }
        }

        return Evidence.builder()
            .documents(documentsList)
            .build();
    }

    private List<DwpTimeExtension> getDwpTimeExtensions(AppealCase appealCase) {
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

    private Gaps2Extract fromJsonToGapsExtract(String json) {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().indentOutput(true).build();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try {
            return mapper.readerFor(Gaps2Extract.class).readValue(json);
        } catch (Exception e) {
            throw new TransformException("Oops...something went wrong...", e);
        }
    }

    private String getValidDate(String dateTime) {
        return dateTime != null ? parseToIsoDateTime(dateTime) : "";
    }

    private String parseToIsoDateTime(String utcTime) {
        ZonedDateTime result = ZonedDateTime.parse(utcTime, DateTimeFormatter.ISO_DATE_TIME);
        return result.toLocalDate().toString();
    }

    private String getValidTime(String dateTime) {
        return dateTime != null ? parseToIsoTime(dateTime) : "";
    }

    private String parseToIsoTime(String utcTime) {
        ZonedDateTime result = ZonedDateTime.parse(utcTime, DateTimeFormatter.ISO_DATE_TIME);
        return result.toLocalTime().toString();
    }
}

