package uk.gov.hmcts.reform.sscs.services;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CaseLoaderServiceTest {

    private final CaseLoaderService caseLoaderService = new SftpCaseLoaderImpl();

    @Test
    public void shouldFetchXmlFilesWhenSentFromGaps2() {
        assertTrue(caseLoaderService.fetchXmlFilesFromGaps2());
    }

    @Test
    public void shouldValidateXmlFiles() {
        assertTrue(caseLoaderService.validateXmlFiles());

    }

    //    @Test
    //    public void shouldTransformXmlFilesToJsonFiles() {
    //
    //    }
}
