package uk.gov.hmcts.reform.sscs.services.xml;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import uk.gov.hmcts.reform.sscs.exceptions.Gap2ValidationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.validation.SchemaFactory.newInstance;

@Service
public class XmlValidator {

    @Value("${sscs.gaps2.schema.location.reference}")
    private String refSchemaPath;
    @Value("${sscs.gaps2.schema.location.delta}")
    private String deltaSchemaPath;

    public void validateXml(String fileName) throws IOException, SAXException, XMLStreamException {

        String schemaPath;
        if (fileName.contains("Reference")) {
            schemaPath = refSchemaPath;
        } else if (fileName.contains("Delta") || fileName.contains("Full")) {
            schemaPath = deltaSchemaPath;
        } else {
            throw new Gap2ValidationException(String.format("Invalid input file %s", fileName));
        }

        try (FileInputStream stream = new FileInputStream(new File(fileName))) {
            InputStream in = getClass().getResourceAsStream(schemaPath);
            StreamSource schemaSource = new StreamSource(in);
            Validator validator = newInstance(W3C_XML_SCHEMA_NS_URI).newSchema(schemaSource).newValidator();
            validator.setErrorHandler(new XmlErrorHandler());
            XMLStreamReader xmlStreamReader = XMLInputFactory.newFactory().createXMLStreamReader(stream);
            validator.validate(new StAXSource(xmlStreamReader));
        }
    }
}
