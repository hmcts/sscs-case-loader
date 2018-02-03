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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

@Service
public class XmlValidator {

    @Value("${sscs.gaps2.schema.location.reference}")
    private String refSchemaPath;
    @Value("${sscs.gaps2.schema.location.delta}")
    private String deltaSchemaPath;

    public void validateXml(InputStream inputStream, String type) throws IOException, SAXException,
        XMLStreamException {
        String schemaPath = type.equals("Ref") ? refSchemaPath : deltaSchemaPath;
        InputStream schemaAsStream = getClass().getResourceAsStream(schemaPath);
        StreamSource schemaSource = new StreamSource(schemaAsStream);
        Validator validator = newInstance(W3C_XML_SCHEMA_NS_URI).newSchema(schemaSource).newValidator();
        validator.setErrorHandler(new XmlErrorHandler());
        XMLStreamReader xmlStreamReader = XMLInputFactory.newFactory().createXMLStreamReader(inputStream);
        validator.validate(new StAXSource(xmlStreamReader));
    }
}
