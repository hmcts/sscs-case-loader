package uk.gov.hmcts.reform.tools;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import org.apache.commons.io.FileUtils;

@SuppressWarnings({"PMD", "checkstyle:hideutilityclassconstructor"})

public final class FileCopy {

    public static void copy(String source, String output) throws IOException {
        if (get(output).toFile().exists()) {
            walk(get(output), FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .peek(System.out::println)
                .forEach(File::delete);
        }

        if (get(source).toFile().listFiles() != null) {
            for (File sourceFile : get(source).toFile().listFiles()) {
                FileUtils.copyFileToDirectory(sourceFile, get(output).toFile());
            }
        }
    }
}
