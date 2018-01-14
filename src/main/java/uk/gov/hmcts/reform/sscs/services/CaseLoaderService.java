package uk.gov.hmcts.reform.sscs.services;

import org.json.JSONObject;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;

import java.util.Optional;

public interface CaseLoaderService {
    Optional<XmlFiles> fetchXmlFilesFromGaps2();

    boolean validateXmlFiles(XmlFiles xmlFiles);

    JSONObject transformXmlFilesToJson(XmlFiles xmlFiles);
}
