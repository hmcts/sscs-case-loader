package uk.gov.hmcts.reform.sscs.services.mapper;

import static uk.gov.hmcts.reform.sscs.models.GapsEvent.APPEAL_RECEIVED;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Gaps2Extract;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;

@Service
@Slf4j
public class TransformJsonCasesToCaseData {

    private final TransformAppealCaseToCaseData transformAppealCaseToCaseData;

    @Autowired
    public TransformJsonCasesToCaseData(TransformAppealCaseToCaseData transformAppealCaseToCaseData) {
        this.transformAppealCaseToCaseData = transformAppealCaseToCaseData;
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
            .map(transformAppealCaseToCaseData::transform).collect(Collectors.toList());
    }

    private List<CaseData> findCasesToUpdate(List<AppealCase> appealCaseList) {
        return appealCaseList.stream()
            .filter(appealCase -> !isAwaitResponse(appealCase))
            .map(transformAppealCaseToCaseData::transform).collect(Collectors.toList());
    }

    private boolean isAwaitResponse(AppealCase appealCase) {
        return appealCase.getAppealCaseMajorId().equals(APPEAL_RECEIVED.getStatus());
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

}

