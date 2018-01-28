package uk.gov.hmcts.reform.sscs.exceptions;

public class FailedToReadFromFileException extends RuntimeException {
    public FailedToReadFromFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
