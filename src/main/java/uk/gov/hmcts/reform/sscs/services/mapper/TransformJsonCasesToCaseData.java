package uk.gov.hmcts.reform.sscs.services.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Gaps2Extract;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Address;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Appeal;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Appellant;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.BenefitType;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Contact;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Doc;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Documents;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.DwpTimeExtension;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.DwpTimeExtensionDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Evidence;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Hearing;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.HearingDetails;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.HearingOptions;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Identity;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Name;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Venue;

@Service
public class TransformJsonCasesToCaseData {

    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String Y = "Y";

    public List<CaseData> transform(String json) {
        Gaps2Extract gaps2Extract = fromJsonToGapsExtract(json);
        return fromGaps2ExtractToCaseDataList(gaps2Extract.getAppealCases().getAppealCaseList());
    }

    private List<CaseData> fromGaps2ExtractToCaseDataList(List<AppealCase> appealCaseList) {
        return appealCaseList.stream().map(this::fromAppealCaseToCaseData).collect(Collectors.toList());
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
            .build();
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
            hearings = HearingDetails.builder()
                .venue(Venue.builder().venueTown("Aberdeen").build())
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

