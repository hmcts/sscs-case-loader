package uk.gov.hmcts.reform.sscs.services.xml;

import org.junit.Test;

public class XmlSchemasTest {

    @Test
    public void canLoadDeltaSchema() {
        XmlSchemas.class
            .getClassLoader()
            .getResourceAsStream(XmlSchemas.DELTA.getPath().replaceFirst("/", "")).toString();
    }

    @Test
    public void canLoadReferenceSchema() {
        XmlSchemas.class
            .getClassLoader()
            .getResourceAsStream(XmlSchemas.REF.getPath().replaceFirst("/", "")).toString();
    }
}
