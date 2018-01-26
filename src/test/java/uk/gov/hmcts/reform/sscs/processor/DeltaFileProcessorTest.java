package uk.gov.hmcts.reform.sscs.processor;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.hmcts.reform.sscs.transform.AppealCaseToCcdCaseTransformer;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeltaFileProcessorTest {
    
    private static final String DELTA_JSON = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json";
    
    private DeltaFileProcessor deltaFileProcessor;
    
    @Mock
    private  AppealCaseToCcdCaseTransformer appealCaseToCcdCaseTransformer;
    
    @Before
    public void setUp() {
        deltaFileProcessor = new DeltaFileProcessor(appealCaseToCcdCaseTransformer);
    }
    
    @Test
    public void shouldProcess() {
        
        JSONObject rootObject = new JSONObject(FileUtils.getFileContentGivenFilePath(DELTA_JSON));
        JSONObject appealCasesObject = rootObject.getJSONObject("Appeal_Cases");
        JSONArray appealCasesArray = appealCasesObject.getJSONArray("Appeal_Case");
        
        deltaFileProcessor.process(rootObject);
        
        verify(appealCaseToCcdCaseTransformer).transform(eq(appealCasesArray.getJSONObject(0)));

    }


}

