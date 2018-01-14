package uk.gov.hmcts.reform.sscs.services;

import org.json.JSONObject;
import org.json.XML;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

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

    @Override
    public JSONObject transformXmlFilesToJson(String filePath) {
        return XML.toJSONObject(
            FileUtils.getFileContentGivenFilePath(filePath));
    }
}
