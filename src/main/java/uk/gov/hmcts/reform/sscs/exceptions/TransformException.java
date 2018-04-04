package uk.gov.hmcts.reform.sscs.exceptions;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

public class TransformException extends UnknownErrorCodeException {
    public TransformException(String message, Throwable cause) {
        super(AlertLevel.P1, message, cause);
    }

    public TransformException(String message) {
        super(AlertLevel.P1, message);
    }
}
