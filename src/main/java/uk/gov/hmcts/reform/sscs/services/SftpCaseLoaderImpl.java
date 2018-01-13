package uk.gov.hmcts.reform.sscs.services;

public class SftpCaseLoaderImpl implements CaseLoaderService {

    //TODO Implement once we know we can use SFTP
    @Override
    public boolean fetchXmlFilesFromGaps2() {
        return true;
    }

    //TODO Implement once we know we can use SFTP
    @Override
    public boolean validateXmlFiles() {
        return true;
    }
}
