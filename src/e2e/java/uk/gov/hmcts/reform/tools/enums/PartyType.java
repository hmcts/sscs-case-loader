package uk.gov.hmcts.reform.tools.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PartyType {
    APPELLANT(4),
    SUPPORTER(24);

    private static final Map<String, PartyType> lookup = new HashMap<>();

    static {
        for (PartyType m : EnumSet.allOf(PartyType.class))
            lookup.put(m.getCode(), m);
    }

    private int code;

    PartyType(int code) {
        this.code = code;

    }

    public String getCode() {
        return String.valueOf(code);
    }

    public static PartyType get(int code) {
        return lookup.get(code);
    }
}
