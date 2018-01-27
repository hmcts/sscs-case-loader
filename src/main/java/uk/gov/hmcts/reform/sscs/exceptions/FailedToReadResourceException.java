package uk.gov.hmcts.reform.sscs.exceptions;

public class FailedToReadResourceException extends RuntimeException {
    public FailedToReadResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
