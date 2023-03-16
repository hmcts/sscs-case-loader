package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

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

        SscsCaseData sscsCaseDataWithNullHearingOptions = SscsCaseData.builder()
            .appeal(Appeal.builder().build())
            .build();

        SscsCaseData sscsCaseDataWithAnotherNullHearingOptions = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(null)
                .build())
            .build();

        SscsCaseData sscsCaseDataWithEmptyHearingOptions = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().build())
                .build())
            .build();

        SscsCaseData sscsCaseDataWithNullWantsToAttend = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(null)
                    .build())
                .build())
            .build();

        SscsCaseData sscsCaseDataWithEmptyWantsToAttend = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("")
                    .build())
                .build())
            .build();

        SscsCaseData existingCcdCaseDataWithNullHearingOptions = SscsCaseData.builder()
            .appeal(Appeal.builder().build())
            .build();

        SscsCaseData existingCcdCaseDataWithAnotherNullHearingOptions = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(null)
                .build())
            .build();

        SscsCaseData existingCcdCaseDataWithEmptyHearingOptions = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().build())
                .build())
            .build();

        SscsCaseData existingCcdCaseDataWithEmptyWantsToAttend = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("")
                    .build())
                .build())
            .build();

        SscsCaseData existingCcdCaseDataWithNullWantsToAttend = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(null)
                    .build())
                .build())
            .build();

        return new Object[]{
            new Object[]{sscsCaseDataWithNullHearingOptions, existingCcdCaseData, false, "no"},
            new Object[]{sscsCaseDataWithAnotherNullHearingOptions, existingCcdCaseData, false, "no"},
            new Object[]{sscsCaseDataWithNullWantsToAttend, existingCcdCaseData, false, "no"},
            new Object[]{sscsCaseDataWithEmptyWantsToAttend, existingCcdCaseData, false, "no"},
            new Object[]{sscsCaseDataWithEmptyHearingOptions, existingCcdCaseData, false, "no"},
            new Object[]{gapsCaseData, existingCcdCaseData, true, "yes"},
            new Object[]{gapsCaseData, existingCcdCaseDataWithNullHearingOptions, true, "yes"},
            new Object[]{gapsCaseData, existingCcdCaseDataWithAnotherNullHearingOptions, true, "yes"},
            new Object[]{gapsCaseData, existingCcdCaseDataWithEmptyHearingOptions, true, "yes"},
            new Object[]{gapsCaseData, existingCcdCaseDataWithEmptyWantsToAttend, true, "yes"},
            new Object[]{gapsCaseData, existingCcdCaseDataWithNullWantsToAttend, true, "yes"}
        };
    }
}
