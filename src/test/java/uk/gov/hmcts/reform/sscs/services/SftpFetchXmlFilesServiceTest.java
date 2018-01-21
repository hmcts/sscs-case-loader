package uk.gov.hmcts.reform.sscs.services;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class SftpFetchXmlFilesServiceTest {

    private final FetchXmlFilesService sftpFetchXmlFiles = new SftpFetchXmlFilesImpl();

    @Test
    public void givenXmlFilesUploadedInGaps_shouldFetchXmlFiles() {
        Optional<XmlFiles> optionalXmlFiles = sftpFetchXmlFiles.fetch();
        assertTrue(optionalXmlFiles.isPresent());
    }

}
