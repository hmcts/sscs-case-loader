package uk.gov.hmcts.reform.sscs.exceptions;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

public class JsonMapperErrorException extends UnknownErrorCodeException {
    public JsonMapperErrorException(String message, Throwable cause) {
        super(AlertLevel.P4, message, cause);
    }
}
