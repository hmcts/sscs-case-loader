package uk.gov.hmcts.reform.sscs.exceptions;

public class TransformException extends RuntimeException {
    public TransformException(String message, Throwable cause) {
        super(message, cause);
    }
}
