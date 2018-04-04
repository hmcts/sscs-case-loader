package uk.gov.hmcts.reform.sscs.exceptions;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

public class FailedXmlFileException extends UnknownErrorCodeException {

    public FailedXmlFileException(String fileName) {
        super(AlertLevel.P1, String.format("Failed processing file - %s", fileName));
    }
}
