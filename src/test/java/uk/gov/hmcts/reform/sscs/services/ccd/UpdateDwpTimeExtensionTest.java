package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.DwpTimeExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.DwpTimeExtensionDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

public class UpdateDwpTimeExtensionTest {
    private final UpdateDwpTimeExtension updateDwpTimeExtension = new UpdateDwpTimeExtension();

    @Test
    public void givenDwpTimeExtensionChanged_shouldUpdateData() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .dwpTimeExtension(Collections.singletonList(DwpTimeExtension.builder()
                .value(DwpTimeExtensionDetails.builder()
                    .granted("yes")
                    .build())
                .build()))
            .events(Collections.emptyList())
            .build();

        SscsCaseDetails existingCaseDetails = SscsCaseDetails.builder()
            .data(SscsCaseData.builder()
                .events(Collections.emptyList())
                .build())
            .build();

        boolean updateDwp = updateDwpTimeExtension.updateDwpTimeExtension(gapsCaseData, existingCaseDetails.getData());

        assertTrue(updateDwp);
        assertThat(existingCaseDetails.getData().getDwpTimeExtension(), equalTo(gapsCaseData.getDwpTimeExtension()));
    }

}
