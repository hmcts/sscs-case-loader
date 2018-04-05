package uk.gov.hmcts.reform.sscs.exceptions;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class SftpCustomException extends UnknownErrorCodeException {
    public SftpCustomException(String message, Throwable cause) {

        super(AlertLevel.P1, String.format("SFTP %s", message), cause);
    }

    public SftpCustomException(String message, String fileName, Throwable cause) {
        this(String.format("%s - name: %s", message, fileName), cause);
    }
}
