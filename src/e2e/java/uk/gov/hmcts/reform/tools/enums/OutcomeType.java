package uk.gov.hmcts.reform.tools.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum OutcomeType {
    UNSET(0);

    private static final Map<String, OutcomeType> lookup = new HashMap<>();

    static {
        for (OutcomeType m : EnumSet.allOf(OutcomeType.class))
            lookup.put(m.getCode(), m);
    }

    private int code;

    OutcomeType(int code) {
        this.code = code;

    }

    public String getCode() {
        return String.valueOf(code);
    }

    public static OutcomeType get(int code) {
        return lookup.get(code);
    }
}