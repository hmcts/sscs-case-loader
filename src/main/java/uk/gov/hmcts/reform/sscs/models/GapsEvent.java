package uk.gov.hmcts.reform.sscs.models;

public enum GapsEvent {

    APPEAL_RECEIVED("appealReceived", "Appeal received", "3"),
    RESPONSE_RECEIVED("responseReceived", "Response received", "18");

    private String type;
    private String description;
    private String gapsCode;

    GapsEvent(String type, String description, String gapsCode) {
        this.type = type;
        this.description = description;
        this.gapsCode = gapsCode;
    }

    public static GapsEvent getGapsEventByCode(String x) {
        GapsEvent e = null;
        for (GapsEvent event : GapsEvent.values()) {
            if (event.gapsCode.toString().equals(x)) {
                e = event;
            }
        }
        return e;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getGapsCode() {
        return gapsCode;
    }
}
