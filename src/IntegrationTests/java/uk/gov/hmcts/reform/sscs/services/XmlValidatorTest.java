package uk.gov.hmcts.reform.sscs.services;

import java.io.File;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.SAXException;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = XmlValidator.class)
public class XmlValidatorTest {

    private static final String INVALID_DELTA_PATH =
        "src/test/resources/SSCS_ExtractInvalid_Delta_2017-06-30-09-25-56.xml";
    private static final String DELTA_PATH = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
    private static final String REF_PATH = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.xml";

    @Autowired
    private XmlValidator validator;

    @Test
    public void givenValidDeltaInputStream_shouldValidateContent() throws Exception {
        InputStream deltaStream = FileUtils.openInputStream(new File(DELTA_PATH));
        validator.validateXml(deltaStream, "Delta");
    }

    @Test
    public void givenValidRefXmlInputStream_shouldPassValidator() throws Exception {
        InputStream refStream = FileUtils.openInputStream(new File(REF_PATH));
        validator.validateXml(refStream, "Ref");
    }


    @Test(expected = SAXException.class)
    public void givenInvalidRefXmlFile_shouldFailValidator() throws Exception {
        InputStream invalidDeltaStream = FileUtils.openInputStream(new File(INVALID_DELTA_PATH));
        validator.validateXml(invalidDeltaStream, "Delta");
    }
}
