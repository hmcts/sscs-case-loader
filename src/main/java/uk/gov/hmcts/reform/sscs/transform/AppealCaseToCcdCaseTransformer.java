package uk.gov.hmcts.reform.sscs.transform;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.sscs.models.Appeal;
import uk.gov.hmcts.reform.sscs.models.Appellant;
import uk.gov.hmcts.reform.sscs.models.CcdCase;
import uk.gov.hmcts.reform.sscs.models.Identity;
import uk.gov.hmcts.reform.sscs.models.Name;
import uk.gov.hmcts.reform.sscs.models.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.gaps2.Parties;

@Component
public class AppealCaseToCcdCaseTransformer {
    
    private AppealCase appealCase;

    public CcdCase transform(AppealCase appealCase) {
        this.appealCase = appealCase;

        Name name = getName();
        Identity identity = getIdentity();

        Appellant appellant = Appellant.builder().name(name).identity(identity).build();
        Appeal appeal = Appeal.builder().appellant(appellant).build();
        return CcdCase.builder()
                .caseReference(appealCase.getAppealCaseRefNum())
                .appeal(appeal)
                .build();
    }

    private Identity getIdentity() {
        return Identity.builder()
                .nino(appealCase.getAppealCaseNino())
                .build();
    }
    
    private Name getName() {
        Parties parties = appealCase.getParties();
        return Name.builder()
                .title(parties.getTitle())
                .firstName(parties.getInitials())
                .lastName(parties.getSurname())
                .build();
    }

}

