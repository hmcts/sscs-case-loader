package uk.gov.hmcts.reform.sscs.services.xml;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;
import uk.gov.hmcts.reform.sscs.exceptions.Gap2ValidationException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = XmlValidator.class)
public class XmlValidatorTest {

    @Autowired
    private XmlValidator validator;

    @Test
    public void shouldPassValidatorForValidReferenceXmlFiles() throws Exception {
        String refFilePath = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.xml";

        validator.validateXml(refFilePath);
    }

    @Test
    public void shouldPassValidatorForValidDeltaXmlFiles() throws Exception {
        String deltaFilePath = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";

        validator.validateXml(deltaFilePath);
    }

    @Test(expected = Gap2ValidationException.class)
    public void shouldFailValidationForInvalidFileType() throws Exception {
        String deltaFilePath = "src/test/resources/invalid_file.txt";

        validator.validateXml(deltaFilePath);
    }

    @Test(expected = SAXException.class)
    public void shouldFailValidatorForValidReferenceXmlFiles() throws Exception {
        String refFilePath = "src/test/resources/SSCS_ExtractInvalid_Delta_2017-06-30-09-25-56.xml";

        validator.validateXml(refFilePath);
    }
}
