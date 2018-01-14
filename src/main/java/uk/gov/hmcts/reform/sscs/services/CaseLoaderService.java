package uk.gov.hmcts.reform.sscs.services;

import uk.gov.hmcts.reform.sscs.models.JsonFiles;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;

import java.util.Optional;

public interface CaseLoaderService {
    Optional<XmlFiles> fetchXmlFilesFromGaps2();

    boolean validateXmlFiles(XmlFiles xmlFiles);

    JsonFiles transformXmlFilesToJsonFiles(XmlFiles xmlFiles);
}
