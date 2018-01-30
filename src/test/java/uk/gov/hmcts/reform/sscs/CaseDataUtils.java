package uk.gov.hmcts.reform.sscs;

import uk.gov.hmcts.reform.sscs.models.Appeal;
import uk.gov.hmcts.reform.sscs.models.Appellant;
import uk.gov.hmcts.reform.sscs.models.CaseData;
import uk.gov.hmcts.reform.sscs.models.Identity;
import uk.gov.hmcts.reform.sscs.models.Name;

public final class CaseDataUtils {
    private CaseDataUtils() {
    }

    public static CaseData buildCaseData() {
        Name name = Name.builder()
            .title("Mr")
            .firstName("User")
            .middleName("M")
            .lastName("Test")
            .build();
        Identity identity = Identity.builder()
            .dob("01-04-1985")
            .nino("AB 22 55 66 B")
            .build();
        Appellant appellant = Appellant.builder()
            .name(name)
            .identity(identity)
            .build();
        Appeal appeal = Appeal.builder()
            .mrnDate("2017-10-08")
            .mrnMissingReason("It was missing")
            .appellant(appellant)
            .build();
        return CaseData.builder()
            .appeal(appeal).build();
    }
}
