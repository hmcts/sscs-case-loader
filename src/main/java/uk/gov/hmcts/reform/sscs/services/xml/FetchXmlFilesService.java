package uk.gov.hmcts.reform.sscs.services.xml;

import uk.gov.hmcts.reform.sscs.models.XmlFiles;

import java.util.Optional;

public interface FetchXmlFilesService {
    Optional<XmlFiles> fetch();
}
