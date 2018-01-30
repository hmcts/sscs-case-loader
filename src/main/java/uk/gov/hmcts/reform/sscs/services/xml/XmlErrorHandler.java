package uk.gov.hmcts.reform.sscs.services.xml;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@Component
@Slf4j
public class XmlErrorHandler implements ErrorHandler {

    @Override
    public void error(SAXParseException ex) throws SAXException {
        checkAndThrowException(ex);
    }

    @Override
    public void fatalError(SAXParseException ex) throws SAXException {
        checkAndThrowException(ex);
    }

    @Override
    public void warning(SAXParseException e) {
        log.warn("Warning message while validating gaps2 xml: ", e.getMessage());
    }

    private void checkAndThrowException(SAXParseException ex) throws SAXParseException {
        String message = ex.getMessage();
        if (!message.contains("xml:space")) {
            throw ex;
        }
    }
}
