package uk.gov.hmcts.reform.sscs.services.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class DateUtility {
    public String getValidDate(String dateTime) {
        return dateTime != null ? parseAndReturnLocalDateIfTrueElseReturnLocalTime(dateTime, true) : "";
    }

    private String parseAndReturnLocalDateIfTrueElseReturnLocalTime(String utcTime, boolean date) {
        ZonedDateTime result = ZonedDateTime.parse(utcTime, DateTimeFormatter.ISO_DATE_TIME);
        return date ? result.toLocalDate().toString() : result.toLocalTime().toString();
    }

    public String getValidTime(String dateTime) {
        return dateTime != null ? parseAndReturnLocalDateIfTrueElseReturnLocalTime(dateTime, false) : "";
    }

}
