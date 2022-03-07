package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.*;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;

@RunWith(JUnitParamsRunner.class)
public class UpdateCcdHearingOptionsTest {
    @Test
    @Parameters(method = "generateHearingOptionUpdateScenarios")
    public void givenGapsWantsToAttendChange_shouldUpdateExistingCcdHearingOptions(SscsCaseData gapsCaseData,
                                                                                   SscsCaseData existingCcdCaseData,
                                                                                   boolean expectedUpdateData,
                                                                                   YesNo expectedWantsToAttend) {
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
                    .wantsToAttend(YES)
                    .build())
                .build())
            .build();

        SscsCaseData existingCcdCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(NO)
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
                    .wantsToAttend(isYesOrNo(""))
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
                    .wantsToAttend(isYesOrNo(""))
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
            new Object[]{sscsCaseDataWithNullHearingOptions, existingCcdCaseData, false, NO},
            new Object[]{sscsCaseDataWithAnotherNullHearingOptions, existingCcdCaseData, false, NO},
            new Object[]{sscsCaseDataWithNullWantsToAttend, existingCcdCaseData, false, NO},
            new Object[]{sscsCaseDataWithEmptyWantsToAttend, existingCcdCaseData, false, NO},
            new Object[]{sscsCaseDataWithEmptyHearingOptions, existingCcdCaseData, false, NO},
            new Object[]{gapsCaseData, existingCcdCaseData, true, YES},
            new Object[]{gapsCaseData, existingCcdCaseDataWithNullHearingOptions, true, YES},
            new Object[]{gapsCaseData, existingCcdCaseDataWithAnotherNullHearingOptions, true, YES},
            new Object[]{gapsCaseData, existingCcdCaseDataWithEmptyHearingOptions, true, YES},
            new Object[]{gapsCaseData, existingCcdCaseDataWithEmptyWantsToAttend, true, YES},
            new Object[]{gapsCaseData, existingCcdCaseDataWithNullWantsToAttend, true, YES}
        };
    }
}
