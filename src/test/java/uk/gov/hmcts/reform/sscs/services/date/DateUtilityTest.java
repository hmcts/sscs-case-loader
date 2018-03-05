package uk.gov.hmcts.reform.sscs.services.date;

import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.Test;

public class DateUtilityTest {

    private static final String INSTANT = "2017-05-23T15:15:25.15+01:00";
    private final DateUtility dateUtility = new DateUtility();

    @Test
    public void givenDateTimeZoneAsString_shouldReturnALocalDateString() {
        assertNotNull(LocalDate.parse(dateUtility.getValidDate(INSTANT)));
    }

    @Test
    public void givenInstantAsString_shouldReturnALocalTimeString() {
        assertNotNull(LocalTime.parse(dateUtility.getValidTime(INSTANT)));
    }
}
