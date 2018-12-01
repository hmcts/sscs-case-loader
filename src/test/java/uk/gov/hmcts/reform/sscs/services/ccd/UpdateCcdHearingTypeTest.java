package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingType("oral")
                .build())
            .build();

        SscsCaseData existingCcdCase = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingType("paper")
                .build())
            .build();

        return new Object[]{
            new Object[]{gapsCaseData, existingCcdCase, true, "oral"}
        };
    }

}
