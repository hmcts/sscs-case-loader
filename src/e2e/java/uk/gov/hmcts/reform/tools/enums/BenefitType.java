package uk.gov.hmcts.reform.tools.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum BenefitType {
    PIP(1707),
    ESA(1707);

    private static final Map<String, BenefitType> lookup = new HashMap<>();

    static {
        for (BenefitType m : EnumSet.allOf(BenefitType.class))
            lookup.put(m.getCode(), m);
    }

    private int code;

    BenefitType(int code) {
        this.code = code;

    }

    public String getCode() {
        return String.valueOf(code);
    }

    public static BenefitType get(int code) {
        return lookup.get(code);
    }
}
