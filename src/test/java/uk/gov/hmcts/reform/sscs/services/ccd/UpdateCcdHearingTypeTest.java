package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

public class UpdateCcdHearingTypeTest {
    @Test
    public void givenGapsHearingTypeUpdate_shouldUpdateExistingCcdCase() {
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

        UpdateCcdHearingType updateCcdHearingType = new UpdateCcdHearingType();
        boolean updateData = updateCcdHearingType.updateHearingType(gapsCaseData, existingCcdCase);

        assertTrue(updateData);
        assertThat(existingCcdCase.getAppeal().getHearingType(), is("oral"));
    }
}
