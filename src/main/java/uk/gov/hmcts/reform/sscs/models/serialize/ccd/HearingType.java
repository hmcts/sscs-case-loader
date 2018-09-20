package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

public enum HearingType {
     PAPER("paper", "1"), ORAL("oral", "2"), DOMICILIARY("domiciliary", "3");

    private String value;
    private String tribunalsTypeId;

    HearingType(String value, String tribunalsTypeId) {
        this.value = value;
        this.tribunalsTypeId = tribunalsTypeId;
    }

    public String getValue() {
        return value;
    }

    public static HearingType getHearingTypeByTribunalsTypeId(String tribunalsTypeId) {
        HearingType hearingType = null;
        for (HearingType ht: HearingType.values()) {
            if (ht.tribunalsTypeId.equals(tribunalsTypeId)) {
                hearingType = ht;
            }
        }
        return hearingType;
    }

}
