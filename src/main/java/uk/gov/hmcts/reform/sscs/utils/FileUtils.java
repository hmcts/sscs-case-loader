package uk.gov.hmcts.reform.sscs.utils;

import uk.gov.hmcts.reform.sscs.exceptions.FailedToReadResourceException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class FileUtils {

    public String getResourceContentGivenResourceName(String resourceName) {
        try {
            URL resource = getClass().getResource(resourceName);
            URI resourceUrl = resource.toURI();
            return new String(Files.readAllBytes(Paths.get(resourceUrl)), UTF_8);
        } catch (NullPointerException | IOException | URISyntaxException e) {
            throw new FailedToReadResourceException("failed to read resource: " + resourceName, e);
        }
    }

}
