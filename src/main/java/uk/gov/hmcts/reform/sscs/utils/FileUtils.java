package uk.gov.hmcts.reform.sscs.utils;

import uk.gov.hmcts.reform.sscs.exceptions.FailedToReadFromFileException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FileUtils {

    private FileUtils() {
    }

    public static String getFileContentGivenFilePath(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
        } catch (IOException e) {
            throw new FailedToReadFromFileException("failed to read from file: " + filePath, e);
        }
    }

}
