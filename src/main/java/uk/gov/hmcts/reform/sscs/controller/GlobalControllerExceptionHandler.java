package uk.gov.hmcts.reform.sscs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import uk.gov.hmcts.reform.sscs.exceptions.GapsValidationException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

@ControllerAdvice
public class GlobalControllerExceptionHandler  extends ResponseEntityExceptionHandler  {
    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

        private boolean shouldRethrowException(Exception ex) {
            String message = ex.getMessage();
            if (!message.contains("xml:space")) {
                logger.error("Exception: " + ex.getMessage());
                return true;
            }
            return false;
        }

        @ExceptionHandler({SAXParseException.class, GapsValidationException.class})
        private ResponseEntity<Object> handleException(Exception ex) throws Exception {
            if (shouldRethrowException(ex)) {
                throw ex;
            }
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        @ExceptionHandler(IOException.class)
        public ResponseEntity<Object> handleIOException(IOException ex) {
            logger.error("Failed to read stream for xml file", ex);
            GapsValidationException gapsValidationException = new GapsValidationException("Failed to read stream for xml file", ex);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(gapsValidationException.getMessage());
        }

        @ExceptionHandler({SAXException.class, SAXParseException.class})
        public ResponseEntity<Object> handleSAXException(SAXException ex) {
            logger.error("Failed to read schema", ex);
            GapsValidationException gapsValidationException = new GapsValidationException("Failed to read schema", ex);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(gapsValidationException.getMessage());
        }

        @ExceptionHandler(XMLStreamException.class)
        public ResponseEntity<Object> handleXMLStreamException(XMLStreamException ex) {
            logger.error("Failed to parse xml file", ex);
            GapsValidationException gapsValidationException = new GapsValidationException("Failed to parse xml file", ex);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(gapsValidationException.getMessage());
        }
    }


//    @ResponseBody
//    @ExceptionHandler({SAXParseException.class, GapsValidationException.class})
//    private boolean shouldRethrowException(Exception ex) {
//        String message = ex.getMessage();
//        if (!message.contains("xml:space")) {
//            logger.error("Exception: " + ex.getMessage());
//            return true;
//        }
//        return false;
//    }
//
//    @ExceptionHandler({SAXParseException.class, GapsValidationException.class})
//    private ResponseEntity<Object> handleException(Exception ex) throws Exception {
//        if (shouldRethrowException(ex)) {
//            throw ex;
//        }
//        return ResponseEntity.status(HttpStatus.CONFLICT).build();
//    }

//    private void checkAndThrowException(SAXParseException ex) throws SAXParseException {
//        System.out.println("*******************************************************************");
//        String message = ex.getMessage();
//        if (!message.contains("xml:space")) {
////            log.error("Exception : "  + ex.getMessage());
//            throw ex;
//        }
//    }
//
//    @ExceptionHandler(GapsValidationException.class)
//    private void checkAndThrowGapsValidationException(GapsValidationException ex) throws GapsValidationException {
//        System.out.println("*****************************44444**************************************");
//        String message = ex.getMessage();
//        if (!message.contains("xml:space")) {
////            log.error("Exception : "  + ex.getMessage());
//            throw ex;
//        }
//    }

