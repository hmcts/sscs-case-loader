package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.DwpTimeExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.DwpTimeExtensionDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@RunWith(JUnitParamsRunner.class)
public class UpdateDwpTimeExtensionTest {
    private final UpdateDwpTimeExtension updateDwpTimeExtension = new UpdateDwpTimeExtension();

    @Test
    @Parameters(method = "generateDwpTimeExtensionUpdateScenarios")
    public void givenDwpTimeExtensionChanged_shouldUpdateData(SscsCaseData gapsCaseData,
                                                              SscsCaseData existingCaseData,
                                                              boolean expectedUpdateDwp) {
        boolean updateDwp = updateDwpTimeExtension.updateDwpTimeExtension(gapsCaseData, existingCaseData);

        assertEquals(expectedUpdateDwp, updateDwp);
        if (expectedUpdateDwp) {
            assertThat(existingCaseData.getDwpTimeExtension(), equalTo(gapsCaseData.getDwpTimeExtension()));
        }
    }


    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateDwpTimeExtensionUpdateScenarios() {
        SscsCaseData gapsCaseDataWithDwpTimeExtension = SscsCaseData.builder()
            .dwpTimeExtension(Collections.singletonList(DwpTimeExtension.builder()
                .value(DwpTimeExtensionDetails.builder()
                    .granted("yes")
                    .build())
                .build()))
            .build();

        SscsCaseData gapsCaseDataWithNoDwpTimeExtension = SscsCaseData.builder()
            .dwpTimeExtension(null)
            .build();

        SscsCaseData gapsCaseDataWithEmptyDwpTimeExtension = SscsCaseData.builder()
            .dwpTimeExtension(Collections.emptyList())
            .build();

        SscsCaseData existingCaseDetails = SscsCaseData.builder().build();

        return new Object[]{
            new Object[]{gapsCaseDataWithNoDwpTimeExtension, existingCaseDetails, false},
            new Object[]{gapsCaseDataWithEmptyDwpTimeExtension, existingCaseDetails, false},
            new Object[]{gapsCaseDataWithDwpTimeExtension, existingCaseDetails, true}
        };
    }

}
