package uk.gov.hmcts.reform.sscs;

import uk.gov.hmcts.reform.sscs.models.Appeal;
import uk.gov.hmcts.reform.sscs.models.Appellant;
import uk.gov.hmcts.reform.sscs.models.Name;

public final class AppealUtils {
    private AppealUtils() {
    }

    public static Appeal buildAppeal() {
        Appellant appellant = new Appellant(new Name("Mr", "User", "Test"));
        return Appeal.builder()
            .mrnDate("2017-10-08")
            .mrnMissingReason("It was missing")
            .appellant(appellant)
            .build();
    }
}
