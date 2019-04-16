package uk.gov.hmcts.reform.sscs.exceptions;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class JsonMapperErrorException extends RuntimeException {
    public JsonMapperErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
