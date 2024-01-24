package uk.gov.hmcts.reform.sscs.exceptions;
import feign.FeignException;
import org.slf4j.Logger;
public class FeignExceptionLog {

    public static void debugCcdException(Logger logger, FeignException exception, String introMessage) {
        logger.debug(
            "{}. CCD response: {}",
            introMessage,
            // exception.contentUTF8() uses response body internally
            exception.responseBody().isPresent() ? exception.contentUTF8() : exception.getMessage()
        );
    }
}
