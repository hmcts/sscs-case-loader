package uk.gov.hmcts.reform.sscs.services.date;

import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class DateHelperTest {

    private static final String INSTANT = "2017-05-23T15:15:25.15+01:00";

    @Test
    public void givenDateTimeZoneAsString_shouldReturnALocalDateOrLocalTimeString() {
        assertNotNull(LocalDate.parse(DateHelper.getValidDateOrTime(INSTANT, true)));
    }

    @Test
    public void givenInstantAsString_shouldReturnALocalTimeString() {
        assertNotNull(LocalTime.parse(DateHelper.getValidDateOrTime(INSTANT, false)));
    }

}
