package uk.gov.hmcts.reform.sscs.services;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.exceptions.GapsValidationException;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@RunWith(SpringRunner.class)
@SpringBootTest
public class XmlValidatorTest {

    private static final String INVALID_DELTA_PATH =
        "src/test/resources/SSCS_ExtractInvalid_Delta_2017-06-30-09-25-56.xml";
    private static final String DELTA_PATH = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
    private static final String REF_PATH = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.xml";

    @Autowired
    private XmlValidator validator;

    @Test
    public void shouldValidateContentGivenValidDeltaInputStream() throws IOException {
        validator.validateXml(FileUtils.readFileToString(new File( DELTA_PATH), StandardCharsets.UTF_8.name()),
            true);
    }

    @Test
    public void shouldPassValidatorGivenValidRefXmlInputStream() throws IOException {
        validator.validateXml(FileUtils.readFileToString(new File(REF_PATH), StandardCharsets.UTF_8.name()),
            false);
    }


    @Test(expected = GapsValidationException.class)
    public void shouldFailValidatorGivenInvalidRefXmlFile() throws IOException {
        validator.validateXml(FileUtils.readFileToString(new File(INVALID_DELTA_PATH), StandardCharsets.UTF_8.name()),
            true);
    }
}
