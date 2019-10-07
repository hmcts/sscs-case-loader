package uk.gov.hmcts.reform.sscs.exceptions;


@SuppressWarnings("squid:MaximumInheritanceDepth")
public class GapsValidationException extends RuntimeException {
    public GapsValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
