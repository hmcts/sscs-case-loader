package uk.gov.hmcts.reform.sscs.services.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public final class DateHelper {
    private DateHelper() {
    }

    public static String getValidDateOrTime(String utcTime, boolean isDate) {
        if (utcTime != null) {
            ZonedDateTime result = ZonedDateTime.parse(utcTime, DateTimeFormatter.ISO_DATE_TIME);
            return isDate ? result.toLocalDate().toString() : result.toLocalTime().toString();
        }
        return "";
    }

}
