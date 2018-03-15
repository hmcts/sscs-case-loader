package uk.gov.hmcts.reform.sscs.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Gaps2Extract;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformAppealCaseToCaseData;

@Service
@Slf4j
public class TransformationService {


    private final LocalDate ignoreCasesBeforeDate;

    private final TransformAppealCaseToCaseData transformAppealCaseToCaseData;
    private ObjectMapper mapper;

    public TransformationService(TransformAppealCaseToCaseData transformAppealCaseToCaseData,
                                 @Value("${sscs.case.loader.ignoreCasesBeforeDate}") String ignoreDate) {
        this.transformAppealCaseToCaseData = transformAppealCaseToCaseData;
        this.ignoreCasesBeforeDate = LocalDate.parse(ignoreDate);
        mapper = Jackson2ObjectMapperBuilder.json().indentOutput(true).build();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public List<CaseData> transform(InputStream inputStream) {

        String xmlString;
        try {
            xmlString = IOUtils.toString(inputStream, Charset.defaultCharset());
        } catch (IOException e) {
            throw new TransformException("Error reading input into string", e);
        }
        JSONObject jsonObject = XML.toJSONObject(xmlString);

        Gaps2Extract result;
        try {
            result = mapper.readerFor(Gaps2Extract.class).readValue(jsonObject.toString());
        } catch (IOException e) {
            throw new TransformException("Error transforming string to object", e);
        }
        List<AppealCase> appealCases = result.getAppealCases().getAppealCaseList();
        return appealCases.stream()
            .filter(c -> c.getCreateDate() != null && ignoreCasesBeforeDate.isBefore(c.getCreateDate()))
            .map(transformAppealCaseToCaseData::transform)
            .collect(Collectors.toList());
    }
}
