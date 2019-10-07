package uk.gov.hmcts.reform.sscs.exceptions;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class CcdException extends RuntimeException {
    public CcdException(String message, Throwable cause) {
        super(message, cause);
    }

    public CcdException(String message) {
        super(message);
    }
}
