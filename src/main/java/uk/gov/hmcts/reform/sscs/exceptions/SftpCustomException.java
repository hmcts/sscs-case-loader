package uk.gov.hmcts.reform.sscs.exceptions;

public class SftpCustomException extends RuntimeException {
    public SftpCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
