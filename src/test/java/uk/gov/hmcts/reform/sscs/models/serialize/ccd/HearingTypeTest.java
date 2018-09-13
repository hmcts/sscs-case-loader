package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import static org.junit.Assert.*;

import org.hamcrest.Matchers;
import org.junit.Test;

public class HearingTypeTest {

    @Test
    public void shouldReturnOralHearingIfTribunalsTypeIs2() {
        HearingType hearingType = HearingType.getHearingTypeByTribunalsTypeId("2");

        assertThat(hearingType, Matchers.equalTo(HearingType.ORAL));
    }
}
