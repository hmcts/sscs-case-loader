package uk.gov.hmcts.reform.tools.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PartyPptp {
    APPELLANT(4);

    private static final Map<String, PartyPptp> lookup = new HashMap<>();

    static {
        for (PartyPptp m : EnumSet.allOf(PartyPptp.class)) {
            lookup.put(m.getCode(), m);
        }
    }

    private int code;

    PartyPptp(int code) {
        this.code = code;

    }

    public String getCode() {
        return String.valueOf(code);
    }

    public static PartyPptp get(int code) {
        return lookup.get(code);
    }
}
