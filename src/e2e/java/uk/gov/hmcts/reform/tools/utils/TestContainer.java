package uk.gov.hmcts.reform.tools.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings({"PMD", "checkstyle:hideutilityclassconstructor"})

public class TestContainer {

    public static String asGapsDate(Date date) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        return formatter.format(date) + "+01:00";
    }

    public static Date backDate(Date baseDate, String dayOffset) {
        return backDateOffset(baseDate, Integer.valueOf(dayOffset));
    }

    public static Date backDate(Date baseDate, Integer daysOffset) {
        return backDateOffset(baseDate, daysOffset);
    }

    private static Date backDateOffset(Date baseDate, Integer dayOffset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        cal.add(Calendar.DATE, -dayOffset);
        return cal.getTime();
    }

}
