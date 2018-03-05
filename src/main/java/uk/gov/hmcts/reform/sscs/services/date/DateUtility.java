package uk.gov.hmcts.reform.sscs.services.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class DateUtility {
    public String getValidDate(String dateTime) {
        return dateTime != null ? parseToIsoDateTime(dateTime) : "";
    }

    private String parseToIsoDateTime(String utcTime) {
        ZonedDateTime result = ZonedDateTime.parse(utcTime, DateTimeFormatter.ISO_DATE_TIME);
        return result.toLocalDate().toString();
    }

    public String getValidTime(String dateTime) {
        return dateTime != null ? parseToIsoTime(dateTime) : "";
    }

    private String parseToIsoTime(String utcTime) {
        ZonedDateTime result = ZonedDateTime.parse(utcTime, DateTimeFormatter.ISO_DATE_TIME);
        return result.toLocalTime().toString();
    }
}
