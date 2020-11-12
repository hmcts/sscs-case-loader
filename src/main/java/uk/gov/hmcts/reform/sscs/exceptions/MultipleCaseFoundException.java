package uk.gov.hmcts.reform.sscs.exceptions;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class MultipleCaseFoundException extends RuntimeException {
    public MultipleCaseFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleCaseFoundException(String message) {
        super(message);
    }
}
