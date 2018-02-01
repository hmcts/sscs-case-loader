package uk.gov.hmcts.reform.sscs.services.xml;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlErrorHandlerTest {

    private final XmlErrorHandler xmlErrorHandler = new XmlErrorHandler();

    @Test
    public void givenWarning_shouldNotThrowAException() {
        xmlErrorHandler.warning(new SAXParseException(null, null));
        assertTrue(true);
    }

    @Test(expected = SAXException.class)
    public void givenFatalError_shouldTNothrowException() throws SAXException {
        xmlErrorHandler.fatalError(new SAXParseException("message", null));
    }

    @Test
    public void givenFatalError_shouldThrowException() throws SAXException {
        xmlErrorHandler.fatalError(new SAXParseException("xml:space", null));
    }
}
