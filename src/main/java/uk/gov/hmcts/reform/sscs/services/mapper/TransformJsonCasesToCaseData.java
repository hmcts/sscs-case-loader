package uk.gov.hmcts.reform.sscs.services.mapper;

import static uk.gov.hmcts.reform.sscs.models.GapsEvent.APPEAL_RECEIVED;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
        Name name = getName(appealCase);
        Address address = getAddress(appealCase);
        Contact contact = getContact(appealCase);
        Identity identity = getIdentity(appealCase);

        Appellant appellant = Appellant.builder()
            .name(name)
            .address(address)
            .contact(contact)
            .identity(identity)
            .build();

        BenefitType benefitType = getBenefitType(appealCase);

        HearingOptions hearingOptions = getHearingOptions(appealCase);

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
            .generatedNino(identity.getNino())
            .generatedSurname(name.getLastName())
            .generatedEmail(contact.getEmail())
            .generatedMobile(contact.getMobile())
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

    private Identity getIdentity(AppealCase appealCase) {
        return Identity.builder()
            .dob(getValidDate(appealCase.getParties().getDob()))
            .nino(appealCase.getAppealCaseNino())
            .build();
    }

    private Name getName(AppealCase appealCase) {
        Parties parties = appealCase.getParties();
        return Name.builder()
            .title(parties.getTitle())
            .firstName(parties.getInitials())
            .lastName(parties.getSurname())
            .build();
    }

    private Address getAddress(AppealCase appealCase) {
        return Address.builder()
            .postcode(appealCase.getParties().getPostCode())
            .build();
    }

    private Contact getContact(AppealCase appealCase) {
        return Contact.builder()
            .email(appealCase.getParties().getEmail())
            .phone(appealCase.getParties().getPhone1())
            .mobile(appealCase.getParties().getPhone2())
            .build();
    }

    private BenefitType getBenefitType(AppealCase appealCase) {
        return BenefitType.builder()
            .code(appealCase.getAppealCaseCaseCodeId())
            .build();
    }

    private HearingOptions getHearingOptions(AppealCase appealCase) {
        return HearingOptions.builder()
            .other(Y.equals(appealCase.getParties().getDisabilityNeeds()) ? YES : NO)
            .build();
    }

    private List<Hearing> getHearings(AppealCase appealCase) {
        List<Hearing> hearingsList = new ArrayList<>();
        HearingDetails hearings;

        if (appealCase.getHearing() != null) {
            VenueDetails venueDetails = referenceDataService.getVenueDetails(appealCase.getHearing().getVenueId());

            Venue venue = Venue.builder()
                .name(venueDetails.getVenName())
                .addressLine1(venueDetails.getVenAddressLine1())
                .addressLine2(venueDetails.getVenAddressLine2())
                .town(venueDetails.getVenAddressTown())
                .county(venueDetails.getVenAddressCounty())
                .postcode(venueDetails.getVenAddressPostcode())
                .googleMapLink(venueDetails.getUrl())
                .build();

            hearings = HearingDetails.builder()
                .venue(venue)
                .hearingDate(getValidDate(appealCase.getHearing().getSessionDate()))
                .time(getValidTime(appealCase.getHearing().getAppealTime()))
                .adjourned(isAdjourned(appealCase.getMajorStatus()) ? YES : NO)
                .build();

            Hearing value = Hearing.builder()
                .value(hearings)
                .build();

            hearingsList.add(value);
        }

        return hearingsList;
    }

    private Evidence getEvidence(AppealCase appealCase) {
        List<Documents> documentsList = new ArrayList<>();
        Doc doc;

        if (appealCase.getFurtherEvidence() != null) {
            doc = Doc.builder()
                .dateReceived(getValidDate(appealCase.getFurtherEvidence().getFeDateReceived()))
                .description(appealCase.getFurtherEvidence().getFeTypeofEvidenceId())
                .build();
            Documents documents = Documents.builder().value(doc).build();
            documentsList.add(documents);
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

