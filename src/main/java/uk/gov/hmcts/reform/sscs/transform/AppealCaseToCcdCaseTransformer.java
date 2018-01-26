package uk.gov.hmcts.reform.sscs.transform;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.sscs.models.Appeal;
import uk.gov.hmcts.reform.sscs.models.Appellant;
import uk.gov.hmcts.reform.sscs.models.CcdCase;
import uk.gov.hmcts.reform.sscs.models.Identity;
import uk.gov.hmcts.reform.sscs.models.Name;

@Component
public class AppealCaseToCcdCaseTransformer {
    
    private JSONObject appealCase;
    private JSONObject parties;

    public CcdCase transform(JSONObject appealCase) {
        this.appealCase = appealCase;
        parties = appealCase.getJSONObject("Parties");
        
        Name name = getName();
        Identity identity = getIdentity();
        
        Appellant appellant = Appellant.builder().name(name).identity(identity).build();
        Appeal appeal = Appeal.builder().appellant(appellant).build();
        CcdCase ccdCase = CcdCase.builder().caseReference(valueOf(appealCase, "Appeal_Case_RefNum")).appeal(appeal)
                .build();

        return ccdCase;
    }

    private Identity getIdentity() {
        return Identity.builder().nino(valueOf(appealCase, "Appeal_Case_NINO")).build();
    }
    
    private Name getName() {
        return Name.builder().title(valueOf(parties, "Title")).firstName(valueOf(parties, "INITIALS"))
                .lastName(valueOf(parties, "Surname")).build();
    }
    
    private String valueOf(JSONObject jsonObject, String key) {
        return jsonObject.optString(key);
    }

}

