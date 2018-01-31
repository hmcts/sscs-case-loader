package uk.gov.hmcts.reform.sscs.processor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.sscs.models.CcdCase;
import uk.gov.hmcts.reform.sscs.models.gaps2.Gaps2Extract;
import uk.gov.hmcts.reform.sscs.transform.AppealCaseToCcdCaseTransformer;

import java.io.IOException;

@Component
public class DeltaFileProcessor {

    private final AppealCaseToCcdCaseTransformer appealCaseToCcdCaseTransformer;
    
    @Autowired
    public DeltaFileProcessor(AppealCaseToCcdCaseTransformer appealCaseToCcdCaseTransformer) {
        this.appealCaseToCcdCaseTransformer = appealCaseToCcdCaseTransformer;
    }
    
    public CcdCase process(String json) throws IOException {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().indentOutput(true).build();
        Gaps2Extract gapsExtract = mapper.readerFor(Gaps2Extract.class).readValue(json);
        
        return appealCaseToCcdCaseTransformer.transform(gapsExtract.getAppealCases().getAppealCaseList().get(0));
    }


}

