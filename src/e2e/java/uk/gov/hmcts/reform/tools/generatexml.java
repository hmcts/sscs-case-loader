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
import uk.gov.hmcts.reform.tools.utils.XMLWriter;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class generatexml {

    public static final String OUTPUT_DIR = "src/test/resources/updates";
    public static final String Incoming_dir = "docker/sftp/data/incoming";

    private static XMLWriter xmlWriter;

    @BeforeClass
    public static void cleanUpOldFiles() throws IOException, ParserConfigurationException {
        cleanUpDirectory(OUTPUT_DIR);
    }

    @AfterClass
    public static void copyGeneratedFiles() throws IOException {

        copy(OUTPUT_DIR, Incoming_dir);
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


    public static void generatexmlforappeals() throws IOException, TransformerException, ParserConfigurationException {

        String createPath = "CreateAppeals";

        xmlWriter = new XMLWriter(createPath).newXMLWriter();

        AppealFactory appealFactory = new AppealFactory();

        appealFactory.selectAppeal(xmlWriter, AppealTemplate.NEW_DIRECT_LODGEMENT, 1);
        appealFactory.selectAppeal(xmlWriter, AppealTemplate.APPEAL_AWAITING_RESPONSE, 1);
        appealFactory.selectAppeal(xmlWriter, AppealTemplate.READY_TO_LIST, 1);
        appealFactory.selectAppeal(xmlWriter, AppealTemplate.LISTED_FOR_HEARING, 1);
        appealFactory.selectAppeal(xmlWriter, AppealTemplate.HEARD_FOR_DESTRUCTION, 1);

        xmlWriter.writeXML(OUTPUT_DIR);

        List<Appeal> appeals = xmlWriter.getAppealWrittenToFile();

        for (Appeal appeal : appeals) {

            System.out.println("Appeal [caseId : " + appeal.getCaseId() + ", caseRefNum: " + appeal.getCaseRefNumber() + "]");

        }
    }
}