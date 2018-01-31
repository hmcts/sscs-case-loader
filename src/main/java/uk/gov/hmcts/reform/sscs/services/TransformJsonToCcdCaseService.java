package uk.gov.hmcts.reform.sscs.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.sscs.models.CaseData;
import uk.gov.hmcts.reform.sscs.processor.DeltaFileProcessor;

import java.io.IOException;

@Service
public class TransformJsonToCcdCaseService {
    
    @Autowired
    private DeltaFileProcessor deltaFileProcessor;
    
    public CaseData process(String json) throws IOException {
        return deltaFileProcessor.process(json);
    }
}

