package uk.gov.hmcts.reform.sscs.models;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XmlFilesTest {
    @Test
    public void shouldCreateXmlFilesInstance() {
        XmlFiles xmlFiles = XmlFiles.builder().delta("delta.xml").ref("ref.xml").build();
        assertEquals("delta.xml", xmlFiles.getDelta());
        assertEquals("ref.xml", xmlFiles.getRef());
    }

}
