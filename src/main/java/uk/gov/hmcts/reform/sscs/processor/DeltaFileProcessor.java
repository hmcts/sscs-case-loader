package uk.gov.hmcts.reform.sscs.processor;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.sscs.models.CcdCase;
import uk.gov.hmcts.reform.sscs.transform.AppealCaseToCcdCaseTransformer;

@Component
public class DeltaFileProcessor {

    private  AppealCaseToCcdCaseTransformer appealCaseToCcdCaseTransformer;
    
    @Autowired
    public DeltaFileProcessor(AppealCaseToCcdCaseTransformer appealCaseToCcdCaseTransformer) {
        this.appealCaseToCcdCaseTransformer = appealCaseToCcdCaseTransformer;
    }
    
    public CcdCase process(JSONObject json) {
        JSONObject rootObject = json.getJSONObject("Appeal_Cases");
        JSONArray appealCases = rootObject.getJSONArray("Appeal_Case");
        CcdCase ccdCase = appealCaseToCcdCaseTransformer.transform(appealCases.getJSONObject(0));
        return ccdCase;
    }


}

