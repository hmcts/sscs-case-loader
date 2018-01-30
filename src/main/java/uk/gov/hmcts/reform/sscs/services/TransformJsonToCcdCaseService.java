package uk.gov.hmcts.reform.sscs.services;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.sscs.models.CcdCase;
import uk.gov.hmcts.reform.sscs.processor.DeltaFileProcessor;

@Service
public class TransformJsonToCcdCaseService {
    
    @Autowired
    private DeltaFileProcessor deltaFileProcessor;
    
    public CcdCase process(JSONObject json) {
        return deltaFileProcessor.process(json);
    }
}

