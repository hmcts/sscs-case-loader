package uk.gov.hmcts.reform.sscs.exceptions;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ProcessDeltaException extends RuntimeException {
    public ProcessDeltaException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessDeltaException(String message) {
        super(message);
    }
}
