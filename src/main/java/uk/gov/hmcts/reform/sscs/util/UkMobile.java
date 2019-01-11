package uk.gov.hmcts.reform.sscs.util;

import java.util.regex.Pattern;

public final class UkMobile {

    private static final Pattern UK_MOBILE = Pattern.compile("(?:(?:\\+44[\\s]?)|(?:0[\\s]?))"
            + "(?:7[\\s]?)(?:\\d[\\s]?){9}");

    private UkMobile() {
        // Void
    }

    public static boolean validate(String value) {
        return value != null && UK_MOBILE.matcher(value).matches();
    }

}
