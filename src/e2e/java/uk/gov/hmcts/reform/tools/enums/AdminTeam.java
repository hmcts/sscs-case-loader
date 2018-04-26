package uk.gov.hmcts.reform.tools.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum AdminTeam {
    LEICESTER_B(998);

    private static final Map<String, AdminTeam> lookup = new HashMap<>();

    static {
        for (AdminTeam m : EnumSet.allOf(AdminTeam.class))
            lookup.put(m.getCode(), m);
    }

    private int code;

    AdminTeam(int code) {
        this.code = code;

    }

    public String getCode() {
        return String.valueOf(code);
    }

    public static AdminTeam get(int code) {
        return lookup.get(code);
    }
}
