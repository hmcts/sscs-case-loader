package uk.gov.hmcts.reform.sscs.services.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
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
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.*;

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
        Identity identity = getIdentity(appealCase);

        Appellant appellant = Appellant.builder().name(name).identity(identity).build();
        Appeal appeal = Appeal.builder().appellant(appellant).build();
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

    private Gaps2Extract fromJsonToGapsExtract(String json) {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().indentOutput(true).build();
        try {
            return mapper.readerFor(Gaps2Extract.class).readValue(json);
        } catch (IOException e) {
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

