package uk.gov.hmcts.reform.sscs.models;

public enum GapsEvent {

    APPEAL_RECEIVED("appealReceived", "Appeal received", "3"),
    RESPONSE_RECEIVED("responseReceived", "Response received", "18");

    private String type;
    private String description;
    private String status;

    GapsEvent(String type, String description, String status) {
        this.type = type;
        this.description = description;
        this.status = status;
    }

    public static GapsEvent getGapsEventByStatus(String status) {
        GapsEvent e = null;
        for (GapsEvent event : GapsEvent.values()) {
            if (event.status.toString().equals(status)) {
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

    public String getStatus() {
        return status;
    }
}
