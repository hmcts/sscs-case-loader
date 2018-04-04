package uk.gov.hmcts.reform.sscs.exceptions;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

public class GapsValidationException extends UnknownErrorCodeException {
    public GapsValidationException(String message, Throwable cause) {
        super(AlertLevel.P1, message, cause);
    }
}
