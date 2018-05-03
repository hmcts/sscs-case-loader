package uk.gov.hmcts.reform.tools.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum MajorStatusType {
    NEW_DIRECT_LODGEMENT(600),
    AWAITING_RESPONSE(3),
    READY_TO_LIST(18),
    LISTED_FOR_HEARING(24),
    DORMANT_APPEAL_HEARD_FOR_DESTRUCTION(33);

    private static final Map<String, MajorStatusType> lookup = new HashMap<>();

    static {
        for (MajorStatusType m : EnumSet.allOf(MajorStatusType.class)) {
            lookup.put(m.getCode(), m);
        }
    }

    private int code;

    MajorStatusType(int code) {
        this.code = code;

    }

    public String getCode() {
        return String.valueOf(code);
    }

    public static MajorStatusType get(int code) {
        return lookup.get(code);
    }
}
