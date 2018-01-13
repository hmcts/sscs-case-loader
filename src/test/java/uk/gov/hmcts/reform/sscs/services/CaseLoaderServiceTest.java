package uk.gov.hmcts.reform.sscs.services;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CaseLoaderServiceTest {

    private CaseLoaderService caseLoaderService = new SftpCaseLoaderImpl();

    @Test
    public void shouldFetchXmlFilesWhenSentFromGaps2() {
        assertTrue(caseLoaderService.fetchXmlFilesFromGaps2());
    }

    @Test
    public void shouldValidateXmlFiles() {
        assertTrue(caseLoaderService.validateXmlFiles());

    }

//  TODO implement this test case based on the XmlCasesToJsonCasesTest class
    @Test
    public void shouldTransformXmlFilesToJsonFiles() {

    }
}
