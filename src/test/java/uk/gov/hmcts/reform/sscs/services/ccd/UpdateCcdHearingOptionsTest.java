package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

public class UpdateCcdHearingOptionsTest {
    @Test
    public void givenGapsWantsToAttendChange_shouldUpdateExistingCcdHearingOptions() {
        UpdateCcdHearingOptions updateCcdHearingOptions = new UpdateCcdHearingOptions();

        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("yes")
                    .build())
                .build())
            .build();

        SscsCaseData existingCcdCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("no")
                    .build())
                .build())
            .build();

        boolean updateData = updateCcdHearingOptions.updateHearingOptions(gapsCaseData, existingCcdCaseData);

        assertTrue(updateData);
        assertThat(existingCcdCaseData.getAppeal().getHearingOptions().getWantsToAttend(), is("yes"));
    }
}
