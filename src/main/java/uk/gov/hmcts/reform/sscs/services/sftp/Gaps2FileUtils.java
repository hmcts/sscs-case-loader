package uk.gov.hmcts.reform.sscs.services.sftp;

import static java.time.LocalDateTime.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


public class Gaps2FileUtils {

    public static final String GAPS2_DATETIME_FORMAT = "yyyy-MM-dd-HH-mm-ss";
    public static final String XML = ".xml";
    public static final String UNDER_SCORE = "_";

    private Gaps2FileUtils() {
    }

    public static List<String> getOrderByDateAndTime(List<String> fileNameList) {
        if (null != fileNameList && !fileNameList.isEmpty()) {
            return fileNameList.stream()
                .filter(f -> getDate(f) != null)
                .sorted((f1, f2) -> getDate(f1).compareTo(getDate(f2))).collect(toList());
        } else {
            return Collections.emptyList();
        }
    }

    private static LocalDateTime getDate(String fileName) {
        String[] split = fileName.split(UNDER_SCORE);
        String dateTimeString = split[3].replace(XML, "");
        return  parse(dateTimeString, ofPattern(GAPS2_DATETIME_FORMAT));
    }

}
