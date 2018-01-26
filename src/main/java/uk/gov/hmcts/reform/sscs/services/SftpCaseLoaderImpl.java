package uk.gov.hmcts.reform.sscs.services;

import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.sscs.models.CcdCase;
import uk.gov.hmcts.reform.sscs.models.JsonFiles;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;
import uk.gov.hmcts.reform.sscs.processor.DeltaFileProcessor;
import uk.gov.hmcts.reform.sscs.utils.FileUtils;

import java.util.Optional;

@Service
public class SftpCaseLoaderImpl implements CaseLoaderService {
    
    @Autowired
    private DeltaFileProcessor deltaFileProcessor;
    
    @Override
    public Optional<XmlFiles> fetchXmlFilesFromGaps2() {
        String deltaXml = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
        String refXml = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.xml";
        return Optional.of(XmlFiles.builder().delta(deltaXml).ref(refXml).build());
    }

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

    @Override
    public CcdCase process(JSONObject json) {
        return deltaFileProcessor.process(json);
    }
}
