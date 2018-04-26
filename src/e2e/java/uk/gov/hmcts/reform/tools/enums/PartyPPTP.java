package uk.gov.hmcts.reform.tools.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PartyPPTP {
    APPELLANT(4);

    private static final Map<String, PartyPPTP> lookup = new HashMap<>();

    static {
        for (PartyPPTP m : EnumSet.allOf(PartyPPTP.class))
            lookup.put(m.getCode(), m);
    }

    private int code;

    PartyPPTP(int code) {
        this.code = code;

    }

    public String getCode() {
        return String.valueOf(code);
    }

    public static PartyPPTP get(int code) {
        return lookup.get(code);
    }
}
