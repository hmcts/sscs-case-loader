package uk.gov.hmcts.reform.sscs.services.xml;

public enum XmlSchemas {
    DELTA("/schema/SSCS_Extract_Schema_Delta_0.9.xsd"),
    REF("/schema/SSCS_Extract_Schema_Reference_0.2.xsd");

    private String path;

    XmlSchemas(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
