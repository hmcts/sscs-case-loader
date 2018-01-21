package uk.gov.hmcts.reform.sscs.services;

import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.JsonFiles;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

@Service
public class TransformXmlFilesToJsonFilesService {
    public JsonFiles transform(XmlFiles xmlFiles) {
        JSONObject jsonDelta = XML.toJSONObject(FileUtils.getFileContentGivenFilePath(xmlFiles.getDelta()));
        JSONObject jsonRef = XML.toJSONObject(FileUtils.getFileContentGivenFilePath(xmlFiles.getRef()));
        return JsonFiles.builder().delta(jsonDelta).ref(jsonRef).build();
    }
}
