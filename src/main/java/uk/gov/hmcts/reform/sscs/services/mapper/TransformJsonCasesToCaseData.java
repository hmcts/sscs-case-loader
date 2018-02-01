package uk.gov.hmcts.reform.sscs.services.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Gaps2Extract;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Appeal;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Appellant;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Identity;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Name;



@Service
public class TransformJsonCasesToCaseData {

    public CaseData transform(String json) throws IOException {
        Gaps2Extract gaps2Extract = fromJsonToGapsExtract(json);
        return fromGaps2ExtractToCaseData(gaps2Extract.getAppealCases().getAppealCaseList().get(0));
    }

    private CaseData fromGaps2ExtractToCaseData(AppealCase appealCase) {
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
            .dob(getDob(appealCase))
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

    private Gaps2Extract fromJsonToGapsExtract(String json) throws IOException {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().indentOutput(true).build();
        return mapper.readerFor(Gaps2Extract.class).readValue(json);
    }

    private String getDob(AppealCase appealCase) {
        String dob = appealCase.getParties().getDob();
        return dob != null ? toIsoDate(dob) : "";
    }

    private String toIsoDate(String utcTime) {
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").parseDateTime(utcTime);
        return ISODateTimeFormat.date().print(dateTime);
    }

}

