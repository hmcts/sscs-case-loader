package uk.gov.hmcts.reform.sscs.exceptions;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class TransformException extends RuntimeException {
    public TransformException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransformException(String message) {
        super(message);
    }
}
