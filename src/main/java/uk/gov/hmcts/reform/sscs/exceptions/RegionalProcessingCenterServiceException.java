package uk.gov.hmcts.reform.sscs.exceptions;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class RegionalProcessingCenterServiceException extends UnknownErrorCodeException {
    public RegionalProcessingCenterServiceException(String message, Throwable cause) {
        super(AlertLevel.P4, message, cause);
    }

    public RegionalProcessingCenterServiceException(String message) {
        super(AlertLevel.P4, message);
    }
}
