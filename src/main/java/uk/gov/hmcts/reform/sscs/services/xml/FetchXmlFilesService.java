package uk.gov.hmcts.reform.sscs.services.xml;

import java.util.Optional;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;

public interface FetchXmlFilesService {
    Optional<XmlFiles> fetch();
}
