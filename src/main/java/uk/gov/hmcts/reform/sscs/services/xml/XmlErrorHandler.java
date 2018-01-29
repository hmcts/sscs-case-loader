package uk.gov.hmcts.reform.sscs.services.xml;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class XmlErrorHandler implements ErrorHandler {
    private final Logger log = getLogger(XmlErrorHandler.class);

    @Override
    public void error(SAXParseException ex) throws SAXException {
        checkAndThrowException(ex);
    }

    @Override
    public void fatalError(SAXParseException ex) throws SAXException {
        checkAndThrowException(ex);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        log.warn("Warning message while validating gaps2 xml: ", e.getMessage());
    }

    private void checkAndThrowException(SAXParseException ex) throws SAXParseException {
        String message = ex.getMessage();
        if (!message.contains("xml:space")) {
            throw ex;
        }
    }
}
