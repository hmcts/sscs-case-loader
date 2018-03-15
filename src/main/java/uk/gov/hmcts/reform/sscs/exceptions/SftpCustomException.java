package uk.gov.hmcts.reform.sscs.exceptions;

public class SftpCustomException extends RuntimeException {
    public SftpCustomException(String message, Throwable cause) {
        super(String.format("SFTP %s", message), cause);
    }

    public SftpCustomException(String message, String fileName, Throwable cause) {
        this(String.format("%s - name: %s", message, fileName), cause);
    }
}
