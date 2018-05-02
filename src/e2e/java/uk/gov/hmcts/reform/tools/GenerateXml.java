package uk.gov.hmcts.reform.tools;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static uk.gov.hmcts.reform.tools.FileCopy.copy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import uk.gov.hmcts.reform.tools.builders.Appeal;
import uk.gov.hmcts.reform.tools.enums.AppealTemplate;
import uk.gov.hmcts.reform.tools.factory.AppealFactory;
import uk.gov.hmcts.reform.tools.utils.XmlWriter;


@SuppressWarnings({"PMD", "checkstyle:hideutilityclassconstructor"})

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GenerateXml {

    private static final String OUTPUT_DIR = "src/test/resources/updates";
    private static final String INCOMING_DIR = "docker/sftp/data/incoming";

    @BeforeClass
    public static void cleanUpOldFiles() throws IOException, ParserConfigurationException {
        cleanUpDirectory(OUTPUT_DIR);
    }

    @AfterClass
    public static void copyGeneratedFiles() throws IOException {

        copy(OUTPUT_DIR, INCOMING_DIR);
    }

    private static void cleanUpDirectory(String directory) throws IOException {
        if (get(directory).toFile().exists()) {
            walk(get(directory), FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .peek(System.out::println)
                    .forEach(File::delete);
        }
    }


    public static void generateXmlForAppeals() throws
            IOException, TransformerException, ParserConfigurationException {

        String createPath = "CreateAppeals";

        XmlWriter xmlWriter = new XmlWriter(createPath).newXmlWriter();

        AppealFactory appealFactory = new AppealFactory();

        appealFactory.selectAppeal(xmlWriter, AppealTemplate.NEW_DIRECT_LODGEMENT, 1);
        appealFactory.selectAppeal(xmlWriter, AppealTemplate.APPEAL_AWAITING_RESPONSE, 1);
        appealFactory.selectAppeal(xmlWriter, AppealTemplate.READY_TO_LIST, 1);
        appealFactory.selectAppeal(xmlWriter, AppealTemplate.LISTED_FOR_HEARING, 1);
        appealFactory.selectAppeal(xmlWriter, AppealTemplate.HEARD_FOR_DESTRUCTION, 1);

        xmlWriter.writeXml(OUTPUT_DIR);

        List<Appeal> appeals = xmlWriter.getAppealWrittenToFile();

    }
}
