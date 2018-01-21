package uk.gov.hmcts.reform.sscs.services;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;

import static org.junit.Assert.assertTrue;

public class ValidateXmlFilesServiceTest {
    private static final String DELTA_XML = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
    private static final String REF_XML = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.xml";

    @Test
    public void givenXmlFiles_shouldValidateXmlFiles() {
        ValidateXmlFilesService validateXmlFilesService = new ValidateXmlFilesService();
        XmlFiles xmlFiles = XmlFiles.builder().delta(DELTA_XML).ref(REF_XML).build();
        assertTrue(validateXmlFilesService.validate(xmlFiles));
    }

}
