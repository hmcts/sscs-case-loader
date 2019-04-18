package uk.gov.hmcts.reform.sscs.util;

import java.util.regex.Pattern;

public final class UkMobile {

    private static final Pattern UK_MOBILE = Pattern.compile("^((?:(?:\\(?(?:0(?:0|11)\\)?[\\s-]?\\(?|\\+)\\d{1,4}"
            + "\\)?[\\s-]?(?:\\(?0\\)?[\\s-]?)?)|(?:\\(?0))(?:(?:\\d{5}\\)?[\\s-]?\\d{4,5})|(?:\\d{4}\\)?[\\s-]?"
            + "(?:\\d{5}|\\d{3}[\\s-]?\\d{3}))|(?:\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{3,4})|(?:\\d{2}\\)?[\\s-]?\\d{4}"
            + "[\\s-]?\\d{4}))(?:[\\s-]?(?:x|ext\\.?|\\#)\\d{3,4})?)?$");

    private UkMobile() {
        // Void
    }

    public static boolean validate(String value) {
        return value != null && UK_MOBILE.matcher(value).matches();
    }

}
