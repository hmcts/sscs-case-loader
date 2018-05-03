package uk.gov.hmcts.reform.tools.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Office {
    NORTH_WEST_LEICSTERSHIRE_DC(943);

    private static final Map<String, Office> lookup = new HashMap<>();

    static {
        for (Office m : EnumSet.allOf(Office.class)) {
            lookup.put(m.getCode(), m);
        }
    }

    private int code;

    Office(int code) {
        this.code = code;

    }

    public String getCode() {
        return String.valueOf(code);
    }

    public static Office get(int code) {
        return lookup.get(code);
    }
}
