package uk.gov.hmcts.reform.tools.utils;

import java.util.Calendar;
import java.util.Locale;

public class Utils {

    public String getNewCaseId() {

        return getSixDigitNumber();

    }

    public String getNewHearingId() {

        return getSixDigitNumber();

    }

    private String getSixDigitNumber() {

        java.util.Random generator = new java.util.Random();
        generator.setSeed(System.currentTimeMillis());
        int i = generator.nextInt(1000000) % 1000000;

        java.text.DecimalFormat f = new java.text.DecimalFormat("000000");
        return f.format(i);

    }

    public String getCaseRefNumber(String casePrefix, String appealNumber) {

        return casePrefix + appealNumber.substring(0, 1) + "/" + appealNumber.substring(1, 6);

    }

    public String getSurname(String caseId) {
        Calendar calendar = Calendar.getInstance();
        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + "_" + caseId;
    }

}
