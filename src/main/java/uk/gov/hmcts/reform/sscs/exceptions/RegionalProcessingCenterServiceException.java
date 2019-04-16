package uk.gov.hmcts.reform.sscs.exceptions;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class RegionalProcessingCenterServiceException extends RuntimeException {
    public RegionalProcessingCenterServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegionalProcessingCenterServiceException(String message) {
        super(message);
    }
}
