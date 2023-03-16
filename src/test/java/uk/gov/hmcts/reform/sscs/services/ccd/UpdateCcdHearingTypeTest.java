package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingType.*;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@RunWith(JUnitParamsRunner.class)
public class UpdateCcdHearingTypeTest {
    @Test
    @Parameters(method = "generateHearingTypeUpdateScenarios")
    public void givenGapsHearingTypeUpdate_shouldUpdateExistingCcdCase(SscsCaseData gapsCaseData,
                                                                       SscsCaseData existingCcdCase,
                                                                       boolean expectedUpdateData,
                                                                       String expectedExistingHearingType) {

        UpdateCcdHearingType updateCcdHearingType = new UpdateCcdHearingType();
        boolean updateData = updateCcdHearingType.updateHearingType(gapsCaseData, existingCcdCase);

        assertEquals(expectedUpdateData, updateData);
        assertThat(existingCcdCase.getAppeal().getHearingType(), is(expectedExistingHearingType));
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateHearingTypeUpdateScenarios() {
        SscsCaseData gapsCaseDataOralHearingType = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingType(ORAL.getValue())
                .build())
            .build();

        SscsCaseData gapsCaseDataDomiciliaryHearingType = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingType(DOMICILIARY.getValue())
                .build())
            .build();

        SscsCaseData gapsCaseDataWithNullHearingType = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .build())
            .build();

        SscsCaseData gapsCaseDataWithMoreEmptyHearingType = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingType("")
                .build())
            .build();

        SscsCaseData gapsCaseDataWithCorHearingType = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingType(PAPER.getValue())
                .build())
            .build();

        SscsCaseData existingCcdCase = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingType(PAPER.getValue())
                .build())
            .build();

        SscsCaseData existingCcdCaseWithNullHearingType = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .build())
            .build();

        SscsCaseData existingCcdCaseWithEmptyHearingType = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingType("")
                .build())
            .build();

        SscsCaseData existingCcdCaseCorHearingType = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingType(COR.getValue())
                .build())
            .build();

        return new Object[]{
            new Object[]{gapsCaseDataWithNullHearingType, existingCcdCase, false, "paper"},
            new Object[]{gapsCaseDataWithMoreEmptyHearingType, existingCcdCase, false, "paper"},
            // SSCS-7604 - Do not update hearing type to Domiciliary
            new Object[]{gapsCaseDataDomiciliaryHearingType, existingCcdCase, false, "paper"},
            new Object[]{gapsCaseDataOralHearingType, existingCcdCase, true, "oral"},
            new Object[]{gapsCaseDataOralHearingType, existingCcdCaseWithNullHearingType, true, "oral"},
            new Object[]{gapsCaseDataOralHearingType, existingCcdCaseWithEmptyHearingType, true, "oral"},
            new Object[]{gapsCaseDataWithCorHearingType, existingCcdCaseCorHearingType, false, "cor"}
        };
    }

}
