package uk.gov.hmcts.reform.sscs.transform;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import uk.gov.hmcts.reform.sscs.models.Appeal;
import uk.gov.hmcts.reform.sscs.models.Appellant;
import uk.gov.hmcts.reform.sscs.models.CcdCase;
import uk.gov.hmcts.reform.sscs.models.Identity;
import uk.gov.hmcts.reform.sscs.models.Name;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import static org.junit.Assert.assertEquals;

public class AppealCaseToCcdCaseTransformerTest {
    
    private static final String DELTA_JSON = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.json";
    
    @Test
    public void shouldTransformAppealCase() {
        JSONObject rootObject = new JSONObject(FileUtils.getFileContentGivenFilePath(DELTA_JSON));
        JSONObject appealCasesObject = rootObject.getJSONObject("Appeal_Cases");
        JSONArray appealCasesArray = appealCasesObject.getJSONArray("Appeal_Case");
        
        CcdCase actualCcdCase = new AppealCaseToCcdCaseTransformer().transform(appealCasesArray.getJSONObject(0));
        
        Name name = Name.builder().title("Mrs.").firstName("E").lastName("Elderberry").build();
        Identity identity = Identity.builder().nino("AB 22 55 66 B").build(); 
        Appellant appellant = Appellant.builder().name(name).identity(identity).build();
        Appeal appeal = Appeal.builder().appellant(appellant).build();
        CcdCase expectedCcdCase = CcdCase.builder().caseReference("SC068/17/00013").appeal(appeal).build();
        
        assertEquals(expectedCcdCase, actualCcdCase);
    }

}

