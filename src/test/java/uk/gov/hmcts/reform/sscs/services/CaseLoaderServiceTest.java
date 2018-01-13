package uk.gov.hmcts.reform.sscs.services;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CaseLoaderServiceTest {

    @Test
    public void shouldFetchXmlFilesWhenSentFromGaps2() {
        CaseLoaderService caseLoaderService = new SftpCaseLoaderImpl();
        assertTrue(caseLoaderService.fetchXmlFilesFromGaps2());
    }
}
