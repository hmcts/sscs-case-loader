package uk.gov.hmcts.reform.sscs.exceptions;

public class ApplicationErrorException extends RuntimeException {
    public ApplicationErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
