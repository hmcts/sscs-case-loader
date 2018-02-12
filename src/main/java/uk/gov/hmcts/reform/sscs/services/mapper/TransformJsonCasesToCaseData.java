package uk.gov.hmcts.reform.sscs.services.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Gaps2Extract;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Address;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Appeal;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Appellant;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Contact;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.HearingOptions;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Identity;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Name;

@Service
public class TransformJsonCasesToCaseData {

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

        HearingOptions hearingOptions = getHearingOptions(appealCase);

        Appeal appeal = Appeal.builder()
            .appellant(appellant)
            .hearingOptions(hearingOptions)
            .build();

        return CaseData.builder()
            .caseReference(appealCase.getAppealCaseRefNum())
            .appeal(appeal)
            .build();
    }

    private Identity getIdentity(AppealCase appealCase) {
        return Identity.builder()
            .dob(getValidDoB(appealCase.getParties().getDob()))
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

    private HearingOptions getHearingOptions(AppealCase appealCase) {
        return HearingOptions.builder()
            .languageInterpreter(appealCase.getParties().getInterpreterSignerId())
            .build();
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

    private String getValidDoB(String dob) {
        return dob != null ? parseToIsoDateTime(dob) : "";
    }

    private String parseToIsoDateTime(String utcTime) {
        ZonedDateTime result = ZonedDateTime.parse(utcTime, DateTimeFormatter.ISO_DATE_TIME);
        return result.toLocalDate().toString();
    }

}

