package uk.gov.hmcts.reform.sscs.exceptions;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class FailedXmlFileException extends RuntimeException {

    public FailedXmlFileException(String fileName) {
        super(String.format("Failed processing file - %s", fileName));
    }
}
