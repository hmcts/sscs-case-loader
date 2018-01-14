package uk.gov.hmcts.reform.sscs.services;

import org.json.JSONObject;

public interface CaseLoaderService {
    boolean fetchXmlFilesFromGaps2();

    boolean validateXmlFiles();

    JSONObject transformXmlFilesToJson(String filePath);
}
