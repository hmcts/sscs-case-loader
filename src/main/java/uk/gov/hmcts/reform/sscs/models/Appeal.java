package uk.gov.hmcts.reform.sscs.models;

public class Appeal {
    private String mrnDate;
    private String mrnMissingReason;
    private Appellant appellant;

    public Appeal(String mrnDate, String mrnMissingReason, Appellant appellant) {
        this.mrnDate = mrnDate;
        this.mrnMissingReason = mrnMissingReason;
        this.appellant = appellant;
    }

    public String getMrnDate() {
        return mrnDate;
    }

    public String getMrnMissingReason() {
        return mrnMissingReason;
    }

    public Appellant getAppellant() {
        return appellant;
    }
}
