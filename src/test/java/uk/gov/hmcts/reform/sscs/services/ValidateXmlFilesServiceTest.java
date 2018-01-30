package uk.gov.hmcts.reform.sscs.services;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidateXmlFilesServiceTest {
    private static final String DELTA_XML = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
    private static final String REF_XML = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.xml";

    private ValidateXmlFilesService validateXmlFilesService;
    
    @Before
    public void setUp() {
        validateXmlFilesService = new ValidateXmlFilesService();
    }
    
    @Test
    public void givenXmlFiles_shouldValidateXmlFiles() {

        XmlFiles xmlFiles = XmlFiles.builder().delta(DELTA_XML).ref(REF_XML).build();
        assertTrue(validateXmlFilesService.validate(xmlFiles));
    }
    
    @Test
    public void shouldValidateWithNoFilesGiven() {
        assertFalse(validateXmlFilesService.validate(null));
    }

}
