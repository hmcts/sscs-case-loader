package uk.gov.hmcts.reform.sscs.exceptions;

import feign.FeignException;
import org.slf4j.Logger;

public final class FeignExceptionLogger {

    private FeignExceptionLogger() {
        // empty utility class construct
    }

    public static void debugCaseLoaderException(Logger logger, FeignException exception, String introMessage) {
        logger.debug(
            introMessage,
            // exception.contentUTF8() uses response body internally
            exception.responseBody().isPresent() ? exception.contentUTF8() : exception.getMessage()
        );
    }
}
