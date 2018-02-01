package uk.gov.hmcts.reform.sscs;

import uk.gov.hmcts.reform.sscs.models.serialize.ccd.*;

public final class CaseDataUtils {
    private CaseDataUtils() {
    }

    public static CaseData buildCaseData() {
        Name name = Name.builder()
            .title("Mr")
            .firstName("User")
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
            .caseReference("SC068/17/00013")
            .appeal(appeal).build();
    }
}
