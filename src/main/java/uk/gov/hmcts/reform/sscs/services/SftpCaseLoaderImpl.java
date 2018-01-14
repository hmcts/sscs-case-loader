package uk.gov.hmcts.reform.sscs.services;

import org.json.JSONObject;
import org.json.XML;
import uk.gov.hmcts.reform.sscs.models.JsonFiles;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import java.util.Optional;

public class SftpCaseLoaderImpl implements CaseLoaderService {

    //TODO Implement once we know we can use SFTP
    @Override
    public Optional<XmlFiles> fetchXmlFilesFromGaps2() {
        return Optional.empty();
    }

    //TODO Implement once we know we can use SFTP
    @Override
    public boolean validateXmlFiles(XmlFiles xmlFiles) {
        return true;
    }

    @Override
    public JsonFiles transformXmlFilesToJsonFiles(XmlFiles xmlFiles) {
        JSONObject jsonDelta = XML.toJSONObject(FileUtils.getFileContentGivenFilePath(xmlFiles.getDelta()));
        JSONObject jsonRef = XML.toJSONObject(FileUtils.getFileContentGivenFilePath(xmlFiles.getRef()));
        return JsonFiles.builder().delta(jsonDelta).ref(jsonRef).build();
    }
}
