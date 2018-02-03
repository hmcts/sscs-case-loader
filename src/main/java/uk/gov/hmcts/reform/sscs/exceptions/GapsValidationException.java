package uk.gov.hmcts.reform.sscs.exceptions;

public class GapsValidationException extends RuntimeException {
    public GapsValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
