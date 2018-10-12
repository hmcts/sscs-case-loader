package uk.gov.hmcts.reform.sscs.exceptions;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class CreateCcdCaseException extends UnknownErrorCodeException {

    public CreateCcdCaseException(String message, Throwable cause) {
        super(AlertLevel.P1, message, cause);
    }
}
