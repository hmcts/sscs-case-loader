package uk.gov.hmcts.reform.sscs.services.xml;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import uk.gov.hmcts.reform.sscs.exceptions.GapsValidationException;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;

@Service
public class XmlValidator {

    private final SftpSshService sftpSshService;

    @Autowired
    XmlValidator(SftpSshService sftpSshService) {
        this.sftpSshService = sftpSshService;
    }

    public void validateXml(Gaps2File xmlFile) {
        boolean failure = true;
        try (InputStream xmlAsInputStream = sftpSshService.readExtractFile(xmlFile)) {
            String schemaPath = xmlFile.isDelta() ? XmlSchemas.DELTA.getPath() : XmlSchemas.REF.getPath();
            InputStream schemaAsStream = getClass().getResourceAsStream(schemaPath);
            StreamSource schemaSource = new StreamSource(schemaAsStream);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Validator validator = schemaFactory.newSchema(schemaSource).newValidator();
            validator.setErrorHandler(new XmlErrorHandler());

            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(xmlAsInputStream);
            validator.validate(new StAXSource(xmlStreamReader));
            failure = false;
        } catch (IOException e) {
            throw new GapsValidationException("Failed to read stream for xml file " + xmlFile.getName(), e);
        } catch (SAXException e) {
            throw new GapsValidationException("Failed to read schema", e);
        } catch (XMLStreamException e) {
            throw new GapsValidationException("Failed to parse xml file " + xmlFile.getName(), e);
        } finally {
            if (failure) {
                sftpSshService.move(xmlFile, false);
            }
        }
    }
}
