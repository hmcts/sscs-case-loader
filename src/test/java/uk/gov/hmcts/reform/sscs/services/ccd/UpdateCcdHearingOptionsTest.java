package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@RunWith(JUnitParamsRunner.class)
public class UpdateCcdHearingOptionsTest {
    @Test
    @Parameters(method = "generateHearingOptionUpdateScenarios")
    public void givenGapsWantsToAttendChange_shouldUpdateExistingCcdHearingOptions(SscsCaseData gapsCaseData,
                                                                                   SscsCaseData existingCcdCaseData,
                                                                                   boolean expectedUpdateData,
                                                                                   String expectedWantsToAttend) {
        UpdateCcdHearingOptions updateCcdHearingOptions = new UpdateCcdHearingOptions();

        boolean updateData = updateCcdHearingOptions.updateHearingOptions(gapsCaseData, existingCcdCaseData);

        assertEquals(expectedUpdateData, updateData);
        assertThat(existingCcdCaseData.getAppeal().getHearingOptions().getWantsToAttend(), is(expectedWantsToAttend));
    }


    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateHearingOptionUpdateScenarios() {
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

        return new Object[]{
            new Object[]{gapsCaseData, existingCcdCaseData, true, "yes"}
        };
    }
}
