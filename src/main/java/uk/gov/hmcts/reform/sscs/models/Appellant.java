package uk.gov.hmcts.reform.sscs.models;

public class Appellant {
    private Name name;

    public Appellant(Name name) {
        this.name = name;
    }

    public Name getName() {
        return name;
    }
}
