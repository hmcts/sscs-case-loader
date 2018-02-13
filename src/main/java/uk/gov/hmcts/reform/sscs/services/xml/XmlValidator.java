package uk.gov.hmcts.reform.sscs.services.xml;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.validation.SchemaFactory.newInstance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import uk.gov.hmcts.reform.sscs.exceptions.GapsValidationException;

@Service
public class XmlValidator {

    public void validateXml(String xmlAsString, String type) {
        try {
            InputStream xmlAsInputStream = IOUtils.toInputStream(xmlAsString, StandardCharsets.UTF_8.name());
            String schemaPath = "Reference".equals(type) ? XmlSchemas.REF.getPath() : XmlSchemas.DELTA.getPath();
            InputStream schemaAsStream = getClass().getResourceAsStream(schemaPath);
            StreamSource schemaSource = new StreamSource(schemaAsStream);
            Validator validator = newInstance(W3C_XML_SCHEMA_NS_URI).newSchema(schemaSource).newValidator();
            validator.setErrorHandler(new XmlErrorHandler());
            XMLStreamReader xmlStreamReader = XMLInputFactory.newFactory().createXMLStreamReader(xmlAsInputStream);
            validator.validate(new StAXSource(xmlStreamReader));
        } catch (IOException | SAXException | XMLStreamException e) {
            throw new GapsValidationException("Oops...something went wrong, ", e);
        }
    }
}
